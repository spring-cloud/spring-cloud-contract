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

package org.springframework.cloud.contract.stubrunner.provider.wiremock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.springframework.cloud.contract.stubrunner.HttpServerStubConfigurer;

/**
 * Typed implementation of {@link HttpServerStubConfigurer} for WireMock's
 * {@link WireMockConfiguration}.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public class WireMockHttpServerStubConfigurer
		implements HttpServerStubConfigurer<WireMockConfiguration> {

	@Override
	public boolean isAccepted(Object httpStubConfiguration) {
		return httpStubConfiguration instanceof WireMockConfiguration;
	}

}
