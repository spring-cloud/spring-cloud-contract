package io.codearte.accurest.maven.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.codearte.accurest.stubrunner.StubDownloader
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.VersionRangeRequest
import org.eclipse.aether.resolution.VersionRangeResult
import org.eclipse.aether.version.Version

import static io.codearte.accurest.stubrunner.util.ZipCategory.unzipTo
import static java.nio.file.Files.createTempDirectory

@PackageScope
@CompileStatic
@Slf4j
class AetherStubDownloader implements StubDownloader {

    private static final String ACCUREST_TEMP_DIR_PREFIX = 'accurest'
    private static final String ARTIFACT_EXTENSION = 'jar'
    private static final String ARTIFACT_VERSION = '(0,]'

    private final List<RemoteRepository> remoteRepos
    private final RepositorySystem repositorySystem
    private final RepositorySystemSession session

    AetherStubDownloader(RepositorySystem repositorySystem, List<RemoteRepository> repositories, RepositorySystemSession session) {
        this.remoteRepos = repositories
        this.repositorySystem = repositorySystem
        this.session = session
    }

    @Override
    public File downloadAndUnpackStubJar(boolean workOffline, String stubRepositoryRoot, String stubsGroup, String stubsModule, String classifier) {
        Version highestVersion = resolveArtifactVersion(stubsGroup, stubsModule, classifier);
        log.info("Resolving highest version is $highestVersion")
        Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, highestVersion.toString())
        ArtifactRequest request = new ArtifactRequest(artifact: artifact, repositories: remoteRepos)
        log.info("Resolving artifact $artifact from $remoteRepos")
        ArtifactResult result = repositorySystem.resolveArtifact(session, request)
        log.info("Resolved artifact $artifact to ${result.artifact.file} from ${result.repository}")
        return unpackStubJarToATemporaryFolder(result.artifact.file.toURI())
    }

    private Version resolveArtifactVersion(String stubsGroup, String stubsModule, String classifier) {
        Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, ARTIFACT_VERSION)
        VersionRangeRequest versionRangeRequest = new VersionRangeRequest(artifact, remoteRepos, null);
        VersionRangeResult rangeResult = repositorySystem.resolveVersionRange(session, versionRangeRequest);
        return rangeResult.highestVersion;
    }

    private static File unpackStubJarToATemporaryFolder(URI stubJarUri) {
        File tmpDirWhereStubsWillBeUnzipped = createTempDirectory(ACCUREST_TEMP_DIR_PREFIX).toFile()
        tmpDirWhereStubsWillBeUnzipped.deleteOnExit()
        log.info("Unpacking stub from JAR [URI: ${stubJarUri}]")
        unzipTo(new File(stubJarUri), tmpDirWhereStubsWillBeUnzipped)
        return tmpDirWhereStubsWillBeUnzipped
    }

}
