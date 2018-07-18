package org.springframework.cloud.contract.verifier.config.framework

import groovy.transform.CompileStatic

/**
 * Defines elements characteristic of a given test framework to be used during test class construction.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
interface TestFrameworkDefinition {

	/**
	 * Returns the class access level modifier. E.g. for Java tests that would mean {@code public}
	 **/
	String getClassModifier()

	/**
	 * Returns the method access level modifier along with the return type.
	 * E.g. for Java tests that would mean {@code public void}
	 **/
	String getMethodModifier()

	/**
	 * Returns the characters that should end each line. E.g. for Java tests that would mean {@code ;}
	 **/
	String getLineSuffix()

	/**
	 * Returns the file extension. E.g. for Java tests that would be {@code .java}
	 **/
	String getClassExtension()

	/**
	 * Returns the test class name suffix. E.g. for JUnit tests that would be {@code Test}
	 **/
	String getClassNameSuffix()

	/**
	 * Returns the qualified name of the class used to ignore or disable tests. E.g. for JUnit 4 tests that would
	 * be {@code org.junit.Ignore}
	 **/
	String getIgnoreClass()

	/**
	 * Returns the qualified names of the classes that are used for arranging tests into scenarios.
	 * E.g. for JUnit 4 tests that would be {@code 'org.junit.FixMethodOrder'}, {@code 'org.junit.runners.MethodSorters'}
	 **/
	List<String> getOrderAnnotationImports()

	/**
	 * Returns the annotation used for arranging tests into scenarios.
	 * E.g. for JUnit test that would be {@code @FixMethodOrder(MethodSorters.NAME_ASCENDING)}
	 **/
	String getOrderAnnotation()

	/**
	 * Returns the annotation used for ignoring or disabling tests. E.g. for JUnit tests that would mean {@code @Ignore}
	 **/
	String getIgnoreAnnotation()

	/**
	 * Returns a boolean indicating whether an annotation-type rule or extension is being used or not.
	 * E.g. for JUnit 5 tests that would return {@code true}
	 **/
	boolean annotationLevelRules()

	/**
	 * Returns the test rule or extension annotation with the {@annotationValue} passed as an argument.
	 * E.g. for JUnit 5 tests that could be {@code @ExtendWith(Example.class)}
	 **/
	String getRuleAnnotation(String annotationValue)
}
