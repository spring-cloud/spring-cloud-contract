package io.codearte.accurest.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import io.codearte.accurest.config.TestFramework;

@Mojo(name = "generateTests", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateTestsMojo extends AbstractGenerateVerificationCodeMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Generating JUnit Tests source code for Accurest contract verification");
		generateVerificationCode(TestFramework.JUNIT);
	}

}
