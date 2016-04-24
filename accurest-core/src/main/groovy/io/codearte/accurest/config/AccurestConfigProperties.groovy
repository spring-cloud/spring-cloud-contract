package io.codearte.accurest.config
/**
 * @author Jakub Kubrynski
 */
class AccurestConfigProperties {

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
	 * Which version of JSON Assert (com.toomuchcoding.jsonassert:jsonassert) to use
	 */
	String jsonAssertVersion = "+"

	/**
	 * Patterns that should not be taken into account for processing
	 */
	List<String> excludedFiles = []

	/**
	 * Patterns for which Accurest should generate @Ignored tests
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

}
