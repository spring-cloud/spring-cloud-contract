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
 * Defines elements characteristic of Spock test framework to be used during test class construction.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
class SpockDefinition implements TestFrameworkDefinition {

	@Override
	String getClassModifier() {
		return ''
	}

	@Override
	String getMethodModifier() {
		return 'def '
	}

	@Override
	String getLineSuffix() {
		return ''
	}

	@Override
	String getClassExtension() {
		return '.groovy'
	}

	@Override
	String getClassNameSuffix() {
		return 'Spec'
	}

	@Override
	String getIgnoreClass() {
		return 'spock.lang.Ignore'
	}

	@Override
	List<String> getOrderAnnotationImports() {
		return ['spock.lang.Stepwise']
	}

	@Override
	String getOrderAnnotation() {
		return '@Stepwise'
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
		throw new UnsupportedOperationException('Not available in Spock.')
	}
}
