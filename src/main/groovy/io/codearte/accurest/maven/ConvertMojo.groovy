package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.wiremock.DslToWireMockClientConverter
import io.codearte.accurest.wiremock.RecursiveFilesConverter
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.path.PathTranslator
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

import javax.inject.Inject

@Mojo(name = 'convert', requiresProject = false, defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@CompileStatic
class ConvertMojo extends AbstractMojo {

    @Parameter(defaultValue = '${basedir}', readonly = true, required = true)
    private File baseDir

    @Parameter(defaultValue = '${project.build.directory}', readonly = true, required = true)
    private File projectBuildDirectory

    @Parameter(property = 'contractsDir')
    private String contractsDir

    @Parameter(property = 'mappingsDir')
    private String mappingsDir

    @Component
    private MavenSession mavenSession

    private final PathTranslator translator

    @Inject
    ConvertMojo(PathTranslator translator) {
        this.translator = translator
    }

    void execute() throws MojoExecutionException, MojoFailureException {

        AccurestConfigProperties config = new AccurestConfigProperties()

        if (insideProject) {
            config.contractsDslDir = resolveFile(baseDir, contractsDir, 'src/test/accurest')
            config.stubsOutputDir = resolveFile(projectBuildDirectory, mappingsDir, 'mappings')
        } else {
            config.contractsDslDir = resolveFile(baseDir, contractsDir, '')
            config.stubsOutputDir = resolveFile(baseDir, mappingsDir, '')
        }

        log.info('Converting from accurest contracts written in GroovyDSL to WireMock stubs mappings')
        log.info("     Accurest contracts directory: ${config.contractsDslDir}")
        log.info("WireMock stubs mappings directory: ${config.stubsOutputDir}")

        RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), config)
        converter.processFiles()
    }

    private boolean isInsideProject() {
        return mavenSession.getRequest().isProjectPresent()
    }

    private File resolveFile(File baseDir, String requestedPath, String defaultPath) {
        return requestedPath ? alignToBaseDirectory(baseDir, requestedPath) : alignToBaseDirectory(baseDir, defaultPath)
    }

    private File alignToBaseDirectory(File baseDir, String path) {
        return new File(translator.alignToBaseDirectory(path, baseDir))
    }

}
