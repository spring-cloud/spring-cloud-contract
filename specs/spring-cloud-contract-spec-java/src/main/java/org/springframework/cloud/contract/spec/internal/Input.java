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

package org.springframework.cloud.contract.spec.internal;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an input for messaging. The input can be a message or some action inside the
 * application.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
public class Input extends Common implements RegexCreatingProperty<ClientDslProperty> {

	private static final Log log = LogFactory.getLog(Input.class);

	private ClientPatternValueDslProperty property = new ClientPatternValueDslProperty();

	private DslProperty<String> messageFrom;

	private ExecutionProperty triggeredBy;

	private Headers messageHeaders = new Headers();

	private BodyType messageBody;

	private ExecutionProperty assertThat;

	private BodyMatchers bodyMatchers;

	public Input() {
	}

	public Input(Input input) {
		this.messageFrom = input.getMessageFrom();
		this.messageHeaders = input.getMessageHeaders();
		this.messageBody = input.getMessageBody();
	}

	/**
	 * Name of a destination from which message would come to trigger action in the
	 * system.
	 * @param messageFrom message destination
	 */
	public void messageFrom(String messageFrom) {
		this.messageFrom = new DslProperty<>(messageFrom);
	}

	/**
	 * Name of a destination from which message would come to trigger action in the
	 * system.
	 * @param messageFrom message destination
	 */
	public void messageFrom(DslProperty messageFrom) {
		this.messageFrom = messageFrom;
	}

	/**
	 * Function that needs to be executed to trigger action in the system.
	 * @param triggeredBy method name that triggers the message
	 */
	public void triggeredBy(String triggeredBy) {
		this.triggeredBy = new ExecutionProperty(triggeredBy);
	}

	public BodyType messageBody(Object bodyAsValue) {
		this.messageBody = new BodyType(bodyAsValue);
		return this.messageBody;
	}

	public DslProperty value(ClientDslProperty client) {
		Object dynamicValue = client.getClientValue();
		Object concreteValue = client.getServerValue();
		if (dynamicValue instanceof RegexProperty) {
			return ((RegexProperty) dynamicValue).dynamicClientConcreteProducer();
		}
		return new DslProperty(dynamicValue, concreteValue);
	}

	public DslProperty value(RegexProperty prop) {
		return value(client(prop));
	}

	public DslProperty $(RegexProperty prop) {
		return value(client(prop));
	}

	public DslProperty $(ClientDslProperty client) {
		return value(client);
	}

	@Override
	public RegexProperty regexProperty(Object object) {
		return new RegexProperty(object).dynamicClientConcreteProducer();
	}

	public void assertThat(String assertThat) {
		this.assertThat = new ExecutionProperty(assertThat);
	}

	public ClientPatternValueDslProperty getProperty() {
		return property;
	}

	public void setProperty(ClientPatternValueDslProperty property) {
		this.property = property;
	}

	public DslProperty<String> getMessageFrom() {
		return messageFrom;
	}

	public void setMessageFrom(DslProperty<String> messageFrom) {
		this.messageFrom = messageFrom;
	}

	public ExecutionProperty getTriggeredBy() {
		return triggeredBy;
	}

	public void setTriggeredBy(ExecutionProperty triggeredBy) {
		this.triggeredBy = triggeredBy;
	}

	public Headers getMessageHeaders() {
		return messageHeaders;
	}

	public void setMessageHeaders(Headers messageHeaders) {
		this.messageHeaders = messageHeaders;
	}

	public BodyType getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(BodyType messageBody) {
		this.messageBody = messageBody;
	}

	public ExecutionProperty getAssertThat() {
		return assertThat;
	}

	public void setAssertThat(ExecutionProperty assertThat) {
		this.assertThat = assertThat;
	}

	public BodyMatchers getBodyMatchers() {
		return bodyMatchers;
	}

	public void setBodyMatchers(BodyMatchers bodyMatchers) {
		this.bodyMatchers = bodyMatchers;
	}

	@Override
	public ClientDslProperty anyAlphaUnicode() {
		return property.anyAlphaUnicode();
	}

	@Override
	public ClientDslProperty anyAlphaNumeric() {
		return property.anyAlphaNumeric();
	}

	@Override
	public ClientDslProperty anyNumber() {
		return property.anyNumber();
	}

	@Override
	public ClientDslProperty anyInteger() {
		return property.anyInteger();
	}

	@Override
	public ClientDslProperty anyPositiveInt() {
		return property.anyPositiveInt();
	}

	@Override
	public ClientDslProperty anyDouble() {
		return property.anyDouble();
	}

	@Override
	public ClientDslProperty anyHex() {
		return property.anyHex();
	}

	@Override
	public ClientDslProperty aBoolean() {
		return property.aBoolean();
	}

	@Override
	public ClientDslProperty anyIpAddress() {
		return property.anyIpAddress();
	}

	@Override
	public ClientDslProperty anyHostname() {
		return property.anyHostname();
	}

	@Override
	public ClientDslProperty anyEmail() {
		return property.anyEmail();
	}

	@Override
	public ClientDslProperty anyUrl() {
		return property.anyUrl();
	}

	@Override
	public ClientDslProperty anyHttpsUrl() {
		return property.anyHttpsUrl();
	}

	@Override
	public ClientDslProperty anyUuid() {
		return property.anyUuid();
	}

	@Override
	public ClientDslProperty anyDate() {
		return property.anyDate();
	}

	@Override
	public ClientDslProperty anyDateTime() {
		return property.anyDateTime();
	}

	@Override
	public ClientDslProperty anyTime() {
		return property.anyTime();
	}

	@Override
	public ClientDslProperty anyIso8601WithOffset() {
		return property.anyIso8601WithOffset();
	}

	@Override
	public ClientDslProperty anyNonBlankString() {
		return property.anyNonBlankString();
	}

	@Override
	public ClientDslProperty anyNonEmptyString() {
		return property.anyNonEmptyString();
	}

	@Override
	public ClientDslProperty anyOf(String... values) {
		return property.anyOf(values);
	}

	/**
	 * The message headers part of the contract.
	 * @param consumer function to manipulate the message headers
	 */
	public void messageHeaders(Consumer<Headers> consumer) {
		this.messageHeaders = new Headers();
		consumer.accept(this.messageHeaders);
	}

	/**
	 * The stub matchers part of the contract.
	 * @param consumer function to manipulate the body headers
	 * @deprecated Deprecated in favor of bodyMatchers to support other future
	 * bodyMatchers too
	 */
	@Deprecated
	public void stubMatchers(Consumer<BodyMatchers> consumer) {
		log.warn("stubMatchers method is deprecated. Please use bodyMatchers instead");
		bodyMatchers(consumer);
	}

	/**
	 * The stub matchers part of the contract.
	 * @param consumer function to manipulate the message headers
	 * @deprecated Deprecated in favor of bodyMatchers to support other future
	 * bodyMatchers too
	 */
	public void bodyMatchers(Consumer<BodyMatchers> consumer) {
		this.bodyMatchers = new BodyMatchers();
		consumer.accept(this.bodyMatchers);
	}

	/**
	 * The message headers part of the contract.
	 * @param consumer function to manipulate the message headers
	 */
	public void messageHeaders(@DelegatesTo(Headers.class) Closure consumer) {
		this.messageHeaders = new Headers();
		consumer.setDelegate(this.messageHeaders);
		consumer.call();
	}

	/**
	 * The stub matchers part of the contract.
	 * @param consumer function to manipulate the body headers
	 * @deprecated Deprecated in favor of bodyMatchers to support other future
	 * bodyMatchers too
	 */
	@Deprecated
	public void stubMatchers(@DelegatesTo(BodyMatchers.class) Closure consumer) {
		log.warn("stubMatchers method is deprecated. Please use bodyMatchers instead");
		bodyMatchers(consumer);
	}

	/**
	 * The stub matchers part of the contract.
	 * @param consumer function to manipulate the message headers
	 * @deprecated Deprecated in favor of bodyMatchers to support other future
	 * bodyMatchers too
	 */
	public void bodyMatchers(@DelegatesTo(BodyMatchers.class) Closure consumer) {
		this.bodyMatchers = new BodyMatchers();
		consumer.setDelegate(this.bodyMatchers);
		consumer.call();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Input input = (Input) o;
		return Objects.equals(messageFrom, input.messageFrom)
				&& Objects.equals(triggeredBy, input.triggeredBy)
				&& Objects.equals(messageHeaders, input.messageHeaders)
				&& Objects.equals(messageBody, input.messageBody)
				&& Objects.equals(assertThat, input.assertThat)
				&& Objects.equals(bodyMatchers, input.bodyMatchers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(messageFrom, triggeredBy, messageHeaders, messageBody,
				assertThat, bodyMatchers);
	}

	@Override
	public String toString() {
		return "Input{\n\tmessageFrom=" + messageFrom + ", \n\ttriggeredBy=" + triggeredBy
				+ ", \n\tmessageHeaders=" + messageHeaders + ", \n\tmessageBody="
				+ messageBody + ", \n\tassertThat=" + assertThat + ", \n\tbodyMatchers="
				+ bodyMatchers + "} \n\t" + super.toString();
	}

	public static class BodyType extends DslProperty {

		public BodyType(Object clientValue, Object serverValue) {
			super(clientValue, serverValue);
		}

		public BodyType(Object singleValue) {
			super(singleValue);
		}

	}

	private class ClientPatternValueDslProperty
			extends PatternValueDslProperty<ClientDslProperty> {

		@Override
		protected ClientDslProperty createProperty(Pattern pattern,
				Object generatedValue) {
			return new ClientDslProperty(pattern, generatedValue);
		}

	}

}
