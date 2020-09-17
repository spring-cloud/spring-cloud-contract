/*
 * Copyright 2018-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.junit;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class StubRunnerJUnit5ExtensionCustomMessageVerifierTests {

	// Visible for testing
	@RegisterExtension
	static StubRunnerExtension stubRunnerExtension = new StubRunnerExtension()
			.stubsMode(StubRunnerProperties.StubsMode.REMOTE).repoRoot(repoRoot())
			.downloadStub("org.springframework.cloud.contract.verifier.stubs", "bootService")
			.messageVerifier(new MyMessageVerifier());

	@BeforeAll
	@AfterAll
	static void setupProps() {
		System.clearProperty("stubrunner.repository.root");
		System.clearProperty("stubrunner.classifier");
	}

	private static String repoRoot() {
		try {
			return StubRunnerRuleCustomPortJUnitTest.class.getResource("/m2repo/repository/").toURI().toString();
		}
		catch (Exception e) {
			return "";
		}
	}

	@Test
	void should_use_provided_message_verifier_in_junit5_extension() {
		IllegalStateException emptyTriggerException = assertThrows(IllegalStateException.class,
				() -> stubRunnerExtension.trigger());
		assertThat(emptyTriggerException.getMessage()).contains("Failed to send a message with headers");
		IllegalStateException wrongLabelException = assertThrows(IllegalStateException.class,
				() -> stubRunnerExtension.trigger("return_book_1"));
		assertThat(wrongLabelException.getMessage()).contains("Failed to send a message with headers");
		IllegalStateException wrongLabelWithIvyNotation = assertThrows(IllegalStateException.class,
				() -> stubRunnerExtension.trigger("bootService", "return_book_1"));
		assertThat(wrongLabelWithIvyNotation.getMessage()).contains("Failed to send a message with headers");
	}

	static class MyMessageVerifier implements MessageVerifier {

		@Override
		public void send(Object message, String destination, YamlContract contract) {
			throw new IllegalStateException("Failed to send a message");
		}

		@Override
		public Object receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
			throw new IllegalStateException("Failed to receive a message with timeout");
		}

		@Override
		public Object receive(String destination, YamlContract contract) {
			throw new IllegalStateException("Failed to receive a message");
		}

		@Override
		public void send(Object payload, Map headers, String destination, YamlContract contract) {
			throw new IllegalStateException("Failed to send a message with headers");
		}

	}

}
