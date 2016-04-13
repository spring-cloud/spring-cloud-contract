package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.config.AccurestConfigProperties
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

import static io.codearte.accurest.maven.AccurestConverter.convertAccurestToStubs

@Mojo(name = 'convert', requiresProject = false)
@CompileStatic
public class ConvertMojo extends AbstractMojo {

    @Parameter(property = 'contractsDir', defaultValue = '${basedir}')
    private File contractsDir

    @Parameter(property = 'mappingsDir', defaultValue = '${basedir}')
    private File mappingsDir

    public void execute() throws MojoExecutionException, MojoFailureException {

        AccurestConfigProperties config = new AccurestConfigProperties()
        config.contractsDslDir = contractsDir
        config.stubsOutputDir = mappingsDir

        log.info('Converting from accurest contracts written in GroovyDSL to WireMock stubs mappings')
        log.info("     Accurest contracts directory: ${config.contractsDslDir.absolutePath}")
        log.info("WireMock stubs mappings directory: ${config.stubsOutputDir.absolutePath}")

        convertAccurestToStubs(config)
    }

}
