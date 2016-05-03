package io.codearte.accurest.maven;

import groovy.transform.CompileStatic;
import io.codearte.accurest.config.AccurestConfigProperties;
import io.codearte.accurest.wiremock.DslToWireMockClientConverter;
import io.codearte.accurest.wiremock.RecursiveFilesConverter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * Convert Accurest contracts into WireMock stubs mappings.
 *
 * This goal allow to generate `stubs-jar` or execute `accurest:run` with generated WireMock mappings.
 *
 */
@Mojo(name = "convert", requiresProject = false, defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
@CompileStatic
public class ConvertMojo extends AbstractMojo {

    /**
     * Directory containing Accurest contracts written using the GroovyDSL
     */
    @Parameter(defaultValue = "${basedir}/src/test/resources/accurest")
    private File contractsDirectory;

    /**
     * Directory where the generated WireMock stubs from Groovy DSL should be placed.
     * You can then mention them in your packaging task to create jar with stubs
     */
    @Parameter(defaultValue = "${project.build.directory}/accurest")
    private File outputDirectory;

    /**
     * Directory containing contracts written using the GroovyDSL
     *
     * This parameter is only used when goal is executed outside of maven project.
     */
    @Parameter(property = "contractsDirectory", defaultValue = "${basedir}")
    private File source;

    @Parameter(property = "stubsDirectory", defaultValue = "${basedir}")
    private File destination;

    @Parameter(property = "accurest.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Component(role = MavenResourcesFiltering.class, hint = "default")
    private MavenResourcesFiltering mavenResourcesFiltering;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            getLog().info(String.format("Skipping accurest execution: accurest.skip=%s", skip));
            return;
        }

        new CopyContracts(project, mavenSession, mavenResourcesFiltering).copy(contractsDirectory, outputDirectory);

        final AccurestConfigProperties config = new AccurestConfigProperties();
        config.setContractsDslDir(isInsideProject() ? contractsDirectory : source);
        config.setStubsOutputDir(isInsideProject() ? new File(outputDirectory, "mappings") : destination);

        getLog().info("Converting from accurest contracts written in GroovyDSL to WireMock stubs mappings");
        getLog().info(String.format("     Accurest contracts directory: %s", config.getContractsDslDir()));
        getLog().info(String.format("WireMock stubs mappings directory: %s", config.getStubsOutputDir()));

        RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), config);
        converter.processFiles();
    }

    private boolean isInsideProject() {
        return mavenSession.getRequest().isProjectPresent();
    }


}
