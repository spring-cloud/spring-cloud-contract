/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import shaded.com.google.common.base.Function;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class ResourceResolvingStubDownloader implements StubDownloader {

	private static final Log log = LogFactory
			.getLog(ResourceResolvingStubDownloader.class);

	private final StubRunnerOptions stubRunnerOptions;

	private final BiFunction<StubRunnerOptions, StubConfiguration, RepoRoots> repoRootFunction;

	private final Function<StubConfiguration, Pattern> gavPattern;

	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
			new DefaultResourceLoader());

	ResourceResolvingStubDownloader(StubRunnerOptions stubRunnerOptions,
			BiFunction<StubRunnerOptions, StubConfiguration, RepoRoots> repoRootFunction,
			Function<StubConfiguration, Pattern> gavPattern) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.repoRootFunction = repoRootFunction;
		this.gavPattern = gavPattern;
	}

	@Override
	public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration config) {
		registerShutdownHook();
		List<RepoRoot> repoRoots = repoRootFunction.apply(stubRunnerOptions, config);
		List<String> paths = toPaths(repoRoots);
		List<Resource> resources = resolveResources(paths);
		if (log.isDebugEnabled()) {
			log.debug("For paths " + paths + " found following resources " + resources);
		}
		if (resources.isEmpty() && this.stubRunnerOptions.isFailOnNoStubs()) {
			throw new IllegalStateException("No stubs were found on classpath for ["
					+ config.getGroupId() + ":" + config.getArtifactId() + "]");
		}
		final File tmp = TemporaryFileStorage.createTempDir("classpath-stubs");
		if (stubRunnerOptions.isDeleteStubsAfterTest()) {
			tmp.deleteOnExit();
		}
		boolean atLeastOneFound = false;
		for (Resource resource : resources) {
			try {
				String relativePath = relativePathPicker(resource,
						this.gavPattern.apply(config));
				if (log.isDebugEnabled()) {
					log.debug("Relative path for resource is [" + relativePath + "]");
				}
				if (relativePath == null) {
					log.warn("Unable to match the URI [" + resource.getURI() + "]");
					continue;
				}
				atLeastOneFound = true;
				copyTheFoundFiles(tmp, resource, relativePath);
			}
			catch (IOException e) {
				log.error("Exception occurred while trying to create dirs", e);
				throw new IllegalStateException(e);
			}
		}
		if (!atLeastOneFound) {
			log.warn("Didn't find any matching stubs");
			return null;
		}
		log.info("Unpacked files for [" + config.getGroupId() + ":"
				+ config.getArtifactId() + ":" + config.getVersion() + "] to folder ["
				+ tmp + "]");
		return new AbstractMap.SimpleEntry<>(new StubConfiguration(config.getGroupId(),
				config.getArtifactId(), config.getVersion(), config.getClassifier()),
				tmp);
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> TemporaryFileStorage
				.cleanup(stubRunnerOptions.isDeleteStubsAfterTest())));
	}

	private void copyTheFoundFiles(File tmp, Resource resource, String relativePath)
			throws IOException {
		// the relative path is OS agnostic and contains / only
		int lastIndexOf = relativePath.lastIndexOf("/");
		String relativePathWithoutFile = lastIndexOf > -1
				? relativePath.substring(0, lastIndexOf) : relativePath;
		if (log.isDebugEnabled()) {
			log.debug("Relative path without file name is [" + relativePathWithoutFile
					+ "]");
		}
		Path directory = Files
				.createDirectories(new File(tmp, relativePathWithoutFile).toPath());
		File newFile = new File(directory.toFile(), resource.getFilename());
		if (!newFile.exists() && !isDirectory(resource)) {
			try (InputStream stream = resource.getInputStream()) {
				Files.copy(stream, newFile.toPath());
				TemporaryFileStorage.add(newFile);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Stored file [" + newFile + "]");
		}
	}

	boolean isDirectory(Resource resource) {
		try {
			return resource.getFile().isDirectory();
		}
		catch (Exception e) {
			if (log.isTraceEnabled()) {
				log.trace(
						"Exception occurred while trying to convert path to file for resource ["
								+ resource + "]",
						e);
			}
			return false;
		}
	}

	String relativePathPicker(Resource resource, Pattern groupAndArtifactPattern)
			throws IOException {
		Matcher groupAndArtifactMatcher = matcher(resource, groupAndArtifactPattern);
		if (groupAndArtifactMatcher.matches()
				&& groupAndArtifactMatcher.groupCount() > 2) {
			MatchResult groupAndArtifactResult = groupAndArtifactMatcher.toMatchResult();
			return groupAndArtifactResult.group(2) + groupAndArtifactResult.group(3);
		}
		else if (groupAndArtifactMatcher.matches()) {
			return groupAndArtifactMatcher.group(1);
		}
		else {
			return null;
		}
	}

	private Matcher matcher(Resource resource, Pattern groupAndArtifactPattern)
			throws IOException {
		try {
			String path = resource.getURI().getPath();
			return groupAndArtifactPattern.matcher(path);
		}
		catch (Exception ex) {
			String path = resource.getURI().toString();
			return groupAndArtifactPattern.matcher(path);
		}
	}

	private List<String> toPaths(List<RepoRoot> repoRoots) {
		List<String> list = new ArrayList<>();
		for (RepoRoot repoRoot : repoRoots) {
			list.add(repoRoot.fullPath);
		}
		return list;
	}

	List<Resource> resolveResources(List<String> paths) {
		List<Resource> resources = new ArrayList<>();
		for (String path : paths) {
			try {
				List<Resource> list = Arrays.asList(this.resolver.getResources(path));
				resources.addAll(list);
			}
			catch (IOException e) {
				log.error("Exception occurred while trying to fetch resources from ["
						+ path + "]");
				throw new IllegalStateException(e);
			}
		}
		return resources;
	}

}

class RepoRoot {

	final String repoRoot;

	final String fullPath;

	RepoRoot(String repoRoot) {
		this.repoRoot = repoRoot;
		this.fullPath = repoRoot;
	}

	RepoRoot(String repoRoot, String suffix) {
		this.repoRoot = repoRoot;
		this.fullPath = repoRoot + suffix;
	}

}

class RepoRoots extends LinkedList<RepoRoot> {

	RepoRoots() {
	}

	RepoRoots(Collection<? extends RepoRoot> c) {
		super(c);
	}

	static RepoRoots asList(RepoRoot... repoRoots) {
		return new RepoRoots(Arrays.asList(repoRoots));
	}

}
