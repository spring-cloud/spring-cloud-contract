package io.codearte.accurest.config
/**
 * @author Jakub Kubrynski
 */
class AccurestConfigProperties {
	TestFramework targetFramework = TestFramework.SPOCK
	TestMode testMode = TestMode.MOCKMVC
	String basePackageForTests
	String baseClassForTests
	String ruleClassForTests
	List<String> ignoredFiles = []
	String[] imports = []
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
