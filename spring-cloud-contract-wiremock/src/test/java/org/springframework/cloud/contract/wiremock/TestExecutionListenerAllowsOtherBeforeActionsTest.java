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

package org.springframework.cloud.contract.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.TestExecutionListenerAllowsOtherBeforeActionsTest.ApplicationEventListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = WiremockTestsApplication.class,
		properties = "app.baseUrl=http://localhost:${wiremock.server.port}", webEnvironment = WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0)
@DirtiesContext
@Import(ApplicationEventListener.class)
class TestExecutionListenerAllowsOtherBeforeActionsTest {

	private static boolean beforeAllRunSuccessful;
	private static boolean applicationContextInitialized;

	@Autowired
	private Service service;

	@BeforeAll
	static void checkIfApplicationContextNotInitialized() {
		beforeAllRunSuccessful = !applicationContextInitialized;
	}

	@Test
	void should_run_before_all_action_before_application_context_is_available() {
		// check that beforeAll method was actually run before the application context was initialized
		assertThat(beforeAllRunSuccessful).isTrue();
		// check that wiremock instance is running
		stubFor(get(urlEqualTo("/test"))
				.willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

	public static class ApplicationEventListener implements ApplicationListener<ContextRefreshedEvent> {
		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			applicationContextInitialized = true;
		}
	}
}
