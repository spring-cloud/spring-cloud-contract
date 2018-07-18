package org.springframework.cloud.contract.verifier.config.framework

import groovy.transform.CompileStatic

/**
 * Defines elements characteristic of Spock test framework to be used during test class construction.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
class SpockDefinition implements TestFrameworkDefinition {

	@Override
	String getClassModifier() {
		return ''
	}

	@Override
	String getMethodModifier() {
		return 'def '
	}

	@Override
	String getLineSuffix() {
		return ''
	}

	@Override
	String getClassExtension() {
		return '.groovy'
	}

	@Override
	String getClassNameSuffix() {
		return 'Spec'
	}

	@Override
	String getIgnoreClass() {
		return 'spock.lang.Ignore'
	}

	@Override
	List<String> getOrderAnnotationImports() {
		return ['spock.lang.Stepwise']
	}

	@Override
	String getOrderAnnotation() {
		return '@Stepwise'
	}

	@Override
	String getIgnoreAnnotation() {
		return '@Ignore'
	}

	@Override
	boolean annotationLevelRules() {
		return false
	}

	@Override
	String getRuleAnnotation(String annotationValue) {
		throw new UnsupportedOperationException('Not available in Spock.')
	}
}
