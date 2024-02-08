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
import java.util.regex.Pattern;

/**
 * Represents an input for messaging. The input can be a message or some action inside the
 * application.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
public class Input extends Common implements RegexCreatingProperty<ClientDslProperty> {

	private ClientPatternValueDslProperty property = new ClientPatternValueDslProperty();

	private ExecutionProperty triggeredBy;

	private ExecutionProperty assertThat;

	/**
	 * Function that needs to be executed to trigger action in the system.
	 * @param triggeredBy method name that triggers the message
	 */
	public void triggeredBy(String triggeredBy) {
		this.triggeredBy = new ExecutionProperty(triggeredBy);
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

	public ExecutionProperty getTriggeredBy() {
		return triggeredBy;
	}

	public void setTriggeredBy(ExecutionProperty triggeredBy) {
		this.triggeredBy = triggeredBy;
	}

	public ExecutionProperty getAssertThat() {
		return assertThat;
	}

	public void setAssertThat(ExecutionProperty assertThat) {
		this.assertThat = assertThat;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Input input = (Input) o;
		return Objects.equals(triggeredBy, input.triggeredBy) && Objects.equals(assertThat, input.assertThat);
	}

	@Override
	public int hashCode() {
		return Objects.hash(triggeredBy, assertThat);
	}

	@Override
	public String toString() {
		return "Input{\n\t" + ", \n\ttriggeredBy=" + triggeredBy + ", \n\tassertThat=" + assertThat + "} \n\t"
				+ super.toString();
	}

	private class ClientPatternValueDslProperty extends PatternValueDslProperty<ClientDslProperty> {

		@Override
		protected ClientDslProperty createProperty(Pattern pattern, Object generatedValue) {
			return new ClientDslProperty(pattern, generatedValue);
		}

	}

}
