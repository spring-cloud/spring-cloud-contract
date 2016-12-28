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

package org.springframework.cloud.contract.verifier.config

/**
 * Represents Contract Verifier configuration properties
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
class ContractVerifierConfigProperties {

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
	 * A regular expression that matches contracts. Especially useful when using a single JAR containing
	 * all the contracts in the system. In this case you'd like to take into consideration only some of them.
	 * Defaults to picking all files.
	 */
	String includedContracts = ".*"

	/**
	 * A ant pattern to match files. Gets updated when using repo with common contracts
	 * to reflect the path to proper folder with contracts.
	 */
	String includedRootFolderAntPattern = "**/"

	/**
	 * A package that contains all the base clases for generated tests. If your contract resides in a location
	 * {@code src/test/resources/contracts/com/example/v1/} and you provide the {@code packageWithBaseClasses}
	 * value to {@code com.example.contracts.base} then we will search for a test source file that will
	 * have the package {@code com.example.contracts.base} and name {@code ExampleV1Base}. As you can see
	 * it will take the two last folders to and attach {@code Base} to its name.
	 */
	String packageWithBaseClasses

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
	Map<String, String> baseClassMappings

	/**
	 * If set to true then the {@code target} or {@code build} folders are getting
	 * excluded from any operations. This is used out of the box when working with
	 * common repo with contracts.
	 */
	boolean excludeBuildFolders

}
