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

import static io.codearte.accurest.stubrunner.util.ZipCategory.unzipTo
import static java.nio.file.Files.createTempDirectory

@PackageScope
@CompileStatic
@Slf4j
class AetherStubDownloader implements StubDownloader {

    private static final String ACCUREST_TEMP_DIR_PREFIX = 'accurest'
    private static final String ARTIFACT_EXTENSION = 'jar'
    private static final String ARTIFACT_VERSION = 'LATEST'

    private final List<RemoteRepository> remoteRepos
    private final RepositorySystem repoSystem
    private final RepositorySystemSession repoSession

    AetherStubDownloader(RepositorySystem repoSystem, List<RemoteRepository> remoteRepos, RepositorySystemSession repoSession) {
        this.remoteRepos = remoteRepos
        this.repoSystem = repoSystem
        this.repoSession = repoSession
    }

    @Override
    public File downloadAndUnpackStubJar(boolean workOffline, String stubRepositoryRoot, String stubsGroup, String stubsModule, String classifier) {
        Artifact artifact = new DefaultArtifact(stubsGroup, stubsModule, classifier, ARTIFACT_EXTENSION, ARTIFACT_VERSION)
        ArtifactRequest request = new ArtifactRequest(artifact: artifact, repositories: remoteRepos)
        log.info("Resolving artifact $artifact from $remoteRepos")
        ArtifactResult result = repoSystem.resolveArtifact(repoSession, request)
        log.info("Resolved artifact $artifact to ${result.artifact.file} from ${result.repository}")
        return unpackStubJarToATemporaryFolder(result.artifact.file.toURI())
    }

    private static File unpackStubJarToATemporaryFolder(URI stubJarUri) {
        File tmpDirWhereStubsWillBeUnzipped = createTempDirectory(ACCUREST_TEMP_DIR_PREFIX).toFile()
        tmpDirWhereStubsWillBeUnzipped.deleteOnExit()
        log.info("Unpacking stub from JAR [URI: ${stubJarUri}]")
        unzipTo(new File(stubJarUri), tmpDirWhereStubsWillBeUnzipped)
        return tmpDirWhereStubsWillBeUnzipped
    }

}
