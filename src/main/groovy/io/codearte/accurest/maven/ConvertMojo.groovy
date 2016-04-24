package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.wiremock.DslToWireMockClientConverter
import io.codearte.accurest.wiremock.RecursiveFilesConverter
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Resource
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.apache.maven.shared.filtering.MavenFilteringException
import org.apache.maven.shared.filtering.MavenResourcesExecution
import org.apache.maven.shared.filtering.MavenResourcesFiltering

@Mojo(name = 'convert', requiresProject = false, defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
@CompileStatic
class ConvertMojo extends AbstractMojo {

    @Parameter(defaultValue = '${basedir}/src/test/accurest')
    private File contractsDirectory

    @Parameter(defaultValue = '${project.build.directory}/accurest')
    private File outputDirectory

    @Parameter(property = 'contractsDirectory', defaultValue = '${basedir}')
    private File source

    @Parameter(property = 'stubsDirectory', defaultValue = '${basedir}')
    private File destination

    @Parameter(property = 'accurest.skip', defaultValue = 'false')
    private boolean skip

    @Parameter(defaultValue = '${session}', readonly = true)
    private MavenSession mavenSession

    @Parameter(defaultValue = '${project}', readonly = true)
    private MavenProject project

    @Component(role = MavenResourcesFiltering.class, hint = "default")
    private MavenResourcesFiltering mavenResourcesFiltering;

    void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            log.info("Skipping accurest execution: accurest.skip=${skip}")
            return
        }

        copyContracts()

        AccurestConfigProperties config = new AccurestConfigProperties()
        config.contractsDslDir = insideProject ? contractsDirectory : source
        config.stubsOutputDir = insideProject ? new File(outputDirectory, 'mappings') : destination

        log.info('Converting from accurest contracts written in GroovyDSL to WireMock stubs mappings')
        log.info("     Accurest contracts directory: ${config.contractsDslDir}")
        log.info("WireMock stubs mappings directory: ${config.stubsOutputDir}")

        RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), config)
        converter.processFiles()
    }

    private void copyContracts() {
        log.info("Copying accurest contracts")
        Resource testResource = new Resource(directory: contractsDirectory.absolutePath)
        MavenResourcesExecution mavenResourcesExecution =
                new MavenResourcesExecution([testResource],
                        new File(outputDirectory, 'contracts'), project, 'UTF-8',
                        [], [], mavenSession);
        mavenResourcesExecution.injectProjectBuildFilters = false
        mavenResourcesExecution.overwrite = true
        mavenResourcesExecution.includeEmptyDirs = false
        mavenResourcesExecution.filterFilenames = false
        try {
            mavenResourcesFiltering.filterResources(mavenResourcesExecution);
        } catch (MavenFilteringException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private boolean isInsideProject() {
        return mavenSession.request.projectPresent
    }


}
