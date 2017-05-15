/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sink;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.stubrunner.StubTrigger;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.support.management.MessageChannelMetrics;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marius Bogoevici
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = "spring.cloud.stream.bindings.input.destination=sensor-data")
@AutoConfigureStubRunner
public class MessageConsumedTests {

	@Autowired
	StubTrigger stubTrigger;

	@Autowired
	@Qualifier(Sink.INPUT)
	SubscribableChannel input;

	@Test
	public void testMessageConsumed() throws Exception {
		int count = ((MessageChannelMetrics) input).getSendCount();
		stubTrigger.trigger("sensor1");
		assertThat(((MessageChannelMetrics) input).getSendCount()).isEqualTo(count + 1);
	}
}
