package io.codearte.accurest.plugin

/**
 * @author Jakub Kubrynski
 */
class AccurestPluginExtension {
	String targetFramework = 'Spock'
	String stubsBaseDirectory = 'mappings'
	String basePackageForTests = 'io.codearte.accurest.tests'
	String baseClassForTests
	String ruleClassForTests
	String generatedTestSourcesDir = "generated-test-sources"
}
