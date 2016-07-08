/*
 *  Copyright 2013-2016 the original author or authors.
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

import java.util.LinkedList;
import java.util.regex.Pattern;

import com.toomuchcoding.jsonassert.JsonVerifiable;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

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
		methodsBuffer.offer("." + methodName + "("  + value + ")");
	}

	private void appendMethodWithQuotedValue(String methodName, Object value) {
		appendMethodWithValue(methodName, wrapValueWithQuotes(value));
	}

	@Override
	public MethodBufferingJsonVerifiable contains(Object value) {
		DelegatingJsonVerifiable verifiable = new FinishedDelegatingJsonVerifiable(delegate.contains(value), methodsBuffer);
		verifiable.appendMethodWithQuotedValue("contains", value);
		if (isAssertingAValueInArray()) {
			verifiable.methodsBuffer.offer(".value()");
		}
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable field(Object value) {
		Object valueToPut = value instanceof ShouldTraverse ? ((ShouldTraverse) value).value : value;
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.field(valueToPut), methodsBuffer);
		if (delegate.isIteratingOverArray() && !(value instanceof ShouldTraverse)) {
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
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.array(value), methodsBuffer);
		verifiable.appendMethodWithQuotedValue("array", value);
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable arrayField(Object value) {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.field(value).arrayField(), methodsBuffer);
		verifiable.appendMethodWithQuotedValue("array", value);
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable arrayField() {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.arrayField(), methodsBuffer);
		verifiable.methodsBuffer.offer(".arrayField()");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable array() {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.array(), methodsBuffer);
		verifiable.methodsBuffer.offer(".array()");
		return verifiable;
	}

	@Override
	public JsonVerifiable elementWithIndex(int i) {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.elementWithIndex(i), methodsBuffer);
		verifiable.methodsBuffer.offer(".elementWithIndex(" + i + ")");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable iterationPassingArray() {
		return new DelegatingJsonVerifiable(delegate, methodsBuffer);
	}

	@Override
	public MethodBufferingJsonVerifiable isEqualTo(String value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.isEqualTo(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray() && readyToCheck.methodsBuffer.peekLast().equals(".arrayField()")) {
			readyToCheck.appendMethodWithQuotedValue("isEqualTo", escapeJava(value));
			readyToCheck.methodsBuffer.offer(".value()");
		} else if (delegate.isAssertingAValueInArray() && !readyToCheck.methodsBuffer.peekLast().contains("array")) {
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
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.isEqualTo(value), methodsBuffer);
		// related to #271 - the problem is with asserting arrays of maps vs arrays of primitives
		String last = readyToCheck.methodsBuffer.peekLast();
		boolean containsAMatcher = containsAnyMatcher(last);
		if (delegate.isAssertingAValueInArray() && containsAMatcher) {
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
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.isNull(), methodsBuffer);
		readyToCheck.methodsBuffer.offer(".isNull()");
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable matches(String value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.matches(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.appendMethodWithQuotedValue("matches", escapeJava(value));
			readyToCheck.methodsBuffer.offer(".value()");
		} else {
			readyToCheck.appendMethodWithQuotedValue("matches", escapeJava(value));
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable isEqualTo(Boolean value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.isEqualTo(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.offer(".value()");
		} else {
			readyToCheck.appendMethodWithValue("isEqualTo", String.valueOf(value));
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable value() {
		return new FinishedDelegatingJsonVerifiable(delegate, methodsBuffer);
	}

	@Override
	public boolean assertsSize() {
		for (String s : methodsBuffer) {
			if (s.contains(".hasSize(")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean assertsConcreteValue() {
		for (String s : methodsBuffer) {
			if (FIELD_PATTERN.matcher(s).matches()|| ARRAY_PATTERN.matcher(s).matches()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public JsonVerifiable withoutThrowingException() {
		return delegate.withoutThrowingException();
	}

	@Override
	public String jsonPath() {
		return delegate.jsonPath();
	}

	@Override
	public void matchesJsonPath(String s) {
		delegate.matchesJsonPath(s);
	}

	@Override
	public JsonVerifiable hasSize(int size) {
		FinishedDelegatingJsonVerifiable verifiable = new FinishedDelegatingJsonVerifiable(delegate.hasSize(size), methodsBuffer);
		verifiable.methodsBuffer.offer(".hasSize(" + size + ")");
		return verifiable;
	}

	@Override
	public boolean isIteratingOverNamelessArray() {
		return delegate.isIteratingOverNamelessArray();
	}

	@Override
	public boolean isIteratingOverArray() {
		return delegate.isIteratingOverArray();
	}

	@Override
	public boolean isAssertingAValueInArray() {
		return delegate.isAssertingAValueInArray();
	}

	@Override
	public String method() {
		return createMethodString();
	}

	private String createMethodString() {
		LinkedList<String> queue = new LinkedList<>(methodsBuffer);
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

		if (delegate != null ? !delegate.equals(that.delegate) : that.delegate != null)
			return false;
		if (delegate == null) {
			return false;
		}
		if (delegate.jsonPath() == null && that.delegate.jsonPath() == null)
			return true;
		return delegate.jsonPath().equals(that.delegate.jsonPath());

	}

	@Override
	public int hashCode() {
		int result = delegate != null ? delegate.jsonPath().hashCode() : 0;
		return 31 * result;
	}

	@Override
	public String toString() {
		return "DelegatingJsonVerifiable{" +
				"delegate=\n" + delegate +
				", methodsBuffer=" + methodsBuffer +
				'}';
	}

	@Override
	public <T> T read(Class<T> aClass) {
		return delegate.read(aClass);
	}
}
