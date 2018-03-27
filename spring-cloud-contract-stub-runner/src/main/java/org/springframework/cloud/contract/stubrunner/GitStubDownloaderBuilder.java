/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.stubrunner.util.StringUtils;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Builds a {@link StubDownloader} to work with contracts and stubs in a git repo
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class GitStubDownloaderBuilder implements StubDownloaderBuilder {

	private static final String GIT_PROTOCOL = "git";

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.getStubsMode() == StubRunnerProperties.StubsMode.CLASSPATH ||
				stubRunnerOptions.getStubRepositoryRoot() == null) {
			return null;
		}
		Resource resource = stubRunnerOptions.getStubRepositoryRoot();
		if (!(resource instanceof GitResource)) {
			return null;
		}
		return new GitStubDownloader(stubRunnerOptions);
	}

	@Override public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (!StringUtils.hasText(location) || !location.startsWith(GIT_PROTOCOL)) {
			return null;
		}
		return new GitResource(location);
	}
}


/**
 * Primitive version of a Git {@link Resource}
 */
class GitResource extends AbstractResource {

	private final String rawLocation;

	GitResource(String location) {
		this.rawLocation = location;
	}

	@Override public String getDescription() {
		return this.rawLocation;
	}

	@Override public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override public URI getURI() throws IOException {
		return URI.create(this.rawLocation);
	}
}

class GitContractsRepo {

	private static final Log log = LogFactory.getLog(GitContractsRepo.class);

	private static final String TEMP_DIR_PREFIX = "git-contracts";
	static final Map<Resource, File> CACHED_LOCATIONS = new ConcurrentHashMap<>();

	private final StubRunnerOptions options;

	GitContractsRepo(StubRunnerOptions options) {
		this.options = options;
	}

	File clonedRepo(Resource repo) {
		File file = CACHED_LOCATIONS.get(repo);
		GitStubDownloaderProperties properties = new GitStubDownloaderProperties(repo, this.options);
		if (file == null) {
			File tmpDirWhereStubsWillBeUnzipped = TemporaryFileStorage.unpackStubJarToATemporaryFolder(TEMP_DIR_PREFIX);
			GitRepo gitRepo = new GitRepo(tmpDirWhereStubsWillBeUnzipped, properties);
			file = gitRepo.cloneProject(properties.url);
			gitRepo.checkout(file, properties.branch);
			CACHED_LOCATIONS.put(repo, file);
			if (log.isDebugEnabled()) {
				log.debug("The project hasn't already been cloned. Cloned it to [" + file + "]");
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("The project has already been cloned to [" + file + "]. Will reset any changes.");
			}
			new GitRepo(file, properties).reset(file);
		}
		return file;
	}
}

class GitStubDownloader implements StubDownloader {

	private final StubRunnerOptions stubRunnerOptions;
	private final boolean deleteStubsAfterTest;
	private final GitContractsRepo gitContractsRepo;

	GitStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.deleteStubsAfterTest = this.stubRunnerOptions.isDeleteStubsAfterTest();
		this.gitContractsRepo = new GitContractsRepo(stubRunnerOptions);
		registerShutdownHook();
	}

	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		if (!StringUtils.hasText(stubConfiguration.version) || "+".equals(stubConfiguration.version)) {
			throw new IllegalStateException("Concrete version wasn't passed for [" + stubConfiguration.toColonSeparatedDependencyNotation() + "]");
		}
		try {
			Resource repo = this.stubRunnerOptions.getStubRepositoryRoot();
			File clonedRepo = this.gitContractsRepo.clonedRepo(repo);
			FileWalker walker = new FileWalker(stubConfiguration);
			Files.walkFileTree(clonedRepo.toPath(), walker);
			if (walker.foundFile != null) {
				return new AbstractMap.SimpleEntry<>(stubConfiguration, walker.foundFile.toFile());
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return null;
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(
				() -> TemporaryFileStorage.cleanup(GitStubDownloader.this.deleteStubsAfterTest)));
	}
}

class GitStubDownloaderProperties {
	private static final String GIT_BRANCH_PROPERTY = "git.branch";
	private static final String GIT_USERNAME_PROPERTY = "git.username";
	private static final String GIT_PASSWORD_PROPERTY = "git.password";

	final URI url;
	final String username;
	final String password;
	final String branch;

	GitStubDownloaderProperties(Resource repo, StubRunnerOptions options) {
		String repoUrl = null;
		Map<String, String> args = options.getProperties();
		try {
			repoUrl = schemeSpecificPart(repo.getURI());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		// if we had git://https://... we want the part starting from https
		// if we had git://git@... we want the full address again
		this.url = URI.create(repoUrl.startsWith("git@") ? "git://" + repoUrl : repoUrl);
		String username = StubRunnerPropertyUtils.getProperty(args, GIT_USERNAME_PROPERTY);
		this.username = StringUtils.hasText(username) ? username : options.getUsername();
		String password = StubRunnerPropertyUtils.getProperty(args, GIT_PASSWORD_PROPERTY);
		this.password = StringUtils.hasText(password) ? password : options.getPassword();
		String branch = StubRunnerPropertyUtils.getProperty(args, GIT_BRANCH_PROPERTY);
		this.branch = StringUtils.hasText(branch) ? branch : "master";
	}

	private String schemeSpecificPart(URI uri) {
		String part = uri.getSchemeSpecificPart();
		return StringUtils.hasText(part) && part.startsWith("//") ? part.substring(2) : part;
	}
}

class FileWalker extends SimpleFileVisitor<Path> {

	private final PathMatcher matcherWithDot;
	private final PathMatcher matcherWithoutDot;
	Path foundFile;

	FileWalker(StubConfiguration stubConfiguration) {
		this.matcherWithDot = FileSystems.getDefault()
				.getPathMatcher("glob:" + matcherGlob(stubConfiguration, "."));
		this.matcherWithoutDot = FileSystems.getDefault()
				.getPathMatcher("glob:" + matcherGlob(stubConfiguration, "/"));
	}

	private String matcherGlob(StubConfiguration stubConfiguration, String groupArtifactSeparator) {
		return "**" + stubConfiguration.groupId + groupArtifactSeparator
				+ stubConfiguration.artifactId + "/"
				+ stubConfiguration.version;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		if (this.matcherWithDot.matches(dir.toAbsolutePath()) ||
				this.matcherWithoutDot.matches(dir.toAbsolutePath())) {
			this.foundFile = dir;
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
	}
}