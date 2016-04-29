package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.*
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory

import static io.codearte.accurest.stubrunner.util.ZipCategory.unzipTo
import static java.nio.file.Files.createTempDirectory
/**
 * @author Mariusz Smykula
 */
@CompileStatic
@Slf4j
class AetherStubDownloader implements StubDownloader {

	private static final String MAVEN_LOCAL_REPOSITORY_LOCATION = 'maven.repo.local'
	private static final String ACCUREST_TEMP_DIR_PREFIX = 'accurest'
	private static final String ARTIFACT_EXTENSION = 'jar'
	private static final String LATEST_ARTIFACT_VERSION = '(0,]'
	private static final String LATEST_VERSION_IN_IVY = "+"

	private final List<RemoteRepository> remoteRepos
	private final RepositorySystem repositorySystem
	private final RepositorySystemSession session

	AetherStubDownloader(RepositorySystem repositorySystem, List<RemoteRepository> repositories, RepositorySystemSession session) {
		this.remoteRepos = repositories
		this.repositorySystem = repositorySystem ?: newRepositorySystem()
		this.session = session ?: newSession(this.repositorySystem)
	}

	AetherStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.remoteRepos = remoteRepositories(stubRunnerOptions)
		this.repositorySystem = newRepositorySystem()
		this.session = newSession(this.repositorySystem)
	}

	private List<RemoteRepository> remoteRepositories(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.workOffline || !stubRunnerOptions.stubRepositoryRoot) {
			return []
		}
		return stubRunnerOptions.stubRepositoryRoot.split(',').collect { String repo ->
			new RemoteRepository.Builder("remote", "default", repo).build()
		}
	}

	private RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator()
		locator.addService(RepositoryConnectorFactory, BasicRepositoryConnectorFactory)
		locator.addService(TransporterFactory, FileTransporterFactory)
		locator.addService(TransporterFactory, HttpTransporterFactory)
		return locator.getService(RepositorySystem)
	}

	private RepositorySystemSession newSession(RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		LocalRepository localRepo = new LocalRepository(System.getProperty(MAVEN_LOCAL_REPOSITORY_LOCATION, "${System.getProperty("user.home")}/.m2/repository"));
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
		return session;
	}

	@Override
	public File downloadAndUnpackStubJar(boolean workOffline, String stubRepositoryRoot, String stubsGroup, String stubsModule, String classifier) {
		String version = getVersion(stubsGroup, stubsModule, LATEST_VERSION_IN_IVY, classifier)
		return unpackedJar(version, stubsGroup, stubsModule, classifier, stubRepositoryRoot)
	}

	private File unpackedJar(String resolvedVersion, String stubsGroup, String stubsModule, String classifier, String stubRepositoryRoot) {
		log.info("Resolved version is $resolvedVersion")
		if (!resolvedVersion) {
			log.warn("Stub for group [$stubsGroup] module [$stubsModule] and classifier [$classifier] not found in [$stubRepositoryRoot]")
			return null
		}
		Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, resolvedVersion)
		ArtifactRequest request = new ArtifactRequest(artifact: artifact, repositories: remoteRepos)
		log.info("Resolving artifact $artifact from ${remoteRepos ?: 'local maven repo'}")
		try {
			ArtifactResult result = repositorySystem.resolveArtifact(session, request)
			log.info("Resolved artifact $artifact to ${result.artifact.file} from ${result.repository}")
			return unpackStubJarToATemporaryFolder(result.artifact.file.toURI())
		} catch (Exception e) {
			log.warn("Exception occured while trying to download a stub for group [$stubsGroup] module [$stubsModule] and classifier [$classifier] in [$stubRepositoryRoot]", e)
			return null
		}

	}

	private String getVersion(String stubsGroup, String stubsModule, String version, String classifier) {
		if (!version || LATEST_VERSION_IN_IVY == version) {
			log.info("Desired version is [$version] - will try to resolve the latest version")
			return resolveHighestArtifactVersion(stubsGroup, stubsModule, classifier);
		}
		log.info("Will try to resolve version [$version]")
		return resolveArtifactVersion(stubsGroup, stubsModule, version, classifier)
	}

	@Override
	Map.Entry<StubConfiguration,File> downloadAndUnpackStubJar(StubRunnerOptions options, StubConfiguration stubConfiguration) {
		String version = getVersion(stubConfiguration.groupId, stubConfiguration.artifactId, stubConfiguration.version, stubConfiguration.classifier)
		File unpackedJar = unpackedJar(version, stubConfiguration.groupId, stubConfiguration.artifactId,
				stubConfiguration.classifier, options.stubRepositoryRoot)
		if(!unpackedJar) {
			return null
		}
		return new AbstractMap.SimpleEntry(new StubConfiguration(stubConfiguration.groupId, stubConfiguration.artifactId, version, stubConfiguration.classifier),
		unpackedJar)
	}

	private String resolveHighestArtifactVersion(String stubsGroup, String stubsModule, String classifier) {
		Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, LATEST_ARTIFACT_VERSION)
		VersionRangeRequest versionRangeRequest = new VersionRangeRequest(artifact, remoteRepos, null);
		VersionRangeResult rangeResult = repositorySystem.resolveVersionRange(session, versionRangeRequest);
		return rangeResult.highestVersion ?: ''
	}

	private String resolveArtifactVersion(String stubsGroup, String stubsModule, String version, String classifier) {
		Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, version)
		VersionRequest versionRequest = new VersionRequest(artifact, remoteRepos, null)
		VersionResult versionResult = repositorySystem.resolveVersion(session, versionRequest);
		return versionResult.version ?: ''
	}

	private static File unpackStubJarToATemporaryFolder(URI stubJarUri) {
		File tmpDirWhereStubsWillBeUnzipped = createTempDirectory(ACCUREST_TEMP_DIR_PREFIX).toFile()
		tmpDirWhereStubsWillBeUnzipped.deleteOnExit()
		log.info("Unpacking stub from JAR [URI: ${stubJarUri}]")
		unzipTo(new File(stubJarUri), tmpDirWhereStubsWillBeUnzipped)
		return tmpDirWhereStubsWillBeUnzipped
	}

}