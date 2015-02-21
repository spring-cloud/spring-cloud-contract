package io.coderate.accurest.config

/**
 * @author Jakub Kubrynski
 */
enum TestFramework {
	JUNIT("public ", "public void ", ";", ".java", "Test"),
	SPOCK("", "def ", "", ".groovy", "Spec")

	private final String classModifier
	private final String methodModifier
	private final String lineSuffix
	private final String classExtension;
	private final String classNameSuffix;

	TestFramework(String classModifier, String methodModifier, String lineSuffix, String classExtension, String classNameSuffix) {
		this.classModifier = classModifier
		this.lineSuffix = lineSuffix
		this.methodModifier = methodModifier
		this.classExtension = classExtension
		this.classNameSuffix = classNameSuffix
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
}