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

package org.springframework.cloud.contract.verifier.messaging.stream

import spock.lang.Issue
import spock.lang.Specification

import org.springframework.cloud.stream.config.BindingProperties
import org.springframework.cloud.stream.config.BindingServiceProperties
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.context.ApplicationContext
import org.springframework.messaging.MessageChannel

/**
 * @author Marcin Grzejszczak
 */
class StreamStubMessagesSpec extends Specification {

	@Issue("694")
	def "should resolve input channel if input and output have same destination and receive is called"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			BindingServiceProperties properties = new BindingServiceProperties(
					bindings: [
							input : new BindingProperties(destination: "verifications"),
							output: new BindingProperties(destination: "verifications"),
					]
			)
			MessageCollector collector = Stub(MessageCollector)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			DestinationResolver resolver = new DestinationResolver(applicationContext)
			StreamMessageCollectorMessageReceiver messages = new StreamMessageCollectorMessageReceiver(resolver, applicationContext)
		when:
			messages.receive("verifications")
		then:
			1 * applicationContext.getBean("input", MessageChannel) >> null
	}

	@Issue("694")
	def "should resolve output channel if input and output have same destination and send is called"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			BindingServiceProperties properties = new BindingServiceProperties(
					bindings: [
							input : new BindingProperties(destination: "verifications"),
							output: new BindingProperties(destination: "verifications"),
					]
			)
			MessageCollector collector = Stub(MessageCollector)
			MessageChannel channel = Stub(MessageChannel)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			DestinationResolver resolver = new DestinationResolver(applicationContext)
			StreamFromBinderMappingMessageSender messages = new StreamFromBinderMappingMessageSender(applicationContext, resolver)
		when:
			messages.send("foo", [:], "verifications")
		then:
			1 * applicationContext.getBean("output", MessageChannel) >> channel
	}

	def "should resolve channel via destination for send and receive"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			BindingServiceProperties properties = new BindingServiceProperties(
					bindings: [
							foo: new BindingProperties(destination: "verifications")
					]
			)
			MessageCollector collector = Stub(MessageCollector)
			MessageChannel channel = Stub(MessageChannel)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			DestinationResolver resolver = new DestinationResolver(applicationContext)
			StreamStubMessages messages = new StreamStubMessages(new StreamFromBinderMappingMessageSender(applicationContext, resolver), new StreamMessageCollectorMessageReceiver(resolver, applicationContext))
		when:
			messageInteraction(messages)
		then:
			1 * applicationContext.getBean("foo", MessageChannel) >> channel
		where:
			messageInteraction << [{ StreamStubMessages stream -> stream.send("foo", [:], "verifications") },
								   { StreamStubMessages stream -> stream.receive("verifications") }]
	}

	def "should resolve channel via channel name for send and receive"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			BindingServiceProperties properties = new BindingServiceProperties(
					bindings: [
							verifications: new BindingProperties(destination: "bar")
					]
			)
			MessageCollector collector = Stub(MessageCollector)
			MessageChannel channel = Stub(MessageChannel)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			DestinationResolver resolver = new DestinationResolver(applicationContext)
			StreamStubMessages messages = new StreamStubMessages(new StreamFromBinderMappingMessageSender(applicationContext, resolver), new StreamMessageCollectorMessageReceiver(resolver, applicationContext))
		when:
			messageInteraction(messages)
		then:
			1 * applicationContext.getBean("verifications", MessageChannel) >> channel
		where:
			messageInteraction << [{ StreamStubMessages stream -> stream.send("foo", [:], "verifications") },
								   { StreamStubMessages stream -> stream.receive("verifications") }]
	}

	@Issue("694")
	def "should resolve input channel if input and output have same destination and receive is called and channel name is camel case"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			BindingServiceProperties properties = new BindingServiceProperties(
					bindings: [
							input : new BindingProperties(destination: "verificationsChannel"),
							output: new BindingProperties(destination: "verificationsChannel"),
					]
			)
			MessageCollector collector = Stub(MessageCollector)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			DestinationResolver resolver = new DestinationResolver(applicationContext)
			StreamStubMessages messages = new StreamStubMessages(new StreamFromBinderMappingMessageSender(applicationContext, resolver), new StreamMessageCollectorMessageReceiver(resolver, applicationContext))
		when:
			messages.receive("verificationsChannel")
		then:
			1 * applicationContext.getBean("input", MessageChannel) >> null
	}

	@Issue("694")
	def "should resolve output channel if input and output have same destination and send is called and channel name is camel case"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			BindingServiceProperties properties = new BindingServiceProperties(
					bindings: [
							input : new BindingProperties(destination: "verificationsChannel"),
							output: new BindingProperties(destination: "verificationsChannel"),
					]
			)
			MessageCollector collector = Stub(MessageCollector)
			MessageChannel channel = Stub(MessageChannel)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			DestinationResolver resolver = new DestinationResolver(applicationContext)
			StreamStubMessages messages = new StreamStubMessages(new StreamFromBinderMappingMessageSender(applicationContext, resolver), new StreamMessageCollectorMessageReceiver(resolver, applicationContext))
		when:
			messages.send("foo", [:], "verificationsChannel")
		then:
			1 * applicationContext.getBean("output", MessageChannel) >> channel
	}

	def "should resolve channel via destination for send and receive and channel name is camel case"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			BindingServiceProperties properties = new BindingServiceProperties(
					bindings: [
							foo: new BindingProperties(destination: "verificationsChannel")
					]
			)
			MessageCollector collector = Stub(MessageCollector)
			MessageChannel channel = Stub(MessageChannel)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			DestinationResolver resolver = new DestinationResolver(applicationContext)
			StreamStubMessages messages = new StreamStubMessages(new StreamFromBinderMappingMessageSender(applicationContext, resolver), new StreamMessageCollectorMessageReceiver(resolver, applicationContext))
		when:
			messageInteraction(messages)
		then:
			1 * applicationContext.getBean("foo", MessageChannel) >> channel
		where:
			messageInteraction << [{ StreamStubMessages stream -> stream.send("foo", [:], "verificationsChannel") },
								   { StreamStubMessages stream -> stream.receive("verificationsChannel") }]
	}

	def "should resolve channel via channel name for send and receive and channel name is camel case"() {
		given:
			ApplicationContext applicationContext = Mock(ApplicationContext)
			BindingServiceProperties properties = new BindingServiceProperties(
					bindings: [
							verificationsChannel: new BindingProperties(destination: "bar")
					]
			)
			MessageCollector collector = Stub(MessageCollector)
			MessageChannel channel = Stub(MessageChannel)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			DestinationResolver resolver = new DestinationResolver(applicationContext)
			StreamStubMessages messages = new StreamStubMessages(new StreamFromBinderMappingMessageSender(applicationContext, resolver), new StreamMessageCollectorMessageReceiver(resolver, applicationContext))
		when:
			messageInteraction(messages)
		then:
			1 * applicationContext.getBean("verificationsChannel", MessageChannel) >> channel
		where:
			messageInteraction << [{ StreamStubMessages stream -> stream.send("foo", [:], "verificationsChannel") },
								   { StreamStubMessages stream -> stream.receive("verificationsChannel") }]
	}
}
