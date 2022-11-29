/*
 * Copyright 2016-2020 the original author or authors.
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

package com.example.loan;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marius Bogoevici
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = "spring.cloud.stream.bindings.input.destination=sensor-data", classes = {MessageConsumedTests.Config.class, Application.class})
@AutoConfigureStubRunner(ids = "com.example:http-server-dsl:0.0.1", stubsMode = StubRunnerProperties.StubsMode.LOCAL)
public class MessageConsumedTests {

	@Autowired
	StubTrigger stubTrigger;

	@Autowired
	Listener listener;

	@Test
	public void testMessageConsumed() throws Exception {
		int count = this.listener.getCount();
		stubTrigger.trigger("sensor1");
		assertThat(this.listener.getCount()).isEqualTo(count + 1);
	}

	@TestConfiguration
	@ImportAutoConfiguration(TestChannelBinderConfiguration.class)
	static class Config {

	}
}
