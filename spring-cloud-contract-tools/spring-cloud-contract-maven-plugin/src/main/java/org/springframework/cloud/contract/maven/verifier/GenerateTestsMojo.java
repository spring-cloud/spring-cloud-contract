/*
 *  Copyright 2013-2017 the original author or authors.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.springframework.cloud.contract.maven.verifier.stubrunner.AetherStubDownloaderFactory;
import org.springframework.cloud.contract.spec.ContractVerifierException;
import org.springframework.cloud.contract.verifier.TestGenerator;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.config.TestMode;

/**
 * From the provided directory with contracts generates the acceptance
 * tests on the producer side
 */
@Mojo(name = "generateTests", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
		requiresDependencyResolution = ResolutionScope.TEST)
public class GenerateTestsMojo extends AbstractMojo {

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;

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
	 * Incubating feature. You can check the size of JSON arrays. If not turned on
	 * explicitly will be disabled.
	 */
	@Parameter(property = "spring.cloud.contract.verifier.assert.size",
			defaultValue = "false")
	private boolean assertJsonSize;

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

	/**
	 * The URL from which a JAR containing the contracts should get downloaded. If not provided
	 * but artifactid / coordinates notation was provided then the current Maven's build repositories will be
	 * taken into consideration
	 */
	@Parameter(property = "contractsRepositoryUrl")
	private String contractsRepositoryUrl;

	@Parameter(property = "contractDependency")
	private Dependency contractDependency;

	/**
	 * The path in the JAR with all the contracts where contracts for this particular service lay.
	 * If not provided will be resolved to {@code groupid/artifactid}. Example:
	 * </p>
	 * If {@code groupid} is {@code com.example} and {@code artifactid} is {@code service} then the resolved path will be
	 * {@code /com/example/artifactid}
	 */
	@Parameter(property = "contractsPath")
	private String contractsPath;

	/**
	 * If {@code true} then JAR with contracts will be taken from local maven repository
	 */
	@Parameter(property = "contractsWorkOffline", defaultValue = "false")
	private boolean contractsWorkOffline;

	/**
	 * A package that contains all the base clases for generated tests. If your contract resides in a location
	 * {@code src/test/resources/contracts/com/example/v1/} and you provide the {@code packageWithBaseClasses}
	 * value to {@code com.example.contracts.base} then we will search for a test source file that will
	 * have the package {@code com.example.contracts.base} and name {@code ExampleV1Base}. As you can see
	 * it will take the two last folders to and attach {@code Base} to its name.
	 */
	@Parameter(property = "packageWithBaseClasses")
	private String packageWithBaseClasses;

	/**
	 * A way to override any base class mappings. The keys are regular expressions on the package name of the contract
	 * and the values FQN to a base class for that given expression.
	 * </p>
	 * Example of a mapping
	 * </p>
	 * {@code .*.com.example.v1..*} -> {@code com.example.SomeBaseClass}
	 * </p>
	 * When a contract's package matches the provided regular expression then extending class will be the one
	 * provided in the map - in this case {@code com.example.SomeBaseClass}
	 */
	@Parameter(property = "baseClassMappings")
	private List<BaseClassMapping> baseClassMappings;

	private final AetherStubDownloaderFactory aetherStubDownloaderFactory;

	@Inject
	public GenerateTestsMojo(AetherStubDownloaderFactory aetherStubDownloaderFactory) {
		this.aetherStubDownloaderFactory = aetherStubDownloaderFactory;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.skip || this.mavenTestSkip || this.skipTests) {
			if (this.skip) getLog().info("Skipping Spring Cloud Contract Verifier execution: spring.cloud.contract.verifier.skip=" + this.skip);
			if (this.mavenTestSkip) getLog().info("Skipping Spring Cloud Contract Verifier execution: maven.test.skip=" + this.mavenTestSkip);
			if (this.skipTests) getLog().info("Skipping Spring Cloud Contract Verifier execution: skipTests" + this.skipTests);
			return;
		}
		getLog().info(
				"Generating server tests source code for Spring Cloud Contract Verifier contract verification");
		final ContractVerifierConfigProperties config = new ContractVerifierConfigProperties();
		// download contracts, unzip them and pass as output directory
		File contractsDirectory = new MavenContractsDownloader(this.project, this.contractDependency,
				this.contractsPath, this.contractsRepositoryUrl, this.contractsWorkOffline, getLog(),
				this.aetherStubDownloaderFactory, this.repoSession).downloadAndUnpackContractsIfRequired(config, this.contractsDirectory);
		getLog().info("Directory with contract is present at [" + contractsDirectory + "]");
		config.setContractsDslDir(contractsDirectory);
		config.setGeneratedTestSourcesDir(this.generatedTestSourcesDir);
		config.setTargetFramework(this.testFramework);
		config.setTestMode(this.testMode);
		config.setBasePackageForTests(this.basePackageForTests);
		config.setBaseClassForTests(this.baseClassForTests);
		config.setRuleClassForTests(this.ruleClassForTests);
		config.setNameSuffixForTests(this.nameSuffixForTests);
		config.setImports(this.imports);
		config.setStaticImports(this.staticImports);
		config.setIgnoredFiles(this.ignoredFiles);
		config.setExcludedFiles(this.excludedFiles);
		config.setAssertJsonSize(this.assertJsonSize);
		config.setPackageWithBaseClasses(this.packageWithBaseClasses);
		if (this.baseClassMappings != null) {
			config.setBaseClassMappings(mappingsToMap());
		}
		this.project.addTestCompileSourceRoot(this.generatedTestSourcesDir.getAbsolutePath());
		if (getLog().isInfoEnabled()) {
			getLog().info(
					"Test Source directory: " + this.generatedTestSourcesDir.getAbsolutePath()
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

	public Map<String, String> mappingsToMap() {
		Map<String, String> map = new HashMap<>();
		if (this.baseClassMappings == null) {
			return map;
		}
		for (BaseClassMapping mapping : this.baseClassMappings) {
			map.put(mapping.getContractPackageRegex(), mapping.getBaseClassFQN());
		}
		return map;
	}

	public List<String> getExcludedFiles() {
		return this.excludedFiles;
	}

	public void setExcludedFiles(List<String> excludedFiles) {
		this.excludedFiles = excludedFiles;
	}

	public List<String> getIgnoredFiles() {
		return this.ignoredFiles;
	}

	public void setIgnoredFiles(List<String> ignoredFiles) {
		this.ignoredFiles = ignoredFiles;
	}

	public boolean isAssertJsonSize() {
		return this.assertJsonSize;
	}

	public void setAssertJsonSize(boolean assertJsonSize) {
		this.assertJsonSize = assertJsonSize;
	}
}
