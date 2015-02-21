package io.coderate.accurest.config

/**
 * @author Jakub Kubrynski
 */
enum TestFramework {
	JUNIT("public ", "public void ", ";"), SPOCK("", "def ", "")

	private final String classModifier
	private final String methodModifier
	private final String lineSuffix

	TestFramework(String classModifier, String methodModifier, String lineSuffix) {
		this.classModifier = classModifier
		this.lineSuffix = lineSuffix
		this.methodModifier = methodModifier
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
}