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
import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import org.springframework.cloud.contract.stubrunner.StubRunnerOptions.StubRunnerProxyOptions;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.contract.stubrunner.AetherFactories.newRepositorySystem;
import static org.springframework.cloud.contract.stubrunner.AetherFactories.newSession;
import static org.springframework.cloud.contract.stubrunner.util.ZipCategory.unzipTo;

/**
 * @author Mariusz Smykula
 */
public class AetherStubDownloader implements StubDownloader {

	private static final Log log = LogFactory.getLog(AetherStubDownloader.class);

	private static final String TEMP_DIR_PREFIX = "contracts";

	private static final String ARTIFACT_EXTENSION = "jar";

	private static final String LATEST_ARTIFACT_VERSION = "(,]";

	private static final String LATEST_VERSION_IN_IVY = "+";

	// Preloading class for the shutdown hook not to throw ClassNotFound
	private static final Class CLAZZ = TemporaryFileStorage.class;

	private final List<RemoteRepository> remoteRepos;

	private final RepositorySystem repositorySystem;

	private final RepositorySystemSession session;

	private final boolean workOffline;

	private final boolean deleteStubsAfterTest;

	public AetherStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.deleteStubsAfterTest = stubRunnerOptions.isDeleteStubsAfterTest();
		if (log.isDebugEnabled()) {
			log.debug("Will be resolving versions for the following options: ["
					+ stubRunnerOptions + "]");
		}
		this.remoteRepos = remoteRepositories(stubRunnerOptions);
		boolean remoteReposMissing = remoteReposMissing();
		switch (stubRunnerOptions.stubsMode) {
		case LOCAL:
			log.info("Remote repos not passed but the switch to work offline was set. "
					+ "Stubs will be used from your local Maven repository.");
			break;
		case REMOTE:
			if (remoteReposMissing) {
				throw new IllegalStateException(
						"Remote repositories for stubs are not specified and work offline flag wasn't passed");
			}
			break;
		case CLASSPATH:
			throw new UnsupportedOperationException(
					"You can't use Aether downloader when you use classpath to find stubs");
		}
		this.repositorySystem = newRepositorySystem();
		this.workOffline = stubRunnerOptions.stubsMode == StubRunnerProperties.StubsMode.LOCAL;
		this.session = newSession(this.repositorySystem, this.workOffline);
		registerShutdownHook();
	}

	/**
	 * Used by the Maven Plugin.
	 * @param repositorySystem Maven repository system
	 * @param remoteRepositories remote artifact repositories
	 * @param session repository system session
	 */
	public AetherStubDownloader(RepositorySystem repositorySystem,
			List<RemoteRepository> remoteRepositories, RepositorySystemSession session) {
		this.deleteStubsAfterTest = true;
		this.remoteRepos = remoteRepositories;
		this.repositorySystem = repositorySystem;
		this.session = session;
		if (remoteReposMissing()) {
			log.error(
					"Remote repositories for stubs are not specified and work offline flag wasn't passed");
		}
		this.workOffline = false;
		registerShutdownHook();
	}

	private static File unpackStubJarToATemporaryFolder(URI stubJarUri) {
		File tmpDirWhereStubsWillBeUnzipped = TemporaryFileStorage
				.createTempDir(TEMP_DIR_PREFIX);
		log.info("Unpacking stub from JAR [URI: " + stubJarUri + "]");
		unzipTo(new File(stubJarUri), tmpDirWhereStubsWillBeUnzipped);
		TemporaryFileStorage.add(tmpDirWhereStubsWillBeUnzipped);
		return tmpDirWhereStubsWillBeUnzipped;
	}

	private boolean remoteReposMissing() {
		return this.remoteRepos == null || this.remoteRepos.isEmpty();
	}

	private List<RemoteRepository> remoteRepositories(
			StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.stubRepositoryRoot == null) {
			return new ArrayList<>();
		}
		final String[] repos = stubRunnerOptions.getStubRepositoryRootAsString()
				.split(",");
		final List<RemoteRepository> remoteRepos = new ArrayList<>();
		for (int i = 0; i < repos.length; i++) {
			if (StringUtils.hasText(repos[i])) {
				final RemoteRepository.Builder builder = new RemoteRepository.Builder(
						"remote" + i, "default", repos[i])
								.setAuthentication(new AuthenticationBuilder()
										.addUsername(stubRunnerOptions.username)
										.addPassword(stubRunnerOptions.password).build());
				if (stubRunnerOptions.getProxyOptions() != null) {
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
		try {
			log.info("Resolved version is [" + resolvedVersion + "]");
			if (StringUtils.isEmpty(resolvedVersion)) {
				log.warn("Stub for group [" + stubsGroup + "] module [" + stubsModule
						+ "] and classifier [" + classifier + "] not found in "
						+ this.remoteRepos);
				return null;
			}
			Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier,
					ARTIFACT_EXTENSION, resolvedVersion);
			ArtifactRequest request = new ArtifactRequest(artifact, this.remoteRepos,
					null);
			if (log.isDebugEnabled()) {
				log.debug("Resolving artifact [" + artifact
						+ "] using remote repositories " + this.remoteRepos);
			}
			ArtifactResult result = this.repositorySystem.resolveArtifact(this.session,
					request);
			log.info("Resolved artifact [" + artifact + "] to "
					+ result.getArtifact().getFile());
			File temporaryFile = unpackStubJarToATemporaryFolder(
					result.getArtifact().getFile().toURI());
			log.info("Unpacked file to [" + temporaryFile + "]");
			return temporaryFile;
		}
		catch (IllegalStateException ise) {
			throw ise;
		}
		catch (Exception e) {
			throw new IllegalStateException(
					"Exception occurred while trying to download a stub for group ["
							+ stubsGroup + "] module [" + stubsModule
							+ "] and classifier [" + classifier + "] in "
							+ this.remoteRepos,
					e);
		}
	}

	private String getVersion(String stubsGroup, String stubsModule, String version,
			String classifier) {
		if (StringUtils.isEmpty(version) || LATEST_VERSION_IN_IVY.equals(version)) {
			log.info("Desired version is [" + version
					+ "] - will try to resolve the latest version");
			return resolveHighestArtifactVersion(stubsGroup, stubsModule, classifier,
					LATEST_ARTIFACT_VERSION);
		}
		return resolveHighestArtifactVersion(stubsGroup, stubsModule, classifier,
				version);
	}

	@Override
	public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		try {
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
			return new AbstractMap.SimpleEntry<>(new StubConfiguration(
					stubConfiguration.groupId, stubConfiguration.artifactId, version,
					stubConfiguration.classifier), unpackedJar);
		}
		catch (Exception ex) {
			log.warn("Exception occurred while trying to fetch the stubs", ex);
			return null;
		}
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
			throw new IllegalArgumentException("For groupId [" + stubsGroup
					+ "] artifactId [" + stubsModule + "] " + "and classifier ["
					+ classifier
					+ "] the version was not resolved! The following exceptions took place "
					+ rangeResult.getExceptions());
		}
		return rangeResult.getHighestVersion() == null ? null
				: rangeResult.getHighestVersion().toString();
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> TemporaryFileStorage
				.cleanup(AetherStubDownloader.this.deleteStubsAfterTest)));
	}

}
