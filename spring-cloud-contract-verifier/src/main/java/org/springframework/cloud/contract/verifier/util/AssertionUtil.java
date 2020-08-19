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

package org.springframework.cloud.contract.verifier.util;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

public final class AssertionUtil {

	private static boolean NON_STRICT = false;

	private AssertionUtil() {
		throw new IllegalStateException("You shouldn't instantiate the utility class");
	}

	public static void assertThatJsonsAreEqual(String expected, String actual) {
		try {
			JSONAssert.assertEquals(expected, actual, NON_STRICT);
		}
		catch (JSONException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
