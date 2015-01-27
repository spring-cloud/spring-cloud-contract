package io.codearte.accurest.plugin

import io.coderate.accurest.config.TestFramework
import io.coderate.accurest.config.TestMode

/**
 * @author Jakub Kubrynski
 */
class AccurestPluginExtension {
	TestFramework targetFramework = TestFramework.SPOCK
	TestMode testMode = TestMode.EXPLICIT
	String stubsBaseDirectory = 'mappings'
	String basePackageForTests = 'io.codearte.accurest.tests'
	String baseClassForTests
	String ruleClassForTests
	String generatedTestSourcesDir = 'build/generated-sources/accurest'
}
