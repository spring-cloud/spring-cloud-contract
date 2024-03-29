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

package org.springframework.cloud.contract.stubrunner.junit4

import java.util.concurrent.TimeUnit

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import org.springframework.cloud.contract.stubrunner.junit.StubRunnerRule
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.converter.YamlContract
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender
/**
 * @author Marcin Grzejszczak
 */
class StubRunnerRuleCustomMsgVerifierSpec extends Specification {

	@BeforeClass
	@AfterClass
	void setupProps() {
		System.clearProperty("stubrunner.repository.root")
		System.clearProperty("stubrunner.classifier")
	}

	@ClassRule
	@Shared
	StubRunnerRule rule = new StubRunnerRule()
			.stubsMode(StubRunnerProperties.StubsMode.REMOTE)
			.repoRoot(StubRunnerRuleCustomMsgVerifierSpec.getResource("/m2repo/repository").toURI().toString())
			.downloadStub("org.springframework.cloud.contract.verifier.stubs", "bootService")
			.messageVerifierSender(new MyMessageVerifier())
			.messageVerifierReceiver(new MyMessageVerifier())

	def 'should use the provided message verifier in the junit rule'() {
		when:
			rule.trigger()
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.contains("Failed to send a message with headers")
		when:
			rule.trigger("return_book_1")
		then:
			e = thrown(IllegalStateException)
			e.message.contains("Failed to send a message with headers")
		when:
			rule.trigger("bootService", "return_book_1")
		then:
			e = thrown(IllegalStateException)
			e.message.contains("Failed to send a message with headers")
	}

	static class MyMessageVerifier implements MessageVerifierSender, MessageVerifierReceiver {

		@Override
		void send(Object message, String destination, YamlContract contract) {
			throw new IllegalStateException("Failed to send a message")
		}

		@Override
		Object receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
			throw new IllegalStateException("Failed to receive a message with timeout")
		}

		@Override
		Object receive(String destination, YamlContract contract) {
			throw new IllegalStateException("Failed to receive a message")
		}

		@Override
		void send(Object payload, Map headers, String destination, YamlContract contract) {
			throw new IllegalStateException("Failed to send a message with headers")
		}
	}
}
