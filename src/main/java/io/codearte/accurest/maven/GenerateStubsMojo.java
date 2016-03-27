package io.codearte.accurest.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.codearte.accurest.config.AccurestConfigProperties;
import io.codearte.accurest.wiremock.DslToWireMockClientConverter;
import io.codearte.accurest.wiremock.RecursiveFilesConverter;

@Mojo(name = "generateStubs", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class GenerateStubsMojo extends AbstractMojo {

	@Parameter(defaultValue = "${basedir}")
	private File baseDir;

	@Parameter(defaultValue = "${project.build.directory}")
	private File projectBuildDirectory;

	@Parameter(property = "mappingsDir", defaultValue = "mappings", required = false)
	private String mappingsDir;

	@Parameter(property = "contractsDir", defaultValue = "/src/test/resources/stubs", required = false)
	private String contractsDir;

	public void execute() throws MojoExecutionException, MojoFailureException {
		AccurestConfigProperties config = new AccurestConfigProperties();
		config.setContractsDslDir(new File(baseDir, contractsDir));
		config.setStubsOutputDir(new File(projectBuildDirectory, mappingsDir));

		getLog().info("Accurest Plugin: Invoking GroovyDSL to WireMock client stubs conversion");
		getLog().info(String.format("From '%s' to '%s'", config.getContractsDslDir(), config.getStubsOutputDir()));

		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), config);
		converter.processFiles();
	}

}
