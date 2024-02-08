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

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author Tim Ysewyn
 */
class HttpMethodsTests {

	@Test
	public void GET() {
		BDDAssertions.then(HttpMethods.GET).isEqualTo("GET");
	}

	@Test
	public void HEAD() {
		BDDAssertions.then(HttpMethods.HEAD).isEqualTo("HEAD");
	}

	@Test
	public void POST() {
		BDDAssertions.then(HttpMethods.POST).isEqualTo("POST");
	}

	@Test
	public void PUT() {
		BDDAssertions.then(HttpMethods.PUT).isEqualTo("PUT");
	}

	@Test
	public void PATCH() {
		BDDAssertions.then(HttpMethods.PATCH).isEqualTo("PATCH");
	}

	@Test
	public void DELETE() {
		BDDAssertions.then(HttpMethods.DELETE).isEqualTo("DELETE");
	}

	@Test
	public void OPTIONS() {
		BDDAssertions.then(HttpMethods.OPTIONS).isEqualTo("OPTIONS");
	}

	@Test
	public void TRACE() {
		BDDAssertions.then(HttpMethods.TRACE).isEqualTo("TRACE");
	}

}
