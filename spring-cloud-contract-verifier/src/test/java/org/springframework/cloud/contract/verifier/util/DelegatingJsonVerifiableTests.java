/*
 * Copyright 2020-present the original author or authors.
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

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

class DelegatingJsonVerifiableTests {

	@Test
	void shouldReturnNullValueForNull() {
		String value = DelegatingJsonVerifiable.wrapValueWithQuotes(null);

		BDDAssertions.then(value).isNull();
	}

	@Test
	void shouldReturnQuotedValueForString() {
		String value = DelegatingJsonVerifiable.wrapValueWithQuotes("hello");

		BDDAssertions.then(value).isEqualTo("\"hello\"");
	}

	@Test
	void shouldReturnStringValueForNonString() {
		String value = DelegatingJsonVerifiable.wrapValueWithQuotes(5);

		BDDAssertions.then(value).isEqualTo("5");
	}

}
