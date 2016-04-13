package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.wiremock.DslToWireMockClientConverter
import io.codearte.accurest.wiremock.RecursiveFilesConverter
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@Mojo(name = 'generateStubs', defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@CompileStatic
public class GenerateStubsMojo extends AbstractMojo {

    @Parameter(defaultValue = '${basedir}', readonly = true, required = true)
    private File baseDir

    @Parameter(defaultValue = '${project.build.directory}', readonly = true, required = true)
    private File projectBuildDirectory

    @Parameter(defaultValue = '/src/test/accurest')
    private String contractsDir

    @Parameter(defaultValue = 'mappings')
    private String mappingsDir

    public void execute() throws MojoExecutionException, MojoFailureException {
        AccurestConfigProperties config = new AccurestConfigProperties()
        config.contractsDslDir = new File(baseDir, contractsDir)
        config.stubsOutputDir = new File(projectBuildDirectory, mappingsDir)

        log.info('Accurest Plugin: Invoking GroovyDSL to WireMock client stubs conversion')
        log.info("From '${config.contractsDslDir}' to '${config.stubsOutputDir}'")

        RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), config)
        converter.processFiles()
    }

}
