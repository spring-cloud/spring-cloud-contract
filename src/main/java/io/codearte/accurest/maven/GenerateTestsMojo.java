package io.codearte.accurest.maven;

import static java.lang.String.format;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.codearte.accurest.AccurestException;
import io.codearte.accurest.TestGenerator;
import io.codearte.accurest.config.AccurestConfigProperties;
import io.codearte.accurest.config.TestFramework;
import io.codearte.accurest.config.TestMode;

@Mojo(name = "generateTests", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateTestsMojo extends AbstractMojo {

	@Parameter(defaultValue = "${basedir}", readonly = true, required = true)
	private File baseDir;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
	private File projectBuildDirectory;

	@Parameter(defaultValue = "/src/test/accurest")
	private String contractsDir;

	@Parameter(defaultValue = "/generated-test-sources/accurest")
	private String generatedTestSourcesDir;

	@Parameter(defaultValue = "io.codearte.accurest.tests")
	private String basePackageForTests;

	@Parameter
	private String baseClassForTests;

	@Parameter(defaultValue = "MOCKMVC")
	private TestMode testMode;

	@Parameter(defaultValue = "JUNIT")
	private TestFramework testFramework;

	@Parameter
	private String ruleClassForTests;

	@Parameter
	private String nameSuffixForTests;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Generating server tests source code for Accurest contract verification");

		AccurestConfigProperties config = new AccurestConfigProperties();
		config.setContractsDslDir(new File(baseDir, contractsDir));
		config.setGeneratedTestSourcesDir(new File(projectBuildDirectory, generatedTestSourcesDir));
		config.setTargetFramework(testFramework);
		config.setTestMode(testMode);
		config.setBasePackageForTests(basePackageForTests);
		config.setBaseClassForTests(baseClassForTests);
		config.setRuleClassForTests(ruleClassForTests);
		config.setNameSuffixForTests(nameSuffixForTests);

		getLog().info(format("Using %s as test source directory", config.getGeneratedTestSourcesDir()));
		getLog().info(format("Using %s as base class for test classes", config.getBaseClassForTests()));

		try {
			TestGenerator generator = new TestGenerator(config);
			int generatedClasses = generator.generate();
			getLog().info(format("Generated %s test classes.", generatedClasses));
		} catch (AccurestException e) {
			throw new MojoExecutionException(format("Accurest Plugin exception: %s", e.getMessage()), e);
		}
	}

}
