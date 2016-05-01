package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.maven.stubrunner.LocalStubRunner
import io.codearte.accurest.maven.stubrunner.RemoteStubRunner
import io.codearte.accurest.stubrunner.BatchStubRunner
import io.codearte.accurest.stubrunner.StubRunner
import io.codearte.accurest.stubrunner.StubRunnerOptions
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.eclipse.aether.RepositorySystemSession

import javax.inject.Inject

@Mojo(name = 'run', requiresProject = false, requiresDependencyResolution = ResolutionScope.RUNTIME)
@CompileStatic
class RunMojo extends AbstractMojo {

    @Parameter(defaultValue = '${repositorySystemSession}', readonly = true)
    private RepositorySystemSession repoSession

    @Parameter(defaultValue = '${project.build.directory}/accurest/mappings')
    private File stubsDirectory

    @Parameter(property = 'stubsDirectory', defaultValue = '${basedir}')
    private File destination

    @Parameter(property = 'accurest.http.port', defaultValue = '8080')
    private int httpPort;

    @Parameter(property = 'accurest.skip', defaultValue = 'false')
    private boolean skip

    @Parameter(property = 'accurest.stubs')
    private String stubs

    @Parameter(property = 'accurest.http.minPort', defaultValue = '10000')
    private int minPort

    @Parameter(property = 'accurest.http.maxPort', defaultValue = '15000')
    private int maxPort

    private String stubsClassifier = 'stubs'

    @Parameter(defaultValue = '${session}', readonly = true)
    private MavenSession mavenSession

    private final LocalStubRunner localStubRunner
    private final RemoteStubRunner remoteStubRunner

    @Inject
    RunMojo(LocalStubRunner localStubRunner, RemoteStubRunner remoteStubRunner) {
        this.localStubRunner = localStubRunner
        this.remoteStubRunner = remoteStubRunner
    }

    void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            log.info("Skipping accurest execution: accurest.skip=${skip}")
            return
        }
        BatchStubRunner batchStubRunner
        if (!stubs) {
            StubRunnerOptions options = new StubRunnerOptions(httpPort, httpPort + 1, "", false, stubsClassifier)
            StubRunner stubRunner = localStubRunner.run(resolveStubsDirectory().absolutePath, options)
            batchStubRunner = new BatchStubRunner(Arrays.asList(stubRunner))
        } else {
            EnhancedStubRunnerOptions options = new EnhancedStubRunnerOptionsBuilder(minPort, maxPort, "", false, stubsClassifier)
                    .withStubs(stubs)
                    .build()
            batchStubRunner = remoteStubRunner.run(options, repoSession)
        }

        if (!insideProject) {
            pressAnyKeyToContinue()
            if (batchStubRunner) {
                batchStubRunner.close()
            }
        }
    }

    private File resolveStubsDirectory() {
        if (insideProject) {
            stubsDirectory
        } else {
            destination
        }
    }

    private void pressAnyKeyToContinue() {
        log.info("Press ENTER to continue...")
        try {
            System.in.read()
        } catch (Exception ignored) {
        }
    }

    private boolean isInsideProject() {
        return mavenSession.request.projectPresent
    }

}
