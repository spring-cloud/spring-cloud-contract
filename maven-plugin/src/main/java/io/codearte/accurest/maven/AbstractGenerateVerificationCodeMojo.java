package io.codearte.accurest.maven;

import static java.lang.String.format;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.assertj.core.util.Strings;

import io.codearte.accurest.AccurestException;
import io.codearte.accurest.TestGenerator;
import io.codearte.accurest.config.AccurestConfigProperties;
import io.codearte.accurest.config.TestFramework;
import io.codearte.accurest.config.TestMode;

public abstract class AbstractGenerateVerificationCodeMojo extends AbstractMojo {

	@Parameter(defaultValue = "${basedir}", readonly = true, required = true)
	protected File baseDir;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
	protected File projectBuildDirectory;

	@Parameter(defaultValue = "/src/test/accurest")
	protected String contractsDir;

	@Parameter(defaultValue = "/generated-test-sources/accurest")
	protected String generatedTestSourcesDir;

	@Parameter(defaultValue = "io.codearte.accurest.tests")
	protected String basePackageForTests;

	@Parameter
	protected String baseClassForTests;

	@Parameter(defaultValue = "MOCKMVC")
	protected TestMode testMode;

	protected void generateVerificationCode(TestFramework testFramework) throws MojoExecutionException {

		AccurestConfigProperties config = new AccurestConfigProperties();
		config.setContractsDslDir(new File(baseDir, contractsDir));
		config.setBasePackageForTests(basePackageForTests);
		config.setGeneratedTestSourcesDir(new File(projectBuildDirectory, generatedTestSourcesDir));
		if (Strings.isNullOrEmpty(baseClassForTests)) {
			if (!Strings.isNullOrEmpty(basePackageForTests)) {
				baseClassForTests = basePackageForTests + '.' + "BaseAccurest";
			} else {
				baseClassForTests = "io.codearte.accurest.tests.BaseAccurest";
			}
		}
		config.setBaseClassForTests(baseClassForTests);
		config.setTargetFramework(testFramework);
		config.setTestMode(testMode);

		getLog().info(format("Using %s as test source directory", config.getGeneratedTestSourcesDir()));
		getLog().info(format("Using %s as base class for verification classes", config.getBaseClassForTests()));

		try {
			TestGenerator generator = new TestGenerator(config);
			int generatedClasses = generator.generate();
			getLog().info(format("Generated %s test classes.", generatedClasses));
		} catch (AccurestException e) {
			throw new MojoExecutionException(format("Accurest Plugin exception: %s", e.getMessage()), e);
		}
	}

}
