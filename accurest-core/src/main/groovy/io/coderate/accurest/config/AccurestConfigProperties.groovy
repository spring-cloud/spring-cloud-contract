package io.coderate.accurest.config

/**
 * @author Jakub Kubrynski
 */
class AccurestConfigProperties {
	TestFramework targetFramework = TestFramework.SPOCK
	TestMode testMode = TestMode.MOCKMVC
	String stubsBaseDirectory = 'src/test/resources/stubs'
	String basePackageForTests = 'io.codearte.accurest.tests'
	String baseClassForTests
	String ruleClassForTests
	List<String> ignoredFiles = []
	String generatedTestSourcesDir = 'build/generated-sources/accurest'
	String[] imports = []
	String[] staticImports = []
}
