/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.cloud.contract.wiremock.restdocs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsWebTestClientConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentationConfigurer;

/**
 * Custom configuration for Spring RestDocs that adds a WireMock snippet (for generating
 * JSON stubs). Applied automatically if you use
 * {@link org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs @AutoConfigureRestDocs}
 * in your test case and this class is available. JSON stubs are generated and added to
 * the restdocs path under "stubs".
 *
 * @see WireMockRestDocs for a convenient entry point for customizing and asserting the
 * stub behaviour
 * @author Dave Syer
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebTestClientRestDocumentationConfigurer.class)
public class WireMockWebTestClientConfiguration
		implements RestDocsWebTestClientConfigurationCustomizer {

	private final Environment environment;

	public WireMockWebTestClientConfiguration(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void customize(WebTestClientRestDocumentationConfigurer configurer) {
		if (this.environment.getProperty("wiremock.placeholders.enabled", "true")
				.equals("true")) {
			configurer.operationPreprocessors().withResponseDefaults(
					new DynamicPortPlaceholderInserterPreprocessor());
		}
		configurer.snippets().withAdditionalDefaults(new WireMockSnippet());
	}

}
