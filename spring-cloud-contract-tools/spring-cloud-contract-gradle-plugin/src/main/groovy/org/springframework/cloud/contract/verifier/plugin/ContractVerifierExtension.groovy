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
	 * Dir where the generated WireMock stubs from Groovy DSL should be placed.
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

	void contractDependency(@DelegatesTo(Dependency) Closure closure) {
		closure.delegate = contractDependency
		closure.call()
	}

	static class Dependency {
		String groupId
		String artifactId
		String classifier
		String version
		String stringNotation
	}
}
