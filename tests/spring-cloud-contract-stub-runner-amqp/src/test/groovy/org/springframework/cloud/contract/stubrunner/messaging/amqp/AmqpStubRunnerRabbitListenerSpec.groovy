/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.messaging.amqp

import org.mockito.ArgumentCaptor
import org.mockito.Captor
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.cloud.contract.stubrunner.StubTrigger
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [AmqpMessagingApplication], loader = SpringBootContextLoader)
@AutoConfigureStubRunner
@ActiveProfiles("listener")
class AmqpStubRunnerRabbitListenerSpec extends Specification {

	@Autowired
	StubTrigger stubTrigger

	@Autowired
	MessageSubscriberRabbitListener messageSubscriber

	@Captor
	ArgumentCaptor<Person> personArgumentCaptor

	def "should trigger stub amqp message consumed by annotated listener"() {
		when:
			stubTrigger.trigger("contract-test.person.created.event")
		then:
			messageSubscriber.person != null
			messageSubscriber.person.name != null
	}
}
