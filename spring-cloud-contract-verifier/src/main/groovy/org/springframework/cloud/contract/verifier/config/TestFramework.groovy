/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.config

/**
 * Contains main building blocks for a test class for the given framework
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
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