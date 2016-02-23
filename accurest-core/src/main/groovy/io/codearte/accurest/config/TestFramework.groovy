package io.codearte.accurest.config

/**
 * @author Jakub Kubrynski
 */
enum TestFramework {
	JUNIT("public ", "public void ", ";", ".java", "Test", "org.junit.Ignore", ["org.junit.FixMethodOrder", "org.junit.runners.MethodSorters"], "@FixMethodOrder(MethodSorters.NAME_ASCENDING)"),
	SPOCK("", "def ", "", ".groovy", "Spec", "spock.lang.Ignore", ["spock.lang.Stepwise"], "@Stepwise")

	private final String classModifier
	private final String methodModifier
	private final String lineSuffix
	private final String classExtension;
	private final String classNameSuffix;
	private final String ignoreClass
	private final List<String> orderAnnotationImports
	private final String orderAnnotation

	TestFramework(String classModifier, String methodModifier, String lineSuffix, String classExtension, String classNameSuffix,
	              String ignoreClass, List<String> orderAnnotationImports, String orderAnnotation) {
		this.classModifier = classModifier
		this.lineSuffix = lineSuffix
		this.methodModifier = methodModifier
		this.classExtension = classExtension
		this.classNameSuffix = classNameSuffix
		this.ignoreClass = ignoreClass
		this.orderAnnotationImports = orderAnnotationImports
		this.orderAnnotation = orderAnnotation
	}

	String getClassModifier() {
		return classModifier
	}

	String getMethodModifier() {
		return methodModifier
	}

	String getLineSuffix() {
		return lineSuffix
	}

	String getClassExtension() {
		return classExtension
	}

	String getClassNameSuffix() {
		return classNameSuffix
	}

	String getIgnoreClass() {
		return ignoreClass
	}

	List<String> getOrderAnnotationImport() {
		return orderAnnotationImports
	}

	String getOrderAnnotation() {
		return orderAnnotation
	}
}