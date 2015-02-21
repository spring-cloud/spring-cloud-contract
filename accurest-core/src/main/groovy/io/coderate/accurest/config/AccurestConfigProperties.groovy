package io.coderate.accurest.config

/**
 * @author Jakub Kubrynski
 */
class AccurestConfigProperties {
	TestFramework targetFramework = TestFramework.SPOCK
	TestMode testMode = TestMode.MOCKMVC
	File stubsBaseDirectory
	String basePackageForTests
	String baseClassForTests
	String ruleClassForTests
	List<String> ignoredFiles = []
	File generatedTestSourcesDir
	String[] imports = []
	String[] staticImports = []
	File groovyDslDir
	File generatedWiremockClientStubsDir
}
