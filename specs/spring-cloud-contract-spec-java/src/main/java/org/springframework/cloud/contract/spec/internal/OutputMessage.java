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
 * Represents an output for messaging. Used for verifying the body and headers that are
 * sent.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
public class OutputMessage extends Common implements RegexCreatingProperty<ServerDslProperty> {

	private static final Log log = LogFactory.getLog(OutputMessage.class);

	private DslProperty<String> sentTo;

	private Headers headers;

	private DslProperty body;

	private ExecutionProperty assertThat;

	private ResponseBodyMatchers bodyMatchers;

	private ServerPatternValueDslProperty property = new ServerPatternValueDslProperty();

	public OutputMessage() {
	}

	public OutputMessage(OutputMessage outputMessage) {
		this.sentTo = outputMessage.getSentTo();
		this.headers = outputMessage.getHeaders();
		this.body = outputMessage.getBody();
	}

	public void sentTo(String sentTo) {
		this.sentTo = new DslProperty(sentTo);
	}

	public void sentTo(DslProperty sentTo) {
		this.sentTo = sentTo;
	}

	public void body(Object bodyAsValue) {
		this.body = new DslProperty(bodyAsValue);
	}

	public void body(DslProperty bodyAsValue) {
		this.body = bodyAsValue;
	}

	public void assertThat(String assertThat) {
		this.assertThat = new ExecutionProperty(assertThat);
	}

	public DslProperty value(ServerDslProperty serverDslProperty) {
		Object concreteValue = serverDslProperty.getClientValue();
		Object dynamicValue = serverDslProperty.getServerValue();
		// for the output messages ran via stub runner,
		// entries have to have fixed values
		if (dynamicValue instanceof RegexProperty) {
			return ((RegexProperty) dynamicValue).concreteClientEscapedDynamicProducer();
		}

		return new DslProperty(concreteValue, dynamicValue);
	}

	public DslProperty $(ServerDslProperty property) {
		return value(property);
	}

	public DslProperty $(Pattern pattern) {
		return value(new RegexProperty(pattern));
	}

	public DslProperty $(RegexProperty pattern) {
		return value(pattern);
	}

	public DslProperty value(RegexProperty pattern) {
		return value(producer(pattern));
	}

	public DslProperty $(OptionalProperty property) {
		return value(producer(property.optionalPatternValue()));
	}

	@Override
	public RegexProperty regexProperty(Object object) {
		return new RegexProperty(object).concreteClientDynamicProducer();
	}

	public ServerPatternValueDslProperty getProperty() {
		return property;
	}

	public void setProperty(ServerPatternValueDslProperty property) {
		this.property = property;
	}

	public DslProperty<String> getSentTo() {
		return sentTo;
	}

	public void setSentTo(DslProperty<String> sentTo) {
		this.sentTo = sentTo;
	}

	public Headers getHeaders() {
		return headers;
	}

	public void setHeaders(Headers headers) {
		this.headers = headers;
	}

	public DslProperty getBody() {
		return body;
	}

	public void setBody(DslProperty body) {
		this.body = body;
	}

	public ExecutionProperty getAssertThat() {
		return assertThat;
	}

	public void setAssertThat(ExecutionProperty assertThat) {
		this.assertThat = assertThat;
	}

	public ResponseBodyMatchers getBodyMatchers() {
		return bodyMatchers;
	}

	public void setBodyMatchers(ResponseBodyMatchers bodyMatchers) {
		this.bodyMatchers = bodyMatchers;
	}

	@Override
	public ServerDslProperty anyAlphaUnicode() {
		return property.anyAlphaUnicode();
	}

	@Override
	public ServerDslProperty anyAlphaNumeric() {
		return property.anyAlphaNumeric();
	}

	@Override
	public ServerDslProperty anyNumber() {
		return property.anyNumber();
	}

	@Override
	public ServerDslProperty anyInteger() {
		return property.anyInteger();
	}

	@Override
	public ServerDslProperty anyPositiveInt() {
		return property.anyPositiveInt();
	}

	@Override
	public ServerDslProperty anyDouble() {
		return property.anyDouble();
	}

	@Override
	public ServerDslProperty anyHex() {
		return property.anyHex();
	}

	@Override
	public ServerDslProperty aBoolean() {
		return property.aBoolean();
	}

	@Override
	public ServerDslProperty anyIpAddress() {
		return property.anyIpAddress();
	}

	@Override
	public ServerDslProperty anyHostname() {
		return property.anyHostname();
	}

	@Override
	public ServerDslProperty anyEmail() {
		return property.anyEmail();
	}

	@Override
	public ServerDslProperty anyUrl() {
		return property.anyUrl();
	}

	@Override
	public ServerDslProperty anyHttpsUrl() {
		return property.anyHttpsUrl();
	}

	@Override
	public ServerDslProperty anyUuid() {
		return property.anyUuid();
	}

	@Override
	public ServerDslProperty anyDate() {
		return property.anyDate();
	}

	@Override
	public ServerDslProperty anyDateTime() {
		return property.anyDateTime();
	}

	@Override
	public ServerDslProperty anyTime() {
		return property.anyTime();
	}

	@Override
	public ServerDslProperty anyIso8601WithOffset() {
		return property.anyIso8601WithOffset();
	}

	@Override
	public ServerDslProperty anyNonBlankString() {
		return property.anyNonBlankString();
	}

	@Override
	public ServerDslProperty anyNonEmptyString() {
		return property.anyNonEmptyString();
	}

	@Override
	public ServerDslProperty anyOf(String... values) {
		return property.anyOf(values);
	}

	/**
	 * The message headers part of the contract.
	 * @param consumer function to manipulate the message headers
	 */
	public void headers(Consumer<Headers> consumer) {
		this.headers = new Headers();
		consumer.accept(this.headers);
	}

	/**
	 * The stub matchers part of the contract.
	 * @param consumer function to manipulate the body matchers
	 */
	public void bodyMatchers(Consumer<ResponseBodyMatchers> consumer) {
		this.bodyMatchers = new ResponseBodyMatchers();
		consumer.accept(this.bodyMatchers);
	}

	/**
	 * The message headers part of the contract.
	 * @param consumer function to manipulate the message headers
	 */
	public void headers(@DelegatesTo(Headers.class) Closure consumer) {
		this.headers = new Headers();
		consumer.setDelegate(this.headers);
		consumer.call();
	}

	/**
	 * The stub matchers part of the contract.
	 * @param consumer function to manipulate the body matchers
	 */
	public void bodyMatchers(@DelegatesTo(ResponseBodyMatchers.class) Closure consumer) {
		this.bodyMatchers = new ResponseBodyMatchers();
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
		OutputMessage that = (OutputMessage) o;
		return Objects.equals(sentTo, that.sentTo) && Objects.equals(headers, that.headers)
				&& Objects.equals(body, that.body) && Objects.equals(assertThat, that.assertThat)
				&& Objects.equals(bodyMatchers, that.bodyMatchers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sentTo, headers, body, assertThat, bodyMatchers);
	}

	@Override
	public String toString() {
		return "OutputMessage{" + "\n\tsentTo=" + sentTo + ", \n\theaders=" + headers + ", \n\tbody=" + body
				+ ", \n\tassertThat=" + assertThat + ", \n\tbodyMatchers=" + bodyMatchers + "} \n\t" + super.toString();
	}

	private class ServerPatternValueDslProperty extends PatternValueDslProperty<ServerDslProperty> {

		@Override
		protected ServerDslProperty createProperty(Pattern pattern, Object generatedValue) {
			return new ServerDslProperty(pattern, generatedValue);
		}

	}

}
