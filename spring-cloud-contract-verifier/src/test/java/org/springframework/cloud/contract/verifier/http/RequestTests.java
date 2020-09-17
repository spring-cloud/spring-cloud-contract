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

import java.util.AbstractMap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class RequestTests {

	@Test
	void should_override_entries_when_using_builder_from_request() {
		Request request = Request.given()
							.delete("/foo")
							.header("header", "header-value")
							.cookie("cookie", "cookie-value")
							.queryParam("query", "param-value")
							.protocol(ContractVerifierHttpMetaData.Protocol.HTTP_1_1)
							.scheme(ContractVerifierHttpMetaData.Scheme.HTTPS)
							.build();

		Request changedRequest = Request.from(request)
									.path("/bar")
									.header("header-foo", "header-bar")
									.cookie("cookie-foo", "cookie-bar")
									.queryParam("query-foo", "query-bar")
									.build();

		then(changedRequest.path()).isEqualTo("/bar");
		then(changedRequest.method().name()).isEqualTo("DELETE");
		then(changedRequest.headers()).containsEntry("header-foo", "header-bar").containsEntry("header", "header-value");
		then(changedRequest.cookies()).containsEntry("cookie-foo", "cookie-bar").containsEntry("cookie", "cookie-value");
		then(changedRequest.queryParams()).contains(new AbstractMap.SimpleEntry<>("query", "param-value"), new AbstractMap.SimpleEntry<>("query-foo", "query-bar"));
		then(changedRequest.protocol()).isEqualTo(ContractVerifierHttpMetaData.Protocol.HTTP_1_1);
	}
}
