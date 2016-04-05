package io.codearte.accurest.maven;

import static java.lang.String.format;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.codearte.accurest.config.AccurestConfigProperties;
import io.codearte.accurest.wiremock.DslToWireMockClientConverter;
import io.codearte.accurest.wiremock.RecursiveFilesConverter;

@Mojo(name = "convert", requiresProject = false)
public class ConvertMojo extends AbstractMojo {

	@Parameter(property = "contractsDir", defaultValue = "${basedir}")
	private File contractsDir;

	@Parameter(property = "mappingsDir", defaultValue = "${basedir}")
	private File mappingsDir;

	public void execute() throws MojoExecutionException, MojoFailureException {

		AccurestConfigProperties config = new AccurestConfigProperties();
		config.setContractsDslDir(contractsDir);
		config.setStubsOutputDir(mappingsDir);

		getLog().info("Converting from accurest contracts written in GroovyDSL to WireMock stubs mappings");
		getLog().info(format("     Accurest contracts directory: %s", config.getContractsDslDir().getAbsolutePath()));
		getLog().info(format("WireMock stubs mappings directory: %s", config.getStubsOutputDir().getAbsolutePath()));

		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), config);
		converter.processFiles();
	}

}
