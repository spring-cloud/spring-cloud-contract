package io.coderate.accurest.config

/**
 * @author Jakub Kubrynski
 */
class AccurestConfigProperties {
	TestFramework targetFramework = TestFramework.SPOCK
	TestMode testMode = TestMode.MOCKMVC
	String stubsBaseDirectory = 'stubs'
	String basePackageForTests = 'io.codearte.accurest.tests'
	String baseClassForTests
	String ruleClassForTests
	String generatedTestSourcesDir = 'build/generated-sources/accurest'
	String[] imports = []
	String[] staticImports = []
}
