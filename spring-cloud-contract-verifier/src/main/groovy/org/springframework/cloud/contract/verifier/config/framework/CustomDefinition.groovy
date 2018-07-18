package org.springframework.cloud.contract.verifier.config.framework

import groovy.transform.CompileStatic

/**
 * Defines elements characteristic of Custom test framework to be used during test class construction.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
class CustomDefinition implements TestFrameworkDefinition {

	@Override
	String getClassModifier() {
		return ''
	}

	@Override
	String getMethodModifier() {
		return ''
	}

	@Override
	String getLineSuffix() {
		return ''
	}

	@Override
	String getClassExtension() {
		return ''
	}

	@Override
	String getClassNameSuffix() {
		return ''
	}

	@Override
	String getIgnoreClass() {
		return ''
	}

	@Override
	List<String> getOrderAnnotationImports() {
		return []
	}

	@Override
	String getOrderAnnotation() {
		return ''
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
		throw new UnsupportedOperationException('Not available in framework.')
	}
}
