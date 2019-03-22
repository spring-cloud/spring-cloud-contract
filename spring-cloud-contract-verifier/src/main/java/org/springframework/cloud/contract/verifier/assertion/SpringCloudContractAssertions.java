/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.assertion;

import org.assertj.core.api.Assertions;

/**
 * Assertions used by the generated tests.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class SpringCloudContractAssertions extends Assertions {

	/**
	 * Creates a new instance of <code>{@link CollectionAssert}</code>.
	 * @param <ELEMENT> type to assert
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	public static <ELEMENT> CollectionAssert<ELEMENT> assertThat(
			Iterable<? extends ELEMENT> actual) {
		return new CollectionAssert<>(actual);
	}

}
