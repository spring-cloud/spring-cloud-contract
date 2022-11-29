/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.plugin;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.config.TestMode;
import org.springframework.util.Assert;

/**
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Shannon Pamperl
 */
public class ContractVerifierExtension implements Serializable {

	/**
	 * For which unit test library tests should be generated
	 */
	private final Property<TestFramework> testFramework;

	/**
	 * Which mechanism should be used to invoke REST calls during tests
	 */
	private final Property<TestMode> testMode;

	/**
	 * Base package for generated tests
	 */
	private final Property<String> basePackageForTests;

	/**
	 * Class which all generated tests should extend
	 */
	private final Property<String> baseClassForTests;

	/**
	 * Suffix for generated test classes, like Spec or Test
	 */
	private final Property<String> nameSuffixForTests;

	/**
	 * Patterns that should not be taken into account for processing
	 */
	private final ListProperty<String> excludedFiles;

	/**
	 * Patterns that should be taken into account for processing
	 */
	private final ListProperty<String> includedFiles;

	/**
	 * Patterns for which generated tests should be @Ignored
	 */
	private final ListProperty<String> ignoredFiles;

	/**
	 * Imports that should be added to generated tests
	 */
	private final ListProperty<String> imports;

	/**
	 * Static imports that should be added to generated tests
	 */
	private final ListProperty<String> staticImports;

	/**
	 * Directory containing contracts written using the GroovyDSL
	 */
	private final DirectoryProperty contractsDslDir;

	/**
	 * Java test source directory where tests generated from Contract DSL should be placed
	 */
	private final DirectoryProperty generatedTestJavaSourcesDir;

	/**
	 * Groovy test source directory where tests generated from Contract DSL should be
	 * placed
	 */
	private final DirectoryProperty generatedTestGroovySourcesDir;

	/**
	 * Test resource directory where tests generated from Groovy DSL should be referenced
	 */
	private final DirectoryProperty generatedTestResourcesDir;

	/**
	 * Dir where the generated stubs from Groovy DSL should be placed. You can then
	 * mention them in your packaging task to create jar with stubs
	 */
	private final DirectoryProperty stubsOutputDir;

	/**
	 * Suffix for the generated Stubs Jar task
	 */
	private final Property<String> stubsSuffix;

	/**
	 * Incubating feature. You can check the size of JSON arrays. If not turned on
	 * explicitly will be disabled.
	 */
	private final Property<Boolean> assertJsonSize;

	/**
	 * When enabled, this flag will tell stub runner to throw an exception when no stubs /
	 * contracts were found.
	 */
	private final Property<Boolean> failOnNoContracts;

	/**
	 * If set to true then if any contracts that are in progress are found, will break the
	 * build. On the producer side you need to be explicit about the fact that you have
	 * contracts in progress and take into consideration that you might be causing false
	 * positive test execution results on the consumer side.
	 */
	private final Property<Boolean> failOnInProgress;

	private final ContractRepository contractRepository;

	private final PublishStubsToScm publishStubsToScm;

	/**
	 * Dependency that contains packaged contracts
	 */
	private final Dependency contractDependency;

	/**
	 * The path in the JAR with all the contracts where contracts for this particular
	 * service lay. If not provided will be resolved to {@code groupid/artifactid}.
	 * Example: If {@code groupid} is {@code com.example} and {@code artifactid} is
	 * {@code service} then the resolved path will be {@code /com/example/artifactid}
	 */
	private final Property<String> contractsPath;

	/**
	 * Picks the mode in which stubs will be found and registered
	 */
	private final Property<StubRunnerProperties.StubsMode> contractsMode;

	/**
	 * A package that contains all the base clases for generated tests. If your contract
	 * resides in a location {@code src/contractTest/resources/contracts/com/example/v1/} and you
	 * provide the {@code packageWithBaseClasses} value to
	 * {@code com.example.contracts.base} then we will search for a test source file that
	 * will have the package {@code com.example.contracts.base} and name
	 * {@code ExampleV1Base}. As you can see it will take the two last folders to and
	 * attach {@code Base} to its name.
	 */
	private final Property<String> packageWithBaseClasses;

	/**
	 * A way to override any base class mappings. The keys are regular expressions on the
	 * package name and the values FQN to a base class for that given expression. Example
	 * of a mapping [.*.com.example.v1..*] to [com.example.SomeBaseClass] When a
	 * contract's package matches the provided regular expression then extending class
	 * will be the one provided in the map - in this case
	 * {@code com.example.SomeBaseClass}
	 */
	private final BaseClassMapping baseClassMappings;

	/**
	 * If set to true then the {@code target} or {@code build} folders are getting
	 * excluded from any operations. This is used out of the box when working with common
	 * repo with contracts.
	 */
	private final Property<Boolean> excludeBuildFolders;

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary folder after running
	 * tests
	 */
	private final Property<Boolean> deleteStubsAfterTest;

	/**
	 * If {@code true} then will convert contracts to a YAML representation
	 */
	private final Property<Boolean> convertToYaml;

	/**
	 * Map of properties that can be passed to custom
	 * {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}
	 */
	private final MapProperty<String, String> contractsProperties;

	/**
	 * Source set where the contracts are stored. If not provided will assume
	 * {@code test}.
	 */
	private final Property<String> sourceSet;

	@Inject
	public ContractVerifierExtension(ProjectLayout layout, ObjectFactory objects) {
		this.testFramework = objects.property(TestFramework.class).convention(TestFramework.JUNIT5);
		this.testMode = objects.property(TestMode.class).convention(TestMode.MOCKMVC);
		this.basePackageForTests = objects.property(String.class);
		this.baseClassForTests = objects.property(String.class);
		this.nameSuffixForTests = objects.property(String.class);
		this.excludedFiles = objects.listProperty(String.class).convention(new ArrayList<>());
		this.includedFiles = objects.listProperty(String.class).convention(new ArrayList<>());
		this.ignoredFiles = objects.listProperty(String.class).convention(new ArrayList<>());
		this.imports = objects.listProperty(String.class).convention(new ArrayList<>());
		this.staticImports = objects.listProperty(String.class).convention(new ArrayList<>());
		this.contractsDslDir = objects.directoryProperty()
				.convention(layout.getProjectDirectory().dir("src/contractTest/resources/contracts"));
		this.generatedTestJavaSourcesDir = objects.directoryProperty()
				.convention(layout.getBuildDirectory().dir("generated-test-sources/contractTest/java"));
		this.generatedTestGroovySourcesDir = objects.directoryProperty()
				.convention(layout.getBuildDirectory().dir("generated-test-sources/contractTest/groovy"));
		this.generatedTestResourcesDir = objects.directoryProperty()
				.convention(layout.getBuildDirectory().dir("generated-test-resources/contractTest"));
		this.stubsOutputDir = objects.directoryProperty().convention(layout.getBuildDirectory().dir("stubs"));
		this.stubsSuffix = objects.property(String.class).convention("stubs");
		this.assertJsonSize = objects.property(Boolean.class).convention(false);
		this.failOnNoContracts = objects.property(Boolean.class).convention(true);
		this.failOnInProgress = objects.property(Boolean.class).convention(true);
		this.contractRepository = objects.newInstance(ContractRepository.class);
		this.publishStubsToScm = objects.newInstance(PublishStubsToScm.class);
		this.contractDependency = objects.newInstance(Dependency.class);
		this.contractsPath = objects.property(String.class);
		this.contractsMode = objects.property(StubRunnerProperties.StubsMode.class)
				.convention(StubRunnerProperties.StubsMode.CLASSPATH);
		this.packageWithBaseClasses = objects.property(String.class);
		this.baseClassMappings = objects.newInstance(BaseClassMapping.class);
		this.excludeBuildFolders = objects.property(Boolean.class).convention(false);
		this.deleteStubsAfterTest = objects.property(Boolean.class).convention(true);
		this.convertToYaml = objects.property(Boolean.class).convention(false);
		this.contractsProperties = objects.mapProperty(String.class, String.class).convention(new HashMap<>());
		this.sourceSet = objects.property(String.class);
	}

	public Property<TestFramework> getTestFramework() {
		return testFramework;
	}

	public void setTestFramework(TestFramework testFramework) {
		this.testFramework.set(testFramework);
	}

	public void setTestFramework(String testFramework) {
		this.testFramework.set(TestFramework.valueOf(testFramework.toUpperCase()));
	}

	public Property<TestMode> getTestMode() {
		return testMode;
	}

	public void setTestMode(TestMode testMode) {
		this.testMode.set(testMode);
	}

	public void setTestMode(String testMode) {
		this.testMode.set(TestMode.valueOf(testMode.toUpperCase()));
	}

	public Property<String> getBasePackageForTests() {
		return basePackageForTests;
	}

	public void setBasePackageForTests(String basePackageForTests) {
		this.basePackageForTests.set(basePackageForTests);
	}

	public Property<String> getBaseClassForTests() {
		return baseClassForTests;
	}

	public void setBaseClassForTests(String baseClassForTests) {
		this.baseClassForTests.set(baseClassForTests);
	}

	public Property<String> getNameSuffixForTests() {
		return nameSuffixForTests;
	}

	public void setNameSuffixForTests(String nameSuffixForTests) {
		this.nameSuffixForTests.set(nameSuffixForTests);
	}

	public ListProperty<String> getExcludedFiles() {
		return excludedFiles;
	}

	public void setExcludedFiles(List<String> excludedFiles) {
		this.excludedFiles.set(excludedFiles);
	}

	public ListProperty<String> getIncludedFiles() {
		return includedFiles;
	}

	public void setIncludedFiles(List<String> includedFiles) {
		this.includedFiles.set(includedFiles);
	}

	public ListProperty<String> getIgnoredFiles() {
		return ignoredFiles;
	}

	public void setIgnoredFiles(List<String> ignoredFiles) {
		this.ignoredFiles.set(ignoredFiles);
	}

	public ListProperty<String> getImports() {
		return imports;
	}

	public void setImports(String[] imports) {
		this.imports.set(new ArrayList<>(Arrays.asList(imports)));
	}

	public void setInputs(List<String> imports) {
		this.imports.set(imports);
	}

	public ListProperty<String> getStaticImports() {
		return staticImports;
	}

	public void setStaticImports(String[] staticImports) {
		this.staticImports.set(new ArrayList<>(Arrays.asList(staticImports)));
	}

	public void setStaticImports(List<String> staticImports) {
		this.staticImports.set(staticImports);
	}

	public DirectoryProperty getContractsDslDir() {
		return contractsDslDir;
	}

	public void setContractsDslDir(File contractsDslDir) {
		this.contractsDslDir.set(contractsDslDir);
	}

	public DirectoryProperty getGeneratedTestJavaSourcesDir() {
		return generatedTestJavaSourcesDir;
	}

	public void setGeneratedTestJavaSourcesDir(File generatedTestJavaSourcesDir) {
		this.generatedTestJavaSourcesDir.set(generatedTestJavaSourcesDir);
	}

	public DirectoryProperty getGeneratedTestGroovySourcesDir() {
		return generatedTestGroovySourcesDir;
	}

	public void setGeneratedTestGroovySourcesDir(File generatedTestGroovySourcesDir) {
		this.generatedTestGroovySourcesDir.set(generatedTestGroovySourcesDir);
	}

	public DirectoryProperty getGeneratedTestResourcesDir() {
		return generatedTestResourcesDir;
	}

	public void setGeneratedTestResourcesDir(File generatedTestResourcesDir) {
		this.generatedTestResourcesDir.set(generatedTestResourcesDir);
	}

	public DirectoryProperty getStubsOutputDir() {
		return stubsOutputDir;
	}

	public void setStubsOutputDir(File stubsOutputDir) {
		this.stubsOutputDir.set(stubsOutputDir);
	}

	public Property<String> getStubsSuffix() {
		return stubsSuffix;
	}

	public void setStubsSuffix(String stubsSuffix) {
		this.stubsSuffix.set(stubsSuffix);
	}

	public Property<Boolean> getAssertJsonSize() {
		return assertJsonSize;
	}

	public void setAssertJsonSize(boolean assertJsonSize) {
		this.assertJsonSize.set(assertJsonSize);
	}

	public Property<Boolean> getFailOnNoContracts() {
		return failOnNoContracts;
	}

	public void setFailOnNoContracts(boolean failOnNoContracts) {
		this.failOnNoContracts.set(failOnNoContracts);
	}

	public Property<Boolean> getFailOnInProgress() {
		return failOnInProgress;
	}

	public void setFailOnInProgress(boolean failOnInProgress) {
		this.failOnInProgress.set(failOnInProgress);
	}

	public ContractRepository getContractRepository() {
		return contractRepository;
	}

	public void contractRepository(Action<ContractRepository> action) {
		action.execute(contractRepository);
	}

	public PublishStubsToScm getPublishStubsToScm() {
		return publishStubsToScm;
	}

	public void publishStubsToScm(Action<PublishStubsToScm> action) {
		action.execute(publishStubsToScm);
	}

	public Dependency getContractDependency() {
		return contractDependency;
	}

	public void contractDependency(Action<Dependency> action) {
		action.execute(contractDependency);
	}

	public Property<String> getContractsPath() {
		return contractsPath;
	}

	public void setContractsPath(String contractsPath) {
		this.contractsPath.set(contractsPath);
	}

	public Property<StubRunnerProperties.StubsMode> getContractsMode() {
		return contractsMode;
	}

	public void setContractsMode(StubRunnerProperties.StubsMode contractsMode) {
		this.contractsMode.set(contractsMode);
	}

	public void setContractsMode(String contractsMode) {
		this.contractsMode.set(StubRunnerProperties.StubsMode.valueOf(contractsMode.toUpperCase()));
	}

	public Property<String> getPackageWithBaseClasses() {
		return packageWithBaseClasses;
	}

	public void setPackageWithBaseClasses(String packageWithBaseClasses) {
		this.packageWithBaseClasses.set(packageWithBaseClasses);
	}

	public BaseClassMapping getBaseClassMappings() {
		return baseClassMappings;
	}

	public void setBaseClassMappings(Map<String, String> baseClassMappings) {
		this.baseClassMappings.getBaseClassMappings().set(baseClassMappings);
	}

	public void baseClassMappings(Action<BaseClassMapping> action) {
		action.execute(baseClassMappings);
	}

	public Property<Boolean> getExcludeBuildFolders() {
		return excludeBuildFolders;
	}

	public void setExcludeBuildFolders(boolean excludeBuildFolders) {
		this.excludeBuildFolders.set(excludeBuildFolders);
	}

	public Property<Boolean> getDeleteStubsAfterTest() {
		return deleteStubsAfterTest;
	}

	public void setDeleteStubsAfterTest(boolean deleteStubsAfterTest) {
		this.deleteStubsAfterTest.set(deleteStubsAfterTest);
	}

	public Property<Boolean> getConvertToYaml() {
		return convertToYaml;
	}

	public void setConvertToYaml(boolean convertToYaml) {
		this.convertToYaml.set(convertToYaml);
	}

	public MapProperty<String, String> getContractsProperties() {
		return contractsProperties;
	}

	public void setContractsProperties(Map<String, String> contractsProperties) {
		this.contractsProperties.set(contractsProperties);
	}

	public Property<String> getSourceSet() {
		return sourceSet;
	}

	public void setSourceSet(String sourceSet) {
		this.sourceSet.set(sourceSet);
	}

	public static class Dependency implements Serializable {

		private final Property<String> groupId;

		private final Property<String> artifactId;

		private final Property<String> version;

		private final Property<String> classifier;

		private final Property<String> stringNotation;

		@Inject
		public Dependency(ObjectFactory objects) {
			groupId = objects.property(String.class);
			artifactId = objects.property(String.class);
			version = objects.property(String.class);
			classifier = objects.property(String.class);
			stringNotation = objects.property(String.class);
		}

		public Property<String> getGroupId() {
			return groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId.set(groupId);
		}

		public Property<String> getArtifactId() {
			return artifactId;
		}

		public void setArtifactId(String artifactId) {
			this.artifactId.set(artifactId);
		}

		public Property<String> getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version.set(version);
		}

		public Property<String> getClassifier() {
			return classifier;
		}

		public void setClassifier(String classifier) {
			this.classifier.set(classifier);
		}

		public Property<String> getStringNotation() {
			return stringNotation;
		}

		public void setStringNotation(String stringNotation) {
			this.stringNotation.set(stringNotation);
		}

		@Override
		public String toString() {
			return "Dependency{" + "groupId=" + groupId.getOrNull() + ", artifactId=" + artifactId.getOrNull()
					+ ", classifier=" + classifier.getOrNull() + ", version=" + version.getOrNull()
					+ ", stringNotation=" + stringNotation.getOrNull() + '}';
		}

	}

	public static class BaseClassMapping implements Serializable {

		private final MapProperty<String, String> baseClassMappings;

		@Inject
		public BaseClassMapping(ObjectFactory objects) {
			this.baseClassMappings = objects.mapProperty(String.class, String.class).convention(new HashMap<>());
		}

		public MapProperty<String, String> getBaseClassMappings() {
			return baseClassMappings;
		}

		public void baseClassMapping(String packageRegex, String fqnBaseClass) {
			baseClassMappings.put(packageRegex, fqnBaseClass);
		}

		public void baseClassMapping(Map<String, String> mapping) {
			baseClassMappings.putAll(mapping);
		}

	}

	// This class is used as an input to the tasks, so all fields are marked as `@Input`
	// to allow incremental build
	public static class ContractRepository implements Serializable {

		private final Property<String> repositoryUrl;

		private final Property<String> username;

		private final Property<String> password;

		private final Property<Integer> proxyPort;

		private final Property<String> proxyHost;

		@Inject
		public ContractRepository(ObjectFactory objects) {
			this.repositoryUrl = objects.property(String.class);
			this.username = objects.property(String.class);
			this.password = objects.property(String.class);
			this.proxyHost = objects.property(String.class);
			this.proxyPort = objects.property(Integer.class);
		}

		public Property<String> getRepositoryUrl() {
			return repositoryUrl;
		}

		public void setRepositoryUrl(String repositoryUrl) {
			this.repositoryUrl.set(repositoryUrl);
		}

		public Property<String> getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username.set(username);
		}

		public Property<String> getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password.set(password);
		}

		public Property<String> getProxyHost() {
			return proxyHost;
		}

		public void setProxyHost(String proxyHost) {
			this.proxyHost.set(proxyHost);
		}

		public Property<Integer> getProxyPort() {
			return proxyPort;
		}

		public void setProxyPort(int proxyPort) {
			Assert.state(0 < proxyPort && proxyPort <= 65536, "Proxy port should be between 1 and 65536");
			this.proxyPort.set(proxyPort);
		}

		@Override
		public String toString() {
			return "ContractRepository{" + "repositoryUrl=" + repositoryUrl.getOrNull() + ", username="
					+ username.getOrNull() + ", password=" + password.getOrNull() + ", proxyPort="
					+ proxyPort.getOrNull() + ", proxyHost=" + proxyHost.getOrNull() + '}';
		}

	}

	public static class PublishStubsToScm implements Serializable {

		/**
		 * Dependency that contains packaged contracts
		 */
		private final Dependency contractDependency;

		private final ContractRepository contractRepository;

		@Inject
		public PublishStubsToScm(ObjectFactory objects) {
			contractDependency = objects.newInstance(Dependency.class);
			contractRepository = objects.newInstance(ContractRepository.class);
		}

		public Dependency getContractDependency() {
			return contractDependency;
		}

		public void contractDependency(Action<Dependency> action) {
			action.execute(contractDependency);
		}

		public ContractRepository getContractRepository() {
			return contractRepository;
		}

		public void contractRepository(Action<ContractRepository> action) {
			action.execute(contractRepository);
		}

		@Override
		public String toString() {
			return "PublishStubsToScm{" + "contractDependency=" + contractDependency + ", contractRepository="
					+ contractRepository + '}';
		}

	}

}
