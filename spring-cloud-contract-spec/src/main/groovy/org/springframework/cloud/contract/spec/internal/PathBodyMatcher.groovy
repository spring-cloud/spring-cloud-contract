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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * @author Marcin Grzejszczak
 */
@ToString(includePackage = false)
@Canonical
@CompileStatic
class PathBodyMatcher implements BodyMatcher {

	String path
	MatchingTypeValue matchingTypeValue

	@Override
	MatchingType matchingType() {
		return this.matchingTypeValue.type
	}

	@Override
	String path() {
		return this.path
	}

	@Override
	Object value() {
		return this.matchingTypeValue.value
	}

	@Override
	Integer minTypeOccurrence() {
		return this.matchingTypeValue.minTypeOccurrence
	}

	@Override
	Integer maxTypeOccurrence() {
		return this.matchingTypeValue.maxTypeOccurrence
	}
}
