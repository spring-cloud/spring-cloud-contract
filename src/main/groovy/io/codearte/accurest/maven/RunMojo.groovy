package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.maven.stubrunner.LocalStubRunner
import io.codearte.accurest.maven.stubrunner.RemoteStubRunner
import io.codearte.accurest.stubrunner.StubRunnerOptions
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.eclipse.aether.RepositorySystemSession

import javax.inject.Inject

@Mojo(name = 'run', requiresProject = false)
@CompileStatic
class RunMojo extends AbstractMojo {

    @Parameter(defaultValue = '${repositorySystemSession}', readonly = true)
    private RepositorySystemSession repoSession

    @Parameter(property = 'contractsDir', defaultValue = '${basedir}')
    private File contractsDir

    @Parameter(property = 'stubs')
    private String stubs

    @Parameter(property = 'minPort', defaultValue = '10000')
    private int minPort

    @Parameter(property = 'maxPort', defaultValue = '15000')
    private int maxPort

    private String stubsClassifier = 'stubs'

    private final LocalStubRunner localStubRunner
    private final RemoteStubRunner remoteStubRunner

    @Inject
    RunMojo(LocalStubRunner localStubRunner, RemoteStubRunner remoteStubRunner) {
        this.localStubRunner = localStubRunner
        this.remoteStubRunner = remoteStubRunner
    }

    void execute() throws MojoExecutionException, MojoFailureException {
        StubRunnerOptions options = new StubRunnerOptions(minPort, maxPort, "", false, stubsClassifier)
        log.debug("Launching StubRunner with args: $options")
        if (!stubs) {
            localStubRunner.run(contractsDir.getAbsolutePath(), options)
        } else {
            remoteStubRunner.run(stubs, options, repoSession)
        }
        pressAnyKeyToContinue()
    }

    private void pressAnyKeyToContinue() {
        log.info("Press ENTER to continue...")
        try {
            System.in.read()
        } catch (Exception ignored) {
        }
    }

}
