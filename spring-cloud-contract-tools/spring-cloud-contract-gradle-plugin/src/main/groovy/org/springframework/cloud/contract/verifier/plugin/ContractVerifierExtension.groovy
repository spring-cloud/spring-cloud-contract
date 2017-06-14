package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.ToString
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

/**
 * @author Marcin Grzejszczak
 */
@ToString
class ContractVerifierExtension {
	/**
	 * For which unit test library tests should be generated
	 */
	TestFramework targetFramework = TestFramework.JUNIT

	/**
	 * Which mechanism should be used to invoke REST calls during tests
	 */
	TestMode testMode = TestMode.MOCKMVC

	/**
	 * Base package for generated tests
	 */
	String basePackageForTests

	/**
	 * Class which all generated tests should extend
	 */
	String baseClassForTests

	/**
	 * Suffix for generated test classes, like Spec or Test
	 */
	String nameSuffixForTests

	/**
	 * Rule class that should be added to generated tests
	 */
	String ruleClassForTests

	/**
	 * Patterns that should not be taken into account for processing
	 */
	List<String> excludedFiles = []

	/**
	 * Patterns for which generated tests should be @Ignored
	 */
	List<String> ignoredFiles = []

	/**
	 * Imports that should be added to generated tests
	 */
	String[] imports = []

	/**
	 * Static imports that should be added to generated tests
	 */
	String[] staticImports = []

	/**
	 * Directory containing contracts written using the GroovyDSL
	 */
	File contractsDslDir

	/**
	 * Test source directory where tests generated from Groovy DSL should be placed
	 */
	File generatedTestSourcesDir

	/**
	 * Dir where the generated stubs from Groovy DSL should be placed.
	 * You can then mention them in your packaging task to create jar with stubs
	 */
	File stubsOutputDir

	/**
	 * Suffix for the generated Stubs Jar task
	 */
	String stubsSuffix = 'stubs'

	/**
	 * Incubating feature. You can check the size of JSON arrays. If not turned on
	 * explicitly will be disabled.
	 */
	Boolean assertJsonSize = false

	/**
	 * The URL from which a JAR containing the contracts should get downloaded. If not provided
	 * but artifactid / coordinates notation was provided then the current Maven's build repositories will be
	 * taken into consideration
	 */
	String contractsRepositoryUrl

	/**
	 * Dependency that contains packaged contracts
	 */
	Dependency contractDependency = new Dependency()

	/**
	 * The path in the JAR with all the contracts where contracts for this particular service lay.
	 * If not provided will be resolved to {@code groupid/artifactid}. Example:
	 * </p>
	 * If {@code groupid} is {@code com.example} and {@code artifactid} is {@code service} then the resolved path will be
	 * {@code /com/example/artifactid}
	 */
	String contractsPath

	/**
	 * If {@code true} then JAR with contracts will be taken from local maven repository
	 */
	boolean contractsWorkOffline

	/**
	 * A package that contains all the base clases for generated tests. If your contract resides in a location
	 * {@code src/test/resources/contracts/com/example/v1/} and you provide the {@code packageWithBaseClasses}
	 * value to {@code com.example.contracts.base} then we will search for a test source file that will
	 * have the package {@code com.example.contracts.base} and name {@code ExampleV1Base}. As you can see
	 * it will take the two last folders to and attach {@code Base} to its name.
	 */
	String packageWithBaseClasses

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
	Map<String, String> baseClassMappings = [:]

	/**
	 * If set to true then the {@code target} or {@code build} folders are getting
	 * excluded from any operations. This is used out of the box when working with
	 * common repo with contracts.
	 */
	boolean excludeBuildFolders

	void contractDependency(@DelegatesTo(Dependency) Closure closure) {
		closure.delegate = contractDependency
		closure.call()
	}

	void baseClassMappings(@DelegatesTo(BaseClassMapping) Closure closure) {
		closure.delegate = new BaseClassMapping(baseClassMappings)
		closure.call()
	}

	@ToString
	static class Dependency {
		String groupId
		String artifactId
		String classifier
		String version
		String stringNotation
	}

	static class BaseClassMapping {
		private final Map<String, String> delegate

		BaseClassMapping(Map<String, String> delegate) {
			this.delegate = delegate
		}

		void baseClassMapping(String packageRegex, String fqnBaseClass) {
			this.delegate[packageRegex] = fqnBaseClass
		}

		void baseClassMapping(Map mapping) {
			this.delegate.putAll(mapping)
		}
	}
}
