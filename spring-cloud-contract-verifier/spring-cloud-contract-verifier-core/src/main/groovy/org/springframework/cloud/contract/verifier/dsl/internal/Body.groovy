/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a body of a request / response or a message
 *
 * @since 1.0.0
 */
@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class Body extends DslProperty {

	Body(Map<String, DslProperty> body) {
		super(extractValue(body, { DslProperty p -> p.clientValue}), extractValue(body, {DslProperty p -> p.serverValue}))
	}

	private static Map<String, Object> extractValue(Map<String, DslProperty> body, Closure valueProvider) {
		body.collectEntries { Map.Entry<String, DslProperty> entry ->
			[(entry.key): valueProvider(entry.value)]
		} as Map<String, Object>
	}

	Body(List<DslProperty> bodyAsList) {
		super(bodyAsList.collect { DslProperty p -> p.clientValue }, bodyAsList.collect { DslProperty p -> p.serverValue })
	}

	Body(Object bodyAsValue) {
		this("${bodyAsValue}")
	}

	Body(GString bodyAsValue) {
		super(bodyAsValue, bodyAsValue)
	}

	Body(DslProperty bodyAsValue) {
		super(bodyAsValue.clientValue, bodyAsValue.serverValue)
	}

	Body(MatchingStrategy matchingStrategy) {
		super(matchingStrategy, matchingStrategy)
	}


}
