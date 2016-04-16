package io.codearte.accurest.maven.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.maven.project.MavenProject
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named
@Singleton
@CompileStatic
@Slf4j
class AetherStubDownloaderFactory {

    private final MavenProject project
    private final RepositorySystem repoSystem

    @Inject
    AetherStubDownloaderFactory(RepositorySystem repoSystem, MavenProject project) {
        this.repoSystem = repoSystem
        this.project = project
    }

    AetherStubDownloader build(RepositorySystemSession repoSession) {
        return new AetherStubDownloader(repoSystem, project.remoteProjectRepositories, repoSession)
    }

}
