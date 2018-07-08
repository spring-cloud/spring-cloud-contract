package org.springframework.cloud.contract.verifier.config.framework

/**
 * Defines elements characteristic of JUnit test framework to be used during test class construction.
 *
 * @author Olga Maciaszek-Sharma
 */
class JUnitDefinition implements TestFrameworkDefinition {

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
		return '.java'
	}

	@Override
	String getClassNameSuffix() {
		return 'Test'
	}

	@Override
	String getIgnoreClass() {
		return 'org.junit.Ignore'
	}

	@Override
	List<String> getOrderAnnotationImports() {
		return ['org.junit.FixMethodOrder', 'org.junit.runners.MethodSorters']
	}

	@Override
	String getOrderAnnotation() {
		return '@FixMethodOrder(MethodSorters.NAME_ASCENDING)'
	}

	@Override
	String getIgnoreAnnotation() {
		return '@Ignore'
	}
}
