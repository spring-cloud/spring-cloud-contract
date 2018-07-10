package org.springframework.cloud.contract.verifier.config.framework

/**
 * Defines elements characteristic of a given test framework to be used during test class construction.
 *
 * @author Olga Maciaszek-Sharma
 */
interface TestFrameworkDefinition {

	String getClassModifier()

	String getMethodModifier()

	String getLineSuffix()

	String getClassExtension()

	String getClassNameSuffix()

	String getIgnoreClass()

	List<String> getOrderAnnotationImports()

	String getOrderAnnotation()

	String getIgnoreAnnotation()

	boolean annotationLevelRules()

	String getRuleAnnotation(String annotationValue)
}
