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

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.tasks.Internal

import javax.inject.Inject

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

/**
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 */
@CompileStatic
class ContractVerifierExtension {

	private static final Log log = LogFactory.getLog(ContractVerifierExtension)

	@Deprecated
	void setTargetFramework(TestFramework targetFramework) {
		log.warn("Please use the [testFramework] field. This one is deprecated")
		this.testFramework.set(targetFramework)
	}

	@Deprecated
	TestFramework getTargetFramework() {
		return getTestFramework().get()
	}

	/**
	 * For which unit test library tests should be generated
	 */
	Property<TestFramework> testFramework

	/**
	 * Which mechanism should be used to invoke REST calls during tests
	 */
	Property<TestMode> testMode

	/**
	 * Base package for generated tests
	 */
	Property<String> basePackageForTests

	/**
	 * Class which all generated tests should extend
	 */
	Property<String> baseClassForTests

	/**
	 * Suffix for generated test classes, like Spec or Test
	 */
	Property<String> nameSuffixForTests

	/**
	 * Rule class that should be added to generated tests
	 */
	Property<String> ruleClassForTests

	/**
	 * Patterns that should not be taken into account for processing
	 */
	ListProperty<String> excludedFiles

	/**
	 * Patterns that should be taken into account for processing
	 */
	ListProperty<String> includedFiles

	/**
	 * Patterns for which generated tests should be @Ignored
	 */
	ListProperty<String> ignoredFiles

	/**
	 * Imports that should be added to generated tests
	 */
	ListProperty<String> imports

	/**
	 * Static imports that should be added to generated tests
	 */
	ListProperty<String> staticImports

	/**
	 * Directory containing contracts written using the GroovyDSL
	 */
	DirectoryProperty contractsDslDir

	/**
	 * Test source directory where tests generated from Groovy DSL should be placed
	 */
	DirectoryProperty generatedTestSourcesDir

	/**
	 * Test resource directory where tests generated from Groovy DSL should be referenced
	 */
	DirectoryProperty generatedTestResourcesDir

	/**
	 * Dir where the generated stubs from Groovy DSL should be placed.
	 * You can then mention them in your packaging task to create jar with stubs
	 */
	DirectoryProperty stubsOutputDir

	/**
	 * Suffix for the generated Stubs Jar task
	 */
	Property<String> stubsSuffix

	/**
	 * Incubating feature. You can check the size of JSON arrays. If not turned on
	 * explicitly will be disabled.
	 */
	Property<Boolean> assertJsonSize

	/**
	 * When enabled, this flag will tell stub runner to throw an exception when no stubs /
	 * contracts were found.
	 */
	Property<Boolean> failOnNoContracts

	/**
	 * If set to true then if any contracts that are in progress are found, will break the
	 * build. On the producer side you need to be explicit about the fact that you have
	 * contracts in progress and take into consideration that you might be causing false
	 * positive test execution results on the consumer side.
	 */
	Property<Boolean> failOnInProgress

	ContractRepository contractRepository

	/**
	 * Dependency that contains packaged contracts
	 */
	Dependency contractDependency

	/**
	 * The path in the JAR with all the contracts where contracts for this particular service lay.
	 * If not provided will be resolved to {@code groupid/artifactid}. Example:
	 * </p>
	 * If {@code groupid} is {@code com.example} and {@code artifactid} is {@code service} then the resolved path will be
	 * {@code /com/example/artifactid}
	 */
	Property<String> contractsPath

	/**
	 * Picks the mode in which stubs will be found and registered
	 */
	Property<StubRunnerProperties.StubsMode> contractsMode

	/**
	 * A package that contains all the base clases for generated tests. If your contract resides in a location
	 * {@code src/test/resources/contracts/com/example/v1/} and you provide the {@code packageWithBaseClasses}
	 * value to {@code com.example.contracts.base} then we will search for a test source file that will
	 * have the package {@code com.example.contracts.base} and name {@code ExampleV1Base}. As you can see
	 * it will take the two last folders to and attach {@code Base} to its name.
	 */
	Property<String> packageWithBaseClasses

	/**
	 * A way to override any base class mappings. The keys are regular expressions on the package name
	 * and the values FQN to a base class for that given expression.
	 * </p>
	 * Example of a mapping
	 * </p>
	 * {@code .*.com.example.v1..*} -> {@code com.example.SomeBaseClass}
	 * </p>
	 * When a contract's package matches the provided regular expression then extending class will be the one
	 * provided in the map - in this case {@code com.example.SomeBaseClass}
	 */
	MapProperty<String, String> baseClassMappings

	/**
	 * If set to true then the {@code target} or {@code build} folders are getting
	 * excluded from any operations. This is used out of the box when working with
	 * common repo with contracts.
	 */
	Property<Boolean> excludeBuildFolders

	/**
	 * If set to {@code true} will not assert whether the downloaded stubs / contract
	 * JAR was downloaded from a remote location or a local one(only applicable to Maven repos, not Git or Pact)
	 *
	 * @deprecated - with 2.1.0 this option is redundant
	 */
	@Deprecated
	Property<Boolean> contractsSnapshotCheckSkip

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary
	 * folder after running tests
	 */
	Property<Boolean> deleteStubsAfterTest

	/**
	 * If {@code true} then will convert contracts to a YAML representation
	 */
	Property<Boolean> convertToYaml

	/**
	 * Map of properties that can be passed to custom {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}
	 */
	MapProperty<String, String> contractsProperties

	void contractDependency(@DelegatesTo(Dependency) Closure closure) {
		closure.delegate = contractDependency
		closure.call()
	}

	void baseClassMappings(@DelegatesTo(BaseClassMapping) Closure closure) {
		closure.delegate = new BaseClassMapping(baseClassMappings)
		closure.call()
	}

	void contractRepository(@DelegatesTo(ContractRepository) Closure closure) {
		closure.delegate = contractRepository
		closure.call()
	}

	void contractsProperties(Map<String, String> map) {
		contractsProperties.set(map)
	}

	void setContractsProperties(Map<String, String> map) {
		contractsProperties.set(map)
	}

	/**
	 * Is set to true will not provide the default publication task
	 */
	Property<Boolean> disableStubPublication

	// Added for backward compatibility only. Use setter of dedicated enum type
	@Deprecated
	void setTestMode(String testMode) {
		if (testMode != null) {
			this.testMode.set(TestMode.valueOf(testMode.toUpperCase()))
		}
	}

	// Added for backward compatibility only. Use setter of dedicated enum type
	@Deprecated
	void setTestFramework(String testFramework) {
		if (testFramework != null) {
			this.testFramework.set(TestFramework.valueOf(testFramework.toUpperCase()))
		}
	}

	// Added for backward compatibility only. Use setter of dedicated enum type
	@Deprecated
	void setContractsMode(String contractsMode) {
		if (contractsMode != null) {
			this.contractsMode.set(StubRunnerProperties.StubsMode.valueOf(contractsMode.toUpperCase()))
		}
	}

	void setBasePackageForTests(String basePackageForTests) {
		this.basePackageForTests.set(basePackageForTests)
	}

	void setBaseClassForTests(String baseClassForTests) {
		this.baseClassForTests.set(baseClassForTests)
	}

	void setNameSuffixForTests(String nameSuffixForTests) {
		this.nameSuffixForTests.set(nameSuffixForTests)
	}

	void setRuleClassForTests(String ruleClassForTests) {
		this.ruleClassForTests.set(ruleClassForTests)
	}

	void setContractsDslDir(String contractsDslDir) {
		this.contractsDslDir.set(new File(contractsDslDir))
	}

	void setGeneratedTestSourcesDir(String generatedTestSourcesDir) {
		this.generatedTestSourcesDir.set(new File(generatedTestSourcesDir))
	}

	void setGeneratedTestResourcesDir(String generatedTestResourcesDir) {
		this.generatedTestResourcesDir.set(new File(generatedTestResourcesDir))
	}

	void setStubsOutputDir(String stubsOutputDir) {
		this.stubsOutputDir.set(new File(stubsOutputDir))
	}

	void setStubsSuffix(String stubsSuffix) {
		this.stubsSuffix.set(stubsSuffix)
	}

	void setContractsPath(String contractsPath) {
		this.contractsPath.set(contractsPath)
	}

	void setPackageWithBaseClasses(String packageWithBaseClasses) {
		this.packageWithBaseClasses.set(packageWithBaseClasses)
	}

	void setBasePackageForTests(GString basePackageForTests) {
		this.basePackageForTests.set(basePackageForTests.toString())
	}

	void setBaseClassForTests(GString baseClassForTests) {
		this.baseClassForTests.set(baseClassForTests.toString())
	}

	void setNameSuffixForTests(GString nameSuffixForTests) {
		this.nameSuffixForTests.set(nameSuffixForTests.toString())
	}

	void setRuleClassForTests(GString ruleClassForTests) {
		this.ruleClassForTests.set(ruleClassForTests.toString())
	}

	void setContractsDslDir(GString contractsDslDir) {
		this.contractsDslDir.set(new File(contractsDslDir.toString()))
	}

	void setGeneratedTestSourcesDir(GString generatedTestSourcesDir) {
		this.generatedTestSourcesDir.set(new File(generatedTestSourcesDir.toString()))
	}

	void setGeneratedTestResourcesDir(GString generatedTestResourcesDir) {
		this.generatedTestResourcesDir.set(new File(generatedTestResourcesDir.toString()))
	}

	void setStubsOutputDir(GString stubsOutputDir) {
		this.stubsOutputDir.set(new File(stubsOutputDir.toString()))
	}

	void setStubsSuffix(GString stubsSuffix) {
		this.stubsSuffix.set(stubsSuffix.toString())
	}

	void setContractsPath(GString contractsPath) {
		this.contractsPath.set(contractsPath.toString())
	}

	void setPackageWithBaseClasses(GString packageWithBaseClasses) {
		this.packageWithBaseClasses.set(packageWithBaseClasses.toString())
	}


	@Inject
	ContractVerifierExtension(ObjectFactory objects) {
		this.testFramework = objects.property(TestFramework).convention(TestFramework.JUNIT)
		this.testMode = objects.property(TestMode).convention(TestMode.MOCKMVC)
		this.basePackageForTests = objects.property(String)
		this.baseClassForTests = objects.property(String)
		this.nameSuffixForTests = objects.property(String)
		this.ruleClassForTests = objects.property(String)
		this.excludedFiles = objects.listProperty(String).convention([])
		this.includedFiles = objects.listProperty(String).convention([])
		this.ignoredFiles = objects.listProperty(String).convention([])
		this.imports = objects.listProperty(String).convention([])
		this.staticImports = objects.listProperty(String).convention([])
		this.contractsDslDir = objects.directoryProperty()
		this.generatedTestSourcesDir = objects.directoryProperty()
		this.generatedTestResourcesDir = objects.directoryProperty()
		this.stubsOutputDir = objects.directoryProperty()
		this.stubsSuffix = objects.property(String).convention("stubs")
		this.assertJsonSize = objects.property(Boolean).convention(false)
		this.failOnNoContracts = objects.property(Boolean).convention(true)
		this.failOnInProgress = objects.property(Boolean).convention(true)
		this.contractRepository = new ContractRepository(objects)
		this.contractDependency = new Dependency(objects)
		this.contractsPath = objects.property(String)
		this.contractsMode = objects.property(StubRunnerProperties.StubsMode).convention(StubRunnerProperties.StubsMode.CLASSPATH)
		this.packageWithBaseClasses = objects.property(String)
		this.baseClassMappings = objects.mapProperty(String, String).convention([:])
		this.excludeBuildFolders = objects.property(Boolean).convention(false)
		this.contractsSnapshotCheckSkip = objects.property(Boolean).convention(false)
		this.deleteStubsAfterTest = objects.property(Boolean).convention(true)
		this.convertToYaml = objects.property(Boolean).convention(false)
		this.contractsProperties = objects.mapProperty(String, String).convention([:])
		this.disableStubPublication = objects.property(Boolean).convention(false)
	}

	static class Dependency {
		@Input
		@Optional
		Property<String> groupId
		@Input
		@Optional
		Property<String> artifactId
		@Input
		@Optional
		Property<String> version
		@Input
		@Optional
		Property<String> classifier
		@Input
		@Optional
		Property<String> stringNotation

		@Inject
		Dependency(ObjectFactory objects) {
			groupId = objects.property(String)
			artifactId = objects.property(String)
			version = objects.property(String)
			classifier = objects.property(String)
			stringNotation = objects.property(String)
		}

		@Override
		String toString() {
			return "Dependency{" +
					"groupId=" + groupId.getOrNull() +
					", artifactId=" + artifactId.getOrNull() +
					", classifier=" + classifier.getOrNull() +
					", version=" + version.getOrNull() +
					", stringNotation=" + stringNotation.getOrNull() +
					'}'
		}

		void setGroupId(String groupId) {
			this.groupId.set(groupId)
		}

		void setArtifactId(String artifactId) {
			this.artifactId.set(artifactId)
		}

		void setVersion(String version) {
			this.version.set(version)
		}

		void setClassifier(String classifier) {
			this.classifier.set(classifier)
		}

		void setStringNotation(String stringNotation) {
			this.stringNotation.set(stringNotation)
		}

		void setGroupId(GString groupId) {
			this.groupId.set(groupId.toString())
		}

		void setArtifactId(GString artifactId) {
			this.artifactId.set(artifactId.toString())
		}

		void setVersion(GString version) {
			this.version.set(version.toString())
		}

		void setClassifier(GString classifier) {
			this.classifier.set(classifier.toString())
		}

		void setStringNotation(GString stringNotation) {
			this.stringNotation.set(stringNotation.toString())
		}
	}

	static class BaseClassMapping {
		private final MapProperty<String, String> delegate

		private BaseClassMapping(MapProperty<String, String> delegate) {
			this.delegate = delegate
		}

		void baseClassMapping(String packageRegex, String fqnBaseClass) {
			delegate.put(packageRegex, fqnBaseClass)
		}

		void baseClassMapping(Map<String, String> mapping) {
			delegate.putAll(mapping)
		}
	}

	// This class is used as an input to the tasks, so all fields are marked as `@Input` to allow incremental build
	static class ContractRepository {
		@Input
		@Optional
		Property<String> repositoryUrl
		@Input
		@Optional
		Property<String> username
		@Input
		@Optional
		Property<String> password
		@Input
		@Optional
		Property<Integer> proxyPort
		@Input
		@Optional
		Property<String> proxyHost
		/**
		 * Not used any more, as we switched to Gradle's incremental build.
		 */
		@Internal
		@Deprecated
		boolean cacheDownloadedContracts

		@Inject
		ContractRepository(ObjectFactory objects) {
			this.repositoryUrl = objects.property(String)
			this.username = objects.property(String)
			this.password = objects.property(String)
			this.proxyHost = objects.property(String)
			this.proxyPort = objects.property(Integer)
		}

		@Override
		String toString() {
			return "ContractRepository{" +
					"repositoryUrl=" + repositoryUrl.getOrNull() +
					", username=" + username.getOrNull() +
					", password=" + password.getOrNull() +
					", proxyPort=" + proxyPort.getOrNull() +
					", proxyHost=" + proxyHost.getOrNull() +
					'}'
		}

		void setRepositoryUrl(String repositoryUrl) {
			this.repositoryUrl.set(repositoryUrl)
		}

		void setUsername(String username) {
			this.username.set(username)
		}

		void setPassword(String password) {
			this.password.set(password)
		}

		void setProxyHost(String proxyHost) {
			this.proxyHost.set(proxyHost)
		}

		void setRepositoryUrl(GString repositoryUrl) {
			this.repositoryUrl.set(repositoryUrl.toString())
		}

		void setUsername(GString username) {
			this.username.set(username.toString())
		}

		void setPassword(GString password) {
			this.password.set(password.toString())
		}

		void setProxyHost(GString proxyHost) {
			this.proxyHost.set(proxyHost.toString())
		}

		void setProxyPort(Integer proxyPort) {
			this.proxyPort.set(proxyPort)
		}

		void repositoryUrl(String repositoryUrl) {
			this.repositoryUrl.set(repositoryUrl)
		}

		void username(String username) {
			this.username.set(username)
		}

		void password(String password) {
			this.password.set(password)
		}

		void proxyHost(String proxyHost) {
			this.proxyHost.set(proxyHost)
		}

		void repositoryUrl(GString repositoryUrl) {
			this.repositoryUrl.set(repositoryUrl.toString())
		}

		void username(GString username) {
			this.username.set(username.toString())
		}

		void password(GString password) {
			this.password.set(password.toString())
		}

		void proxyHost(GString proxyHost) {
			this.proxyHost.set(proxyHost.toString())
		}
	}
}