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

package org.springframework.cloud.contract.verifier.messaging.stream

import spock.lang.Issue
import spock.lang.Specification

import org.springframework.cloud.stream.binder.test.InputDestination
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.function.StreamFunctionProperties
import org.springframework.context.ApplicationContext
/**
 * @author Marcin Grzejszczak
 */
class StreamStubMessagesWithDestinationsSpec extends Specification {

	@Issue("694")
	def "should resolve input channel if input and output have same destination and receive is called"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			StreamFunctionProperties functionProperties = new StreamFunctionProperties(definition: 'verifications')
			OutputDestination output = Mock(OutputDestination)
		and:
			applicationContext.getBean(OutputDestination) >> output
			applicationContext.getBean(StreamFunctionProperties) >> functionProperties
		and:
			StreamOutputDestinationMessageReceiver messages = new StreamOutputDestinationMessageReceiver(applicationContext)
		when:
			messages.receive("verifications")
		then:
			1 * output.receive(5000, 'verifications') >> null
	}

	@Issue("694")
	def "should resolve output channel if input and output have same destination and send is called"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			StreamFunctionProperties functionProperties = new StreamFunctionProperties(definition: 'verifications')
			InputDestination input = Mock(InputDestination)
		and:
			applicationContext.getBean(InputDestination) >> input
			applicationContext.getBean(StreamFunctionProperties) >> functionProperties
		and:
			StreamInputDestinationMessageSender messages = new StreamInputDestinationMessageSender(applicationContext)
		when:
			messages.send("foo", [:], "verifications")
		then:
			1 * input.send(_, 'verifications')
	}

	def "should resolve channel via destination for send"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			StreamFunctionProperties functionProperties = new StreamFunctionProperties(definition: 'verifications')
			InputDestination input = Mock(InputDestination)
			OutputDestination output = Mock(OutputDestination)
		and:
			applicationContext.getBean(InputDestination) >> input
			applicationContext.getBean(OutputDestination) >> output
			applicationContext.getBean(StreamFunctionProperties) >> functionProperties
		and:
			StreamStubMessages messages = new StreamStubMessages(new StreamInputDestinationMessageSender(applicationContext), new StreamOutputDestinationMessageReceiver(applicationContext))
		when:
			messages.send("foo", [:], "verifications")
		then:
			1 * input.send(_, 'verifications')
	}

	def "should resolve channel via destination for receive"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			StreamFunctionProperties functionProperties = new StreamFunctionProperties(definition: 'verifications')
			InputDestination input = Mock(InputDestination)
			OutputDestination output = Mock(OutputDestination)
		and:
			applicationContext.getBean(InputDestination) >> input
			applicationContext.getBean(OutputDestination) >> output
			applicationContext.getBean(StreamFunctionProperties) >> functionProperties
		and:
			StreamStubMessages messages = new StreamStubMessages(new StreamInputDestinationMessageSender(applicationContext), new StreamOutputDestinationMessageReceiver(applicationContext))
		when:
			messages.receive("verifications")
		then:
			1 * output.receive(5000, 'verifications') >> null
	}
}
