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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public class StubRunnerJUnit5ExtensionExceptionThrowingTests {

	@RegisterExtension
	static StubRunnerExtension stubRunnerExtension = new StubRunnerExtension()
			.stubsMode(StubRunnerProperties.StubsMode.REMOTE).repoRoot(repoRoot())
			.downloadStub("org.springframework.cloud.contract.verifier.stubs", "bootService");

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
	void should_throw_exception_when_no_message_verifier_was_passed_and_message_related_method_was_triggered() {
		UnsupportedOperationException emptyTriggerException = assertThrows(UnsupportedOperationException.class,
				() -> stubRunnerExtension.trigger());
		UnsupportedOperationException wrongLabelException = assertThrows(UnsupportedOperationException.class,
				() -> stubRunnerExtension.trigger("return_book_1"));
		UnsupportedOperationException wrongLabelWithIvyNotation = assertThrows(UnsupportedOperationException.class,
				() -> stubRunnerExtension.trigger("bootService", "return_book_1"));
	}

}
