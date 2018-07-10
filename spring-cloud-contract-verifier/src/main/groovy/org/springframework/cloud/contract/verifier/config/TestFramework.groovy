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

import org.springframework.cloud.contract.verifier.config.framework.CustomDefinition
import org.springframework.cloud.contract.verifier.config.framework.JUnit5Definition
import org.springframework.cloud.contract.verifier.config.framework.JUnitDefinition
import org.springframework.cloud.contract.verifier.config.framework.SpockDefinition
import org.springframework.cloud.contract.verifier.config.framework.TestFrameworkDefinition

/**
 * Contains main building blocks for a test class for the given framework
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Olga Maciaszek-Sharma
 *
 * @since 1.0.0
 */
enum TestFramework {

	SPOCK(new SpockDefinition()),
	JUNIT(new JUnitDefinition()),
	JUNIT5(new JUnit5Definition()),
	CUSTOM(new CustomDefinition())

	@Delegate
	private final TestFrameworkDefinition testFrameworkDefinition


	TestFramework(TestFrameworkDefinition testFrameworkDefinition) {
		this.testFrameworkDefinition = testFrameworkDefinition
	}

	/**
	 * @deprecated use {@link #TestFramework(TestFrameworkDefinition)}
	 * @param classModifier
	 * @param methodModifier
	 * @param lineSuffix
	 * @param classExtension
	 * @param classNameSuffix
	 * @param ignoreClass
	 * @param orderAnnotationImports
	 * @param orderAnnotation
	 */
	@Deprecated
	TestFramework(String classModifier, String methodModifier, String lineSuffix, String classExtension, String classNameSuffix,
	              String ignoreClass, List<String> orderAnnotationImports, String orderAnnotation) {
		testFrameworkDefinition = new TestFrameworkDefinition() {

			@Override
			String getClassModifier() {
				return classModifier
			}

			@Override
			String getMethodModifier() {
				return methodModifier
			}

			@Override
			String getLineSuffix() {
				return lineSuffix
			}

			@Override
			String getClassExtension() {
				return classExtension
			}

			@Override
			String getClassNameSuffix() {
				return classNameSuffix
			}

			@Override
			String getIgnoreClass() {
				return ignoreClass
			}

			@Override
			List<String> getOrderAnnotationImports() {
				return orderAnnotationImports
			}

			@Override
			String getOrderAnnotation() {
				return orderAnnotation
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
	}
}