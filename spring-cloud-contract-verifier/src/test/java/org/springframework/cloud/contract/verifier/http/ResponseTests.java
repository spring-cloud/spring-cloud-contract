/*
 * Copyright 2020-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class ResponseTests {

	@Test
	void should_override_entries_when_using_builder_from_response() {
		Response response = Response.builder()
							.statusCode(200)
							.header("header", "header-value")
							.cookie("cookie", "cookie-value")
							.build();

		Response changedResponse = Response.from(response)
									.header("header-foo", "header-bar")
									.cookie("cookie-foo", "cookie-bar")
									.build();

		then(changedResponse.statusCode()).isEqualTo(200);
		then(changedResponse.headers()).containsEntry("header-foo", "header-bar").containsEntry("header", "header-value");
		then(changedResponse.cookies()).containsEntry("cookie-foo", "cookie-bar").containsEntry("cookie", "cookie-value");
	}
}
