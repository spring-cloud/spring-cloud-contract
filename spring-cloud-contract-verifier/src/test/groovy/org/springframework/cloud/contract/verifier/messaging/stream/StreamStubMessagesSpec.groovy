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
							input: new BindingProperties(destination: "verifications"),
							output: new BindingProperties(destination: "verifications"),
					]
			)
			MessageCollector collector = Stub(MessageCollector)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			StreamStubMessages messages = new StreamStubMessages(applicationContext)
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
							input: new BindingProperties(destination: "verifications"),
							output: new BindingProperties(destination: "verifications"),
					]
			)
			MessageCollector collector = Stub(MessageCollector)
			MessageChannel channel = Stub(MessageChannel)
		and:
			applicationContext.getBean(BindingServiceProperties) >> properties
			applicationContext.getBean(MessageCollector) >> collector
		and:
			StreamStubMessages messages = new StreamStubMessages(applicationContext)
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
			StreamStubMessages messages = new StreamStubMessages(applicationContext)
		when:
			messageInteraction(messages)
		then:
			1 * applicationContext.getBean("foo", MessageChannel) >> channel
		where:
			messageInteraction << [ { StreamStubMessages stream -> stream.send("foo", [:], "verifications")},
									{ StreamStubMessages stream -> stream.receive("verifications")}]
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
			StreamStubMessages messages = new StreamStubMessages(applicationContext)
		when:
			messageInteraction(messages)
		then:
			1 * applicationContext.getBean("verifications", MessageChannel) >> channel
		where:
			messageInteraction << [ { StreamStubMessages stream -> stream.send("foo", [:], "verifications")},
									{ StreamStubMessages stream -> stream.receive("verifications")}]
	}
}
