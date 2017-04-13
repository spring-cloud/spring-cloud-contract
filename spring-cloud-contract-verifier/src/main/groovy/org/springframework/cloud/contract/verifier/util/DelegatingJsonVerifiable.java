/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

import java.util.LinkedList;
import java.util.regex.Pattern;

import com.toomuchcoding.jsonassert.JsonVerifiable;

/**
 * Implementation of the {@link MethodBufferingJsonVerifiable} that contains a list
 * of String method commands that need to be executed to assert JSONs.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class DelegatingJsonVerifiable implements MethodBufferingJsonVerifiable {

	private static final Pattern FIELD_PATTERN = Pattern.compile("\\.field\\((\")?(.)+(\")?\\)");
	private static final Pattern ARRAY_PATTERN = Pattern.compile("\\.array\\((\")?(.)+(\")?\\)");

	final JsonVerifiable delegate;
	final LinkedList<String> methodsBuffer;

	DelegatingJsonVerifiable(JsonVerifiable delegate,
			LinkedList<String> methodsBuffer) {
		this.delegate = delegate;
		this.methodsBuffer = new LinkedList<>(methodsBuffer);
	}

	DelegatingJsonVerifiable(JsonVerifiable delegate) {
		this.delegate = delegate;
		this.methodsBuffer = new LinkedList<>();
	}

	private static String stringWithEscapedQuotes(Object object) {
		String stringValue = object.toString();
		return stringValue.replaceAll("\"", "\\\\\"");
	}

	private static String wrapValueWithQuotes(Object value) {
		return value instanceof String ?
				"\"" + stringWithEscapedQuotes(value) + "\"" :
				value.toString();
	}

	private void appendMethodWithValue(String methodName, Object value) {
		this.methodsBuffer.offer("." + methodName + "("  + value + ")");
	}

	private void appendMethodWithQuotedValue(String methodName, Object value) {
		appendMethodWithValue(methodName, wrapValueWithQuotes(value));
	}

	@Override
	public MethodBufferingJsonVerifiable contains(Object value) {
		DelegatingJsonVerifiable verifiable = new FinishedDelegatingJsonVerifiable(this.delegate.contains(value), this.methodsBuffer);
		verifiable.appendMethodWithQuotedValue("contains", value);
		if (isAssertingAValueInArray()) {
			verifiable.methodsBuffer.offer(".value()");
		}
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable field(Object value) {
		Object valueToPut = value instanceof ShouldTraverse ? ((ShouldTraverse) value).value : value;
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(this.delegate.field(valueToPut), this.methodsBuffer);
		if (this.delegate.isIteratingOverArray() && !(value instanceof ShouldTraverse)) {
			verifiable.appendMethodWithQuotedValue("contains", valueToPut);
		} else {
			verifiable.appendMethodWithQuotedValue("field", valueToPut);
		}
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable field(String... strings) {
		MethodBufferingJsonVerifiable verifiable = null;
		for (String string : strings) {
			verifiable = verifiable == null ? field(string) : verifiable.field(string);
		}
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable array(Object value) {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(this.delegate.array(value), this.methodsBuffer);
		verifiable.appendMethodWithQuotedValue("array", value);
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable arrayField(Object value) {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(this.delegate.field(value).arrayField(), this.methodsBuffer);
		verifiable.appendMethodWithQuotedValue("array", value);
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable arrayField() {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(this.delegate.arrayField(), this.methodsBuffer);
		verifiable.methodsBuffer.offer(".arrayField()");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable array() {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(this.delegate.array(), this.methodsBuffer);
		verifiable.methodsBuffer.offer(".array()");
		return verifiable;
	}

	@Override
	public JsonVerifiable elementWithIndex(int i) {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(this.delegate.elementWithIndex(i), this.methodsBuffer);
		this.methodsBuffer.offer(".elementWithIndex(" + i + ")");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable iterationPassingArray() {
		return new DelegatingJsonVerifiable(this.delegate, this.methodsBuffer);
	}

	@Override
	public MethodBufferingJsonVerifiable isEqualTo(String value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(this.delegate.isEqualTo(value), this.methodsBuffer);
		if (this.delegate.isAssertingAValueInArray() && readyToCheck.methodsBuffer.peekLast().equals(".arrayField()")) {
			readyToCheck.appendMethodWithQuotedValue("isEqualTo", escapeJava(value));
			readyToCheck.methodsBuffer.offer(".value()");
		} else if (this.delegate.isAssertingAValueInArray() && !readyToCheck.methodsBuffer.peekLast().contains("array")) {
			readyToCheck.methodsBuffer.offer(".value()");
		} else {
			readyToCheck.appendMethodWithQuotedValue("isEqualTo", escapeJava(value));
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable isEqualTo(Object value) {
		if (value == null) {
			return isNull();
		}
		return isEqualTo(value.toString());
	}

	@Override
	public MethodBufferingJsonVerifiable isEqualTo(Number value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(this.delegate.isEqualTo(value), this.methodsBuffer);
		// related to #271 - the problem is with asserting arrays of maps vs arrays of primitives
		String last = readyToCheck.methodsBuffer.peekLast();
		boolean containsAMatcher = containsAnyMatcher(last);
		if (this.delegate.isAssertingAValueInArray() && containsAMatcher) {
			readyToCheck.methodsBuffer.offer(".value()");
		} else {
			readyToCheck.appendMethodWithValue("isEqualTo", String.valueOf(value));
		}
		return readyToCheck;
	}

	private boolean containsAnyMatcher(String string) {
		return string.contains("isEqualTo") || string.contains("matches") || string.contains("isNull");
	}

	@Override
	public MethodBufferingJsonVerifiable isNull() {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(this.delegate.isNull(), this.methodsBuffer);
		readyToCheck.methodsBuffer.offer(".isNull()");
		return readyToCheck;
	}

	@Override public MethodBufferingJsonVerifiable isEmpty() {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(this.delegate.isEmpty(), this.methodsBuffer);
		readyToCheck.methodsBuffer.offer(".isEmpty()");
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable matches(String value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(this.delegate.matches(value), this.methodsBuffer);
		if (this.delegate.isAssertingAValueInArray()) {
			readyToCheck.appendMethodWithQuotedValue("matches", escapedHackedJavaText(value));
			readyToCheck.methodsBuffer.offer(".value()");
		} else {
			readyToCheck.appendMethodWithQuotedValue("matches", escapedHackedJavaText(value));
		}
		return readyToCheck;
	}

	/**
	 * We need to escape the pattern in order for the produced text to be compilable.
	 * The problem is that sometimes we get quotes that already escaped. If we escape them
	 * we get code that doesn't compile. That's why we're doing this hack to unescape
	 * an double escaped text. Related to https://github.com/spring-cloud/spring-cloud-contract/issues/169
	 */
	private String escapedHackedJavaText(String value) {
		return escapeJava(value)
				.replace("\\\"", "\"");
	}

	@Override
	public MethodBufferingJsonVerifiable isEqualTo(Boolean value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(this.delegate.isEqualTo(value), this.methodsBuffer);
		if (this.delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.offer(".value()");
		} else {
			readyToCheck.appendMethodWithValue("isEqualTo", String.valueOf(value));
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable value() {
		return new FinishedDelegatingJsonVerifiable(this.delegate, this.methodsBuffer);
	}

	@Override
	public boolean assertsSize() {
		for (String s : this.methodsBuffer) {
			if (s.contains(".hasSize(") || s.contains(".isEmpty()")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean assertsConcreteValue() {
		for (String s : this.methodsBuffer) {
			if (FIELD_PATTERN.matcher(s).matches()|| ARRAY_PATTERN.matcher(s).matches()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public JsonVerifiable withoutThrowingException() {
		return this.delegate.withoutThrowingException();
	}

	@Override
	public String jsonPath() {
		return this.delegate.jsonPath();
	}

	@Override
	public void matchesJsonPath(String s) {
		this.delegate.matchesJsonPath(s);
	}

	@Override
	public JsonVerifiable hasSize(int size) {
		FinishedDelegatingJsonVerifiable verifiable = new FinishedDelegatingJsonVerifiable(this.delegate.hasSize(size), this.methodsBuffer);
		verifiable.methodsBuffer.offer(".hasSize(" + size + ")");
		return verifiable;
	}

	@Override
	public boolean isIteratingOverNamelessArray() {
		return this.delegate.isIteratingOverNamelessArray();
	}

	@Override
	public boolean isIteratingOverArray() {
		return this.delegate.isIteratingOverArray();
	}

	@Override
	public boolean isAssertingAValueInArray() {
		return this.delegate.isAssertingAValueInArray();
	}

	@Override
	public String method() {
		return createMethodString();
	}

	private String createMethodString() {
		LinkedList<String> queue = new LinkedList<>(this.methodsBuffer);
		StringBuilder stringBuffer = new StringBuilder();
		while (!queue.isEmpty()) {
			stringBuffer.append(queue.remove());
		}
		return stringBuffer.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DelegatingJsonVerifiable that = (DelegatingJsonVerifiable) o;

		if (this.delegate != null ? !this.delegate.equals(that.delegate) : that.delegate != null)
			return false;
		if (this.delegate == null) {
			return false;
		}
		if (this.delegate.jsonPath() == null && that.delegate.jsonPath() == null)
			return true;
		return this.delegate.jsonPath().equals(that.delegate.jsonPath());

	}

	@Override
	public int hashCode() {
		int result = this.delegate != null ? this.delegate.jsonPath().hashCode() : 0;
		return 31 * result;
	}

	@Override
	public String toString() {
		return "DelegatingJsonVerifiable{" +
				"delegate=\n" + this.delegate +
				", methodsBuffer=" + this.methodsBuffer +
				'}';
	}

	@Override
	public <T> T read(Class<T> aClass) {
		return this.delegate.read(aClass);
	}
}
