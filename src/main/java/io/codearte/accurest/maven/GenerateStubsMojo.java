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
	protected File baseDir;

	@Parameter(defaultValue = "${project.build.directory}")
	protected File projectBuildDirectory;

	@Parameter(property = "mappingsDir", defaultValue = "mappings", required = false)
	private String mappingsDir = "mappings";

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("");
		getLog().info("*** Accurest Maven Plugin ***");
		getLog().info("");

		File stubsOutputDir = new File(projectBuildDirectory, mappingsDir);

		getLog().info("baseDir = " + baseDir);
		getLog().info("projectBuildDirectory = " + projectBuildDirectory);
		getLog().info("mappings = " + mappingsDir);

		AccurestConfigProperties configProperties = new AccurestConfigProperties();

		File contractsDslDir = new File(baseDir, "/src/test/resources/stubs");

		configProperties.setContractsDslDir(contractsDslDir);
		configProperties.setBasePackageForTests("io.codearte.accurest.tests");
		configProperties.setStubsOutputDir(stubsOutputDir);
		getLog().info("Accurest Plugin: Invoking GroovyDSL to WireMock client stubs conversion");
		getLog().info(String.format("From '%s' to '%s'", contractsDslDir, stubsOutputDir));

		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(),
				configProperties);
		converter.processFiles();

	}

}
