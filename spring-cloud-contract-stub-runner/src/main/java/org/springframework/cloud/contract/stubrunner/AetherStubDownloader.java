/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions.StubRunnerProxyOptions;
import org.springframework.util.StringUtils;

import static java.nio.file.Files.createTempDirectory;
import static org.springframework.cloud.contract.stubrunner.AetherFactories.newRepositorySystem;
import static org.springframework.cloud.contract.stubrunner.AetherFactories.newSession;
import static org.springframework.cloud.contract.stubrunner.util.ZipCategory.unzipTo;

/**
 * @author Mariusz Smykula
 */
public class AetherStubDownloader implements StubDownloader {

	/**
	 * There are problems with removal of stubs unpacked to a temporary folder.
	 * That's why we're creating a bounded in-memory storage of unpacked files
	 * and later we register a shutdown hook to remove all these files.
	 */
	private static final Queue<File> TEMP_FILES_LOG = new LinkedBlockingQueue<>(1000);

	private static final Logger log = LoggerFactory.getLogger(AetherStubDownloader.class);

	private static final String TEMP_DIR_PREFIX = "contracts";
	private static final String ARTIFACT_EXTENSION = "jar";
	private static final String LATEST_ARTIFACT_VERSION = "(,]";
	private static final String LATEST_VERSION_IN_IVY = "+";

	private final List<RemoteRepository> remoteRepos;
	private final RepositorySystem repositorySystem;
	private final RepositorySystemSession session;
	private final boolean workOffline;

	public AetherStubDownloader(StubRunnerOptions stubRunnerOptions) {
		if (log.isDebugEnabled()) {
			log.debug("Will be resolving versions for the following options: [" + stubRunnerOptions + "]");
		}
		this.remoteRepos = remoteRepositories(stubRunnerOptions);
		boolean remoteReposMissing = remoteReposMissing();
		if (remoteReposMissing && stubRunnerOptions.workOffline) {
			log.info("Remote repos not passed but the switch to work offline was set. "
					+ "Stubs will be used from your local Maven repository.");
		}
		if (remoteReposMissing && !stubRunnerOptions.workOffline) {
			throw new IllegalStateException("Remote repositories for stubs are not specified and work offline flag wasn't passed");
		}
		if (!remoteReposMissing && stubRunnerOptions.workOffline) {
			throw new IllegalStateException("Remote repositories for stubs are specified and work offline flag is set. "
					+ "You have to provide one of them.");
		}
		this.repositorySystem = newRepositorySystem();
		this.session = newSession(this.repositorySystem, stubRunnerOptions.workOffline);
		this.workOffline = stubRunnerOptions.workOffline;
		registerShutdownHook();
	}

	private boolean remoteReposMissing() {
		return this.remoteRepos == null || this.remoteRepos.isEmpty();
	}

	/**
	 * Used by the Maven Plugin
	 *
	 * @param repositorySystem
	 * @param remoteRepositories - remote artifact repositories
	 * @param session
	 */
	public AetherStubDownloader(RepositorySystem repositorySystem,
			List<RemoteRepository> remoteRepositories, RepositorySystemSession session) {
		this.remoteRepos = remoteRepositories;
		this.repositorySystem = repositorySystem;
		this.session = session;
		if (remoteReposMissing()) {
			log.error("Remote repositories for stubs are not specified and work offline flag wasn't passed");
		}
		this.workOffline = false;
		registerShutdownHook();
	}

	private List<RemoteRepository> remoteRepositories(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.stubRepositoryRoot == null) {
			return new ArrayList<>();
		}
		final String[] repos = stubRunnerOptions.stubRepositoryRoot.split(",");
		final List<RemoteRepository> remoteRepos = new ArrayList<>();
		for (int i = 0; i < repos.length; i++) {
			if(StringUtils.hasText(repos[i])) {
				final RemoteRepository.Builder builder = new RemoteRepository.Builder("remote" + i, "default", repos[i])
						.setAuthentication(new AuthenticationBuilder()
								.addUsername(stubRunnerOptions.username)
								.addPassword(stubRunnerOptions.password)
								.build());
				if(stubRunnerOptions.getProxyOptions() != null) {
					final StubRunnerProxyOptions p = stubRunnerOptions.getProxyOptions();
					builder.setProxy(new Proxy(null, p.getProxyHost(), p.getProxyPort()));
				}
				remoteRepos.add(builder.build());
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Using the following remote repos " + remoteRepos);
		}
		return remoteRepos;
	}

	private File unpackedJar(String resolvedVersion, String stubsGroup,
			String stubsModule, String classifier) {
		log.info("Resolved version is [" + resolvedVersion + "]");
		if (!StringUtils.hasText(resolvedVersion)) {
			log.warn("Stub for group [" + stubsGroup + "] module [" + stubsModule
					+ "] and classifier [" + classifier + "] not found in "
					+ this.remoteRepos);
			return null;
		}
		Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier,
				ARTIFACT_EXTENSION, resolvedVersion);
		ArtifactRequest request = new ArtifactRequest(artifact, this.remoteRepos, null);
		log.info("Resolving artifact [" + artifact
				+ "] using remote repositories " + this.remoteRepos);
		try {
			ArtifactResult result = this.repositorySystem.resolveArtifact(this.session, request);
			log.info("Resolved artifact [" + artifact + "] to "
					+ result.getArtifact().getFile());
			if (resolvedFromLocalRepo(result) && shouldDownloadFromRemote()) {
				throw new IllegalStateException("The artifact was found in the local repository "
						+ "but you have explicitly stated that it should be downloaded from a remote one");
			}
			File temporaryFile = unpackStubJarToATemporaryFolder(
					result.getArtifact().getFile().toURI());
			log.info("Unpacked file to [" + temporaryFile + "]");
			return temporaryFile;
		}
		catch (IllegalStateException ise) {
			throw ise;
		}
		catch (Exception e) {
			//TODO: Start throwing this exception instead of returning null
			log.warn(
					"Exception occurred while trying to download a stub for group ["
							+ stubsGroup + "] module [" + stubsModule
							+ "] and classifier [" + classifier + "] in " + this.remoteRepos,
					e);
			return null;
		}

	}

	private boolean resolvedFromLocalRepo(ArtifactResult result) {
		return result.getRepository() instanceof LocalRepository;
	}

	private boolean shouldDownloadFromRemote() {
		return !remoteReposMissing() && !this.workOffline;
	}

	private String getVersion(String stubsGroup, String stubsModule, String version,
			String classifier) {
		if (!StringUtils.hasText(version) || LATEST_VERSION_IN_IVY.equals(version)) {
			log.info("Desired version is [" + version
					+ "] - will try to resolve the latest version");
			return resolveHighestArtifactVersion(stubsGroup, stubsModule, classifier, LATEST_ARTIFACT_VERSION);
		}
		return resolveHighestArtifactVersion(stubsGroup, stubsModule, classifier, version);
	}

	@Override
	public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(StubConfiguration stubConfiguration) {
		String version = getVersion(stubConfiguration.groupId,
				stubConfiguration.artifactId, stubConfiguration.version,
				stubConfiguration.classifier);
		if (log.isDebugEnabled()) {
			log.debug("Will download the stub for version [" + version + "]");
		}
		File unpackedJar = unpackedJar(version, stubConfiguration.groupId,
				stubConfiguration.artifactId, stubConfiguration.classifier);
		if (unpackedJar == null) {
			return null;
		}
		return new AbstractMap.SimpleEntry<>(
				new StubConfiguration(stubConfiguration.groupId, stubConfiguration.artifactId, version,
						stubConfiguration.classifier), unpackedJar);
	}

	private String resolveHighestArtifactVersion(String stubsGroup, String stubsModule,
			String classifier, String version) {
		Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier,
				ARTIFACT_EXTENSION, version);
		VersionRangeRequest versionRangeRequest = new VersionRangeRequest(artifact,
				this.remoteRepos, null);
		VersionRangeResult rangeResult;
		try {
			rangeResult = this.repositorySystem.resolveVersionRange(this.session,
					versionRangeRequest);
			if (log.isDebugEnabled()) {
				log.debug("Resolved version range is [" + rangeResult + "]");
			}
		}
		catch (VersionRangeResolutionException e) {
			throw new IllegalStateException("Cannot resolve version range", e);
		}
		if (rangeResult.getHighestVersion() == null) {
			throw new IllegalArgumentException("For groupId [" + stubsGroup + "] artifactId [" + stubsModule + "] "
					+ "and classifier [" + classifier + "] the version was not resolved! The following exceptions took place "
					+ rangeResult.getExceptions());
		}
		return rangeResult.getHighestVersion() == null ? null : rangeResult.getHighestVersion().toString();
	}

	private static File unpackStubJarToATemporaryFolder(URI stubJarUri) {
		File tmpDirWhereStubsWillBeUnzipped;
		try {
			tmpDirWhereStubsWillBeUnzipped = createTempDirectory(TEMP_DIR_PREFIX)
					.toFile();
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot create tmp dir with prefix: [" + TEMP_DIR_PREFIX + "]", e);
		}
		tmpDirWhereStubsWillBeUnzipped.deleteOnExit();
		log.info("Unpacking stub from JAR [URI: " + stubJarUri + "]");
		unzipTo(new File(stubJarUri), tmpDirWhereStubsWillBeUnzipped);
		TEMP_FILES_LOG.add(tmpDirWhereStubsWillBeUnzipped);
		return tmpDirWhereStubsWillBeUnzipped;
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				cleanup();
			}
		});
	}

	private void cleanup() {
		try {
			for (File file : TEMP_FILES_LOG) {
				if (file.isDirectory()) {
					Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if (log.isTraceEnabled()) {
								log.trace("Removing unzipped file [" + file + "]");
							}
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							if (log.isTraceEnabled()) {
								log.trace("Removing unzipped dir [" + dir + "]");
							}
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
					});
				} else {
					Files.delete(file.toPath());
				}
			}
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to remove temporary file", e);
			}
		}
	}
}