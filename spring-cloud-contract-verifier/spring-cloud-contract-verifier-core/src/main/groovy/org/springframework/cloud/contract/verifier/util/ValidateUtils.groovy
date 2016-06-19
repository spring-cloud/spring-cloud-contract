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

package org.springframework.cloud.contract.verifier.util

import groovy.transform.TypeChecked
import org.springframework.cloud.contract.verifier.dsl.internal.DslProperty
import org.springframework.cloud.contract.verifier.dsl.internal.MatchingStrategy

import java.util.regex.Pattern

import static org.springframework.cloud.contract.verifier.dsl.internal.MatchingStrategy.Type.ABSENT
import static org.springframework.cloud.contract.verifier.dsl.internal.MatchingStrategy.Type.EQUAL_TO

/**
 * Checks the validity of DSL entries.
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @since 1.0.0
 */
@TypeChecked
class ValidateUtils {

	static Object validateServerValueIsAvailable(Object value) {
		validateServerValueIsAvailable(value, "Server value")
		return value
	}

	static Object validateServerValueIsAvailable(Object value, String msg) {
		validateServerValue(value, msg)
		return value
	}

	static void validateServerValue(Pattern pattern, String msg) {
		throw new IllegalStateException("$msg can't be a pattern for the server side")
	}

	static List ALLOWED_MATCHING_TYPES_ON_SERVER_SIDE = [EQUAL_TO, ABSENT]

	static void validateServerValue(MatchingStrategy matchingStrategy, String msg) {
		if (!ALLOWED_MATCHING_TYPES_ON_SERVER_SIDE.contains(matchingStrategy.type)) {
			throw new IllegalStateException("$msg can't be of a matching type: $matchingStrategy.type for the server side")
		}
		validateServerValue(matchingStrategy.serverValue, msg)
	}

	static void validateServerValue(DslProperty value, String msg) {
		validateServerValue(value.serverValue, msg)
	}

	static void validateServerValue(Object value, String msg) {
		// OK
	}

}
