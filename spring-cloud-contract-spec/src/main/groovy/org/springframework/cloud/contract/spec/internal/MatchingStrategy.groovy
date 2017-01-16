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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString;

/**
 * Represents a matching strategy for a JSON
 *
 * @since 1.0.0
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@CompileStatic
class MatchingStrategy extends DslProperty {

	Type type
	JSONCompareMode jsonCompareMode

	MatchingStrategy(Object value, Type type) {
		this(value, type, null)
	}

	MatchingStrategy(Object value, Type type, JSONCompareMode jsonCompareMode) {
		super(value)
		this.type = type
		this.jsonCompareMode = jsonCompareMode
	}

	MatchingStrategy(DslProperty value, Type type) {
		this(value, type, null)
	}

	MatchingStrategy(DslProperty value, Type type, JSONCompareMode jsonCompareMode) {
		super(value.clientValue, value.serverValue)
		this.type = type
		this.jsonCompareMode = jsonCompareMode
	}

	enum Type {
		EQUAL_TO("equalTo"), CONTAINS("containing"), MATCHING("matching"), NOT_MATCHING("notMatching"),
		EQUAL_TO_JSON("equalToJson"), EQUAL_TO_XML("equalToXml"), ABSENT("absent")

		final String name

		Type(name) {
			this.name = name
		}
	}

}