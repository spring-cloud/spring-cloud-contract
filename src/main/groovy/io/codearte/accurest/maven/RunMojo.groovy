package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.maven.stubrunner.LocalStubRunner
import io.codearte.accurest.maven.stubrunner.RemoteStubRunner
import io.codearte.accurest.stubrunner.StubRunnerOptions
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.path.PathTranslator
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.eclipse.aether.RepositorySystemSession

import javax.inject.Inject

@Mojo(name = 'run', requiresProject = false)
@CompileStatic
class RunMojo extends AbstractMojo {

    @Parameter(defaultValue = '${basedir}', readonly = true, required = true)
    private File basedir

    @Parameter(defaultValue = '${project.build.directory}', readonly = true, required = true)
    private File projectBuildDirectory

    @Parameter(defaultValue = '${repositorySystemSession}', readonly = true)
    private RepositorySystemSession repoSession

    @Parameter(property = 'stubsDirectory', defaultValue = '${basedir}')
    private String stubsDirectory

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

    @Component
    private MavenSession mavenSession

    private final LocalStubRunner localStubRunner
    private final RemoteStubRunner remoteStubRunner
    private final PathTranslator translator

    @Inject
    RunMojo(LocalStubRunner localStubRunner, RemoteStubRunner remoteStubRunner, PathTranslator translator) {
        this.localStubRunner = localStubRunner
        this.remoteStubRunner = remoteStubRunner
        this.translator = translator
    }

    void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            log.info("Skipping accurest execution: accurest.skip=${skip}")
            return
        }

        if (!stubs) {
            StubRunnerOptions options = new StubRunnerOptions(httpPort, httpPort + 1, "", false, stubsClassifier)
            localStubRunner.run(resolveStubsDirectory().absolutePath, options)
        } else {
            StubRunnerOptions options = new StubRunnerOptions(minPort, maxPort, "", false, stubsClassifier)
            remoteStubRunner.run(stubs, options, repoSession)
        }

        if (!insideProject) {
            pressAnyKeyToContinue()
        }
    }

    private File resolveStubsDirectory() {
        if (insideProject) {
            return resolveFile(projectBuildDirectory, stubsDirectory, 'mappings')
        } else {
            return resolveFile(basedir, stubsDirectory, '')
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

    private File resolveFile(File baseDir, String requestedPath, String defaultPath) {
        return requestedPath ? alignToBaseDirectory(baseDir, requestedPath) : alignToBaseDirectory(baseDir, defaultPath)
    }

    private File alignToBaseDirectory(File baseDir, String path) {
        return new File(translator.alignToBaseDirectory(path, baseDir))
    }


}
