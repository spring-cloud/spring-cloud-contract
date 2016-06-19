/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.VersionRangeRequest
import org.eclipse.aether.resolution.VersionRangeResult
import org.eclipse.aether.resolution.VersionRequest
import org.eclipse.aether.resolution.VersionResult

import static AetherFactories.newRepositories
import static AetherFactories.newRepositorySystem
import static AetherFactories.newSession
import static org.springframework.cloud.contract.stubrunner.util.ZipCategory.unzipTo
import static java.nio.file.Files.createTempDirectory

/**
 * @author Mariusz Smykula
 */
@CompileStatic
@Slf4j
class AetherStubDownloader implements StubDownloader {

	private static final String TEMP_DIR_PREFIX = 'contracts'
	private static final String ARTIFACT_EXTENSION = 'jar'
	private static final String LATEST_ARTIFACT_VERSION = '(,]'
	private static final String LATEST_VERSION_IN_IVY = '+'

	private final List<RemoteRepository> remoteRepos
	private final RepositorySystem repositorySystem
	private final RepositorySystemSession session

	AetherStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.remoteRepos = remoteRepositories(stubRunnerOptions)
		if (!remoteRepos) {
			log.error('Remote repositories for stubs are not specified!')
		}
		this.repositorySystem = newRepositorySystem()
		this.session = newSession(this.repositorySystem, stubRunnerOptions.workOffline)
	}

	/**
	 * Used by the Maven Plugin
	 *
	 * @param repositorySystem
	 * @param remoteRepositories - remote artifact repositories
	 * @param session
	 * @param workOffline
	 */
	AetherStubDownloader(RepositorySystem repositorySystem,
	                     List<RemoteRepository> remoteRepositories,
	                     RepositorySystemSession session) {
		this.remoteRepos = remoteRepositories
		this.repositorySystem = repositorySystem
		this.session = session
		if (!remoteRepos) {
			log.error('Remote remoteRepositories for stubs are not specified!')
		}
	}

	private List<RemoteRepository> remoteRepositories(StubRunnerOptions stubRunnerOptions) {
		return newRepositories(stubRunnerOptions.stubRepositoryRoot.split(',').toList())
	}

	private File unpackedJar(String resolvedVersion, String stubsGroup, String stubsModule, String classifier) {
		log.info("Resolved version is [$resolvedVersion]")
		if (!resolvedVersion) {
			log.warn("Stub for group [$stubsGroup] module [$stubsModule] and classifier [$classifier] not found in $remoteRepos")
			return null
		}
		Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, resolvedVersion)
		ArtifactRequest request = new ArtifactRequest(artifact: artifact, repositories: remoteRepos)
		log.info("Resolving artifact '$artifact' using remote repositories $remoteRepos.url")
		try {
			ArtifactResult result = repositorySystem.resolveArtifact(session, request)
			log.info("Resolved artifact $artifact to ${result.artifact.file}")
			File temporaryFile = unpackStubJarToATemporaryFolder(result.artifact.file.toURI())
			log.info("Unpacked file to [$temporaryFile]")
			return temporaryFile
		} catch (Exception e) {
			log.warn("Exception occured while trying to download a stub for group [$stubsGroup] module [$stubsModule] and classifier [$classifier] in $remoteRepos", e)
			return null
		}

	}

	private String getVersion(String stubsGroup, String stubsModule, String version, String classifier) {
		if (!version || LATEST_VERSION_IN_IVY == version) {
			log.info("Desired version is [$version] - will try to resolve the latest version")
			return resolveHighestArtifactVersion(stubsGroup, stubsModule, classifier)
		}
		log.info("Will try to resolve version [$version]")
		return resolveArtifactVersion(stubsGroup, stubsModule, version, classifier)
	}

	@Override
	Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(StubRunnerOptions options, StubConfiguration stubConfiguration) {
		String version = getVersion(stubConfiguration.groupId, stubConfiguration.artifactId, stubConfiguration.version, stubConfiguration.classifier)
		File unpackedJar = unpackedJar(version, stubConfiguration.groupId, stubConfiguration.artifactId,
				stubConfiguration.classifier)
		if (!unpackedJar) {
			return null
		}
		return new AbstractMap.SimpleEntry(new StubConfiguration(stubConfiguration.groupId, stubConfiguration.artifactId, version, stubConfiguration.classifier),
				unpackedJar)
	}

	private String resolveHighestArtifactVersion(String stubsGroup, String stubsModule, String classifier) {
		Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, LATEST_ARTIFACT_VERSION)
		VersionRangeRequest versionRangeRequest = new VersionRangeRequest(artifact, remoteRepos, null)
		VersionRangeResult rangeResult = repositorySystem.resolveVersionRange(session, versionRangeRequest)
		if (!rangeResult.highestVersion) {
			log.error("Version was not resolved!")
		}
		return rangeResult.highestVersion ?: ''
	}

	private String resolveArtifactVersion(String stubsGroup, String stubsModule, String version, String classifier) {
		Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, version)
		VersionRequest versionRequest = new VersionRequest(artifact, remoteRepos, null)
		VersionResult versionResult = repositorySystem.resolveVersion(session, versionRequest)
		return versionResult.version ?: ''
	}

	private static File unpackStubJarToATemporaryFolder(URI stubJarUri) {
		File tmpDirWhereStubsWillBeUnzipped = createTempDirectory(TEMP_DIR_PREFIX).toFile()
		tmpDirWhereStubsWillBeUnzipped.deleteOnExit()
		log.info("Unpacking stub from JAR [URI: ${stubJarUri}]")
		unzipTo(new File(stubJarUri), tmpDirWhereStubsWillBeUnzipped)
		return tmpDirWhereStubsWillBeUnzipped
	}

}