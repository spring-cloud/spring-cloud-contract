/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
 * Defines elements characteristic of TestNG test framework to be used during test class construction.
 *
 * @author André Hoffmann
 *
 * @since 2.2.0
 */
@CompileStatic
class TestNGDefinition implements TestFrameworkDefinition {

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
		throw new UnsupportedOperationException('There is no @Ignore annotation for TestNG. A test can be disabled directly in the @Test annotation')
	}

	@Override
	List<String> getOrderAnnotationImports() {
		throw new UnsupportedOperationException('Not implemented yet in TestNG')
	}

	@Override
	String getOrderAnnotation() {
		throw new UnsupportedOperationException('Not implemented yet in TestNG')
	}

	@Override
	String getIgnoreAnnotation() {
		throw new UnsupportedOperationException('There is no @Ignore annotation for TestNG. A test can be disabled directly in the @Test annotation')
	}

	@Override
	boolean annotationLevelRules() {
		return false
	}

	@Override
	String getRuleAnnotation(String annotationValue) {
		throw new UnsupportedOperationException('Not available in TestNG.')
	}
}
