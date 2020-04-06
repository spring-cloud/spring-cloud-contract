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

package org.springframework.cloud.contract.spec.internal;

/**
 * Represents the type of matching the should be done against the body of the request or
 * response.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @author Olga Maciaszek-Sharma
 * @since 1.0.3
 */
public enum MatchingType {

	/**
	 * Verification by equality.
	 */
	EQUALITY,
	/**
	 * Verification by type - is the type received in a body for the given path is of the
	 * same type. If it's a collection you can verify number of occurrences.
	 */
	TYPE,
	/**
	 * Special version of regex for date check.
	 */
	DATE,
	/**
	 * Special version of regex for time check.
	 */
	TIME,
	/**
	 * Special version of regex for timestamp check.
	 */
	TIMESTAMP,
	/**
	 * Verification if the value for the given path matches the provided regex.
	 */
	REGEX,
	/**
	 * The user can provide custom command to execute.
	 */
	COMMAND,
	/**
	 * Verification if the value for the given path is null.
	 */
	NULL;

	public static boolean regexRelated(MatchingType type) {
		return !type.equals(EQUALITY) && !type.equals(TYPE) && !type.equals(COMMAND)
				&& !type.equals(NULL);
	}

}
