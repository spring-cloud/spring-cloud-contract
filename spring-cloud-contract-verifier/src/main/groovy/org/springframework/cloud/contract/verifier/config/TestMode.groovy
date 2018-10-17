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

package org.springframework.cloud.contract.verifier.config

/**
 * Provides different testing modes
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
enum TestMode {
	/**
	 * Uses Spring's MockMvc with Rest Assured 2.x
	 */
	MOCKMVC,

	/**
	 * Uses direct HTTP invocations with Rest Assured 2.x
	 */
	EXPLICIT,

	/**
	 * Uses JAX-RS client
	 */
	JAXRSCLIENT,

	/**
	 * Uses Spring's reactive WebTestClient
	 */
	WEBTESTCLIENT
}