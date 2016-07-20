/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.springframework.cloud.contract.maven.verifier;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.cloud.contract.spec.ContractVerifierException;
import org.springframework.cloud.contract.verifier.TestGenerator;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.config.TestMode;

@Mojo(name = "generateTests", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
		requiresDependencyResolution = ResolutionScope.TEST)
public class GenerateTestsMojo extends AbstractMojo {

	@Parameter(property = "spring.cloud.contract.verifier.contractsDirectory",
			defaultValue = "${project.basedir}/src/test/resources/contracts")
	private File contractsDirectory;

	@Parameter(
			defaultValue = "${project.build.directory}/generated-test-sources/contracts")
	private File generatedTestSourcesDir;

	@Parameter(defaultValue = "org.springframework.cloud.contract.verifier.tests")
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

	/**
	 * Imports that should be added to generated tests
	 */
	@Parameter
	private String[] imports;

	/**
	 * Static imports that should be added to generated tests
	 */
	@Parameter
	private String[] staticImports;

	/**
	 * Patterns that should not be taken into account for processing
	 */
	@Parameter
	private List<String> excludedFiles;

	/**
	 * Patterns for which Spring Cloud Contract Verifier should generate @Ignored tests
	 */
	@Parameter
	private List<String> ignoredFiles;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(property = "spring.cloud.contract.verifier.skip", defaultValue = "false")
	private boolean skip;

	@Parameter(property = "maven.test.skip", defaultValue = "false")
	private boolean mavenTestSkip;

	@Parameter(property = "skipTests", defaultValue = "false") private boolean skipTests;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip || mavenTestSkip || skipTests) {
			if (skip) getLog().info("Skipping Spring Cloud Contract Verifier execution: spring.cloud.contract.verifier.skip=" + skip);
			if (mavenTestSkip) getLog().info("Skipping Spring Cloud Contract Verifier execution: maven.test.skip=" + mavenTestSkip);
			if (skipTests) getLog().info("Skipping Spring Cloud Contract Verifier execution: skipTests" + skipTests);
			return;
		}
		getLog().info(
				"Generating server tests source code for Spring Cloud Contract Verifier contract verification");
		final ContractVerifierConfigProperties config = new ContractVerifierConfigProperties();
		config.setContractsDslDir(contractsDirectory);
		config.setGeneratedTestSourcesDir(generatedTestSourcesDir);
		config.setTargetFramework(testFramework);
		config.setTestMode(testMode);
		config.setBasePackageForTests(basePackageForTests);
		config.setBaseClassForTests(baseClassForTests);
		config.setRuleClassForTests(ruleClassForTests);
		config.setNameSuffixForTests(nameSuffixForTests);
		config.setImports(imports);
		config.setStaticImports(staticImports);
		config.setIgnoredFiles(ignoredFiles);
		config.setExcludedFiles(excludedFiles);
		project.addTestCompileSourceRoot(generatedTestSourcesDir.getAbsolutePath());
		if (getLog().isInfoEnabled()) {
			getLog().info(
					"Test Source directory: " + generatedTestSourcesDir.getAbsolutePath()
							+ " added.");
			getLog().info("Using " + config.getBaseClassForTests()
					+ " as base class for test classes");
		}
		try {
			TestGenerator generator = new TestGenerator(config);
			int generatedClasses = generator.generate();
			getLog().info("Generated " + generatedClasses + " test classes.");
		}
		catch (ContractVerifierException e) {
			throw new MojoExecutionException(
					String.format("Spring Cloud Contract Verifier Plugin exception: %s",
							e.getMessage()), e);
		}
	}

	public List<String> getExcludedFiles() {
		return excludedFiles;
	}

	public void setExcludedFiles(List<String> excludedFiles) {
		this.excludedFiles = excludedFiles;
	}

	public List<String> getIgnoredFiles() {
		return ignoredFiles;
	}

	public void setIgnoredFiles(List<String> ignoredFiles) {
		this.ignoredFiles = ignoredFiles;
	}

}
