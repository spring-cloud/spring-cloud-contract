package io.coderate.accurest.config

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
	File contractsDslDir
	File generatedTestSourcesDir
	File stubsOutputDir
}
