/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.config.framework

import groovy.transform.CompileStatic

/**
 * Defines elements characteristic of JUnit5 test framework to be used during test class construction.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
class JUnit5Definition implements TestFrameworkDefinition {

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
		'.java'
	}

	@Override
	String getClassNameSuffix() {
		return 'Test'
	}

	@Override
	String getIgnoreClass() {
		return 'org.junit.jupiter.api.Disabled'
	}

	@Override
	List<String> getOrderAnnotationImports() {
		throw new UnsupportedOperationException('Not implemented yet in JUnit5 - https://github.com/junit-team/junit5/issues/48')
	}

	@Override
	String getOrderAnnotation() {
		throw new UnsupportedOperationException('Not implemented yet in JUnit5 - https://github.com/junit-team/junit5/issues/48')
	}

	@Override
	String getIgnoreAnnotation() {
		return '@Disabled'
	}

	@Override
	boolean annotationLevelRules() {
		return true
	}

	@Override
	String getRuleAnnotation(String annotationValue) {
		return ("@ExtendWith(${annotationValue}.class)")
	}
}
