/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents Contract Verifier configuration properties.
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Tim Ysewyn
 * @since 1.0.0
 */
public class ContractVerifierConfigProperties {

	private static final Log log = LogFactory
			.getLog(ContractVerifierConfigProperties.class);

	/**
	 * For which unit test library tests should be generated.
	 * @deprecated - use {@code testFramework}
	 */
	@Deprecated
	private TestFramework targetFramework;

	/**
	 * For which unit test library tests should be generated.
	 */
	private TestFramework testFramework = TestFramework.JUNIT;

	/**
	 * Which mechanism should be used to invoke REST calls during. tests
	 */
	private TestMode testMode = TestMode.MOCKMVC;

	/**
	 * In which language should the tests be generated.
	 */
	private TestLanguage testLanguage = TestLanguage.JAVA;

	/**
	 * Base package for generated tests.
	 */
	private String basePackageForTests;

	/**
	 * Class which all generated tests should extend.
	 */
	private String baseClassForTests;

	/**
	 * Suffix for generated test classes, like Spec or Test.
	 */
	private String nameSuffixForTests;

	/**
	 * Rule class that should be added to generated tests.
	 */
	private String ruleClassForTests;

	/**
	 * Patterns that should not be taken into account for processing.
	 */
	private List<String> excludedFiles = new ArrayList<>();

	/**
	 * Patterns that should be taken into account for processing.
	 */
	private List<String> includedFiles = new ArrayList<String>();

	/**
	 * Patterns for which generated tests should be @Ignored.
	 */
	private List<String> ignoredFiles = new ArrayList<String>();

	/**
	 * Imports that should be added to generated tests.
	 */
	private String[] imports = new String[0];

	/**
	 * Static imports that should be added to generated tests.
	 */
	private String[] staticImports = new String[0];

	/**
	 * Directory containing contracts.
	 */
	private File contractsDslDir;

	/**
	 * Test source directory where tests generated.
	 */
	private File generatedTestSourcesDir;

	/**
	 * Test resource directory where additional resources for tests will be set.
	 */
	private File generatedTestResourcesDir;

	/**
	 * Dir where the generated stubs from the contracts should be placed. You can then
	 * mention them in your packaging task to create jar with stubs.
	 */
	private File stubsOutputDir;

	/**
	 * Suffix for the generated Stubs Jar task.
	 */
	private String stubsSuffix = "stubs";

	/**
	 * Incubating feature. You can check the size of JSON arrays. If not turned on
	 * explicitly will be disabled.
	 */
	private Boolean assertJsonSize = false;

	/**
	 * A regular expression that matches contracts. Especially useful when using a single
	 * JAR containing all the contracts in the system. In this case you'd like to take
	 * into consideration only some of them. Defaults to picking all files.
	 */
	private String includedContracts = ".*";

	/**
	 * A ant pattern to match files. Gets updated when using repo with common contracts to
	 * reflect the path to proper folder with contracts.
	 */
	private String includedRootFolderAntPattern = "**/";

	/**
	 * A package that contains all the base clases for generated tests. If your contract
	 * resides in a location {@code src/test/resources/contracts/com/example/v1/} and you
	 * provide the {@code packageWithBaseClasses} value to
	 * {@code com.example.contracts.base} then we will search for a test source file that
	 * will have the package {@code com.example.contracts.base} and name
	 * {@code ExampleV1Base}. As you can see it will take the two last folders to and
	 * attach {@code Base} to its name.
	 */
	private String packageWithBaseClasses;

	/**
	 * A way to override any base class mappings. The keys are regular expressions on the
	 * package name of the contract and the values FQN to a base class for that given
	 * expression.
	 * </p>
	 * Example of a mapping
	 * </p>
	 * {@code .*.com.example.v1..*} -> {@code com.example.SomeBaseClass}
	 * </p>
	 * When a contract's package matches the provided regular expression then extending
	 * class will be the one provided in the map - in this case
	 * {@code com.example.SomeBaseClass}
	 */
	private Map<String, String> baseClassMappings;

	/**
	 * If set to true then the {@code target} or {@code build} folders are getting
	 * excluded from any operations. This is used out of the box when working with common
	 * repo with contracts.
	 */
	private boolean excludeBuildFolders;

	/**
	 * If set to true then if any contracts that are in progress are found, will break the
	 * build. On the producer side you need to be explicit about the fact that you have
	 * contracts in progress and take into consideration that you might be causing false
	 * positive test execution results on the consumer side.
	 */
	private boolean failOnInProgress = true;

	@Deprecated
	public void setTargetFramework(TestFramework targetFramework) {
		log.warn("Please use the [testFramework] field. [targetFramework] is deprecated");
		setTestFramework(targetFramework);
	}

	@Deprecated
	public TestFramework getTargetFramework() {
		return getTestFramework();
	}

	public TestFramework getTestFramework() {
		return testFramework;
	}

	public void setTestFramework(TestFramework testFramework) {
		this.testFramework = testFramework;
	}

	public TestMode getTestMode() {
		return testMode;
	}

	public void setTestMode(TestMode testMode) {
		this.testMode = testMode;
	}

	public TestLanguage getTestLanguage() {
		return testLanguage;
	}

	public void setTestLanguage(TestLanguage testLanguage) {
		this.testLanguage = testLanguage;
	}

	public String getBasePackageForTests() {
		return basePackageForTests;
	}

	public void setBasePackageForTests(String basePackageForTests) {
		this.basePackageForTests = basePackageForTests;
	}

	public String getBaseClassForTests() {
		return baseClassForTests;
	}

	public void setBaseClassForTests(String baseClassForTests) {
		this.baseClassForTests = baseClassForTests;
	}

	public String getNameSuffixForTests() {
		return nameSuffixForTests;
	}

	public void setNameSuffixForTests(String nameSuffixForTests) {
		this.nameSuffixForTests = nameSuffixForTests;
	}

	public String getRuleClassForTests() {
		return ruleClassForTests;
	}

	public void setRuleClassForTests(String ruleClassForTests) {
		this.ruleClassForTests = ruleClassForTests;
	}

	public List<String> getExcludedFiles() {
		return excludedFiles;
	}

	public void setExcludedFiles(List<String> excludedFiles) {
		this.excludedFiles = excludedFiles;
	}

	public List<String> getIncludedFiles() {
		return includedFiles;
	}

	public void setIncludedFiles(List<String> includedFiles) {
		this.includedFiles = includedFiles;
	}

	public List<String> getIgnoredFiles() {
		return ignoredFiles;
	}

	public void setIgnoredFiles(List<String> ignoredFiles) {
		this.ignoredFiles = ignoredFiles;
	}

	public String[] getImports() {
		return imports;
	}

	public void setImports(String[] imports) {
		this.imports = imports;
	}

	public String[] getStaticImports() {
		return staticImports;
	}

	public void setStaticImports(String[] staticImports) {
		this.staticImports = staticImports;
	}

	public File getContractsDslDir() {
		return contractsDslDir;
	}

	public void setContractsDslDir(File contractsDslDir) {
		this.contractsDslDir = contractsDslDir;
	}

	public File getGeneratedTestSourcesDir() {
		return generatedTestSourcesDir;
	}

	public void setGeneratedTestSourcesDir(File generatedTestSourcesDir) {
		this.generatedTestSourcesDir = generatedTestSourcesDir;
	}

	public File getGeneratedTestResourcesDir() {
		return generatedTestResourcesDir;
	}

	public void setGeneratedTestResourcesDir(File generatedTestResourcesDir) {
		this.generatedTestResourcesDir = generatedTestResourcesDir;
	}

	public File getStubsOutputDir() {
		return stubsOutputDir;
	}

	public void setStubsOutputDir(File stubsOutputDir) {
		this.stubsOutputDir = stubsOutputDir;
	}

	public String getStubsSuffix() {
		return stubsSuffix;
	}

	public void setStubsSuffix(String stubsSuffix) {
		this.stubsSuffix = stubsSuffix;
	}

	public Boolean getAssertJsonSize() {
		return assertJsonSize;
	}

	public void setAssertJsonSize(Boolean assertJsonSize) {
		this.assertJsonSize = assertJsonSize;
	}

	public String getIncludedContracts() {
		return includedContracts;
	}

	public void setIncludedContracts(String includedContracts) {
		this.includedContracts = includedContracts;
	}

	public String getIncludedRootFolderAntPattern() {
		return includedRootFolderAntPattern;
	}

	public void setIncludedRootFolderAntPattern(String includedRootFolderAntPattern) {
		this.includedRootFolderAntPattern = includedRootFolderAntPattern;
	}

	public String getPackageWithBaseClasses() {
		return packageWithBaseClasses;
	}

	public void setPackageWithBaseClasses(String packageWithBaseClasses) {
		this.packageWithBaseClasses = packageWithBaseClasses;
	}

	public Map<String, String> getBaseClassMappings() {
		return baseClassMappings;
	}

	public void setBaseClassMappings(Map<String, String> baseClassMappings) {
		this.baseClassMappings = baseClassMappings;
	}

	public boolean getExcludeBuildFolders() {
		return excludeBuildFolders;
	}

	public boolean isExcludeBuildFolders() {
		return excludeBuildFolders;
	}

	public void setExcludeBuildFolders(boolean excludeBuildFolders) {
		this.excludeBuildFolders = excludeBuildFolders;
	}

	public boolean isFailOnInProgress() {
		return this.failOnInProgress;
	}

	public void setFailOnInProgress(boolean failOnInProgress) {
		this.failOnInProgress = failOnInProgress;
	}

}
