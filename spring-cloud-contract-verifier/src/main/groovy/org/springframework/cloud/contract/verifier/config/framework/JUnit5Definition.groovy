package org.springframework.cloud.contract.verifier.config.framework

import groovy.transform.CompileStatic

/**
 * Defines elements characteristic of JUnit5 test framework to be used during test class construction.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
class JUnit5Definition implements TestFrameworkDefinition {

	@Override
	String getClassModifier() {
		return 'public '
	}

	@Override
	String getMethodModifier() {
		return 'public void '
	}

	@Override
	String getLineSuffix() {
		return ';'
	}

	@Override
	String getClassExtension() {
		'.java'
	}

	@Override
	String getClassNameSuffix() {
		return 'Test'
	}

	@Override
	String getIgnoreClass() {
		return 'org.junit.jupiter.api.Disabled'
	}

	@Override
	List<String> getOrderAnnotationImports() {
		throw new UnsupportedOperationException('Not implemented yet in JUnit5 - https://github.com/junit-team/junit5/issues/48')
	}

	@Override
	String getOrderAnnotation() {
		throw new UnsupportedOperationException('Not implemented yet in JUnit5 - https://github.com/junit-team/junit5/issues/48')
	}

	@Override
	String getIgnoreAnnotation() {
		return '@Disabled'
	}

	@Override
	boolean annotationLevelRules() {
		return true
	}

	@Override
	String getRuleAnnotation(String annotationValue) {
		return ("@ExtendWith(${annotationValue}.class)")
	}
}
