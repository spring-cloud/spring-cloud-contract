package io.codearte.accurest.maven;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import io.codearte.accurest.config.AccurestConfigProperties;

@Mojo(name = "run", requiresProject = false)
public class RunMojo extends AbstractMojo {

	@Parameter(property = "contractsDir", defaultValue = "${basedir}")
	private File contractsDir;

	@Parameter(property = "port", defaultValue = "8089")
	private int port;

	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			Path mappingsPath = Files.createTempDirectory("mappings");
			AccurestConfigProperties config = new AccurestConfigProperties();
			config.setContractsDslDir(contractsDir);
			config.setStubsOutputDir(mappingsPath.toFile());
			new AccurestConverter().convert(config);
			getLog().info(
					format("     Accurest contracts directory: %s", config.getContractsDslDir().getAbsolutePath()));
			getLog().info(
					format("WireMock stubs mappings directory: %s", config.getStubsOutputDir().getAbsolutePath()));

			WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port));
			wireMockServer.loadMappingsUsing(
					new JsonFileMappingsLoader(new SingleRootFileSource(mappingsPath.toFile())));
			wireMockServer.start();

			pressAnyKeyToContinue();
			getLog().info("Shutting down ...");

			wireMockServer.stop();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void pressAnyKeyToContinue() {
		getLog().info("Press any key to continue...");
		try {
			System.in.read();
		} catch (Exception ignored) {
		}
	}

}
