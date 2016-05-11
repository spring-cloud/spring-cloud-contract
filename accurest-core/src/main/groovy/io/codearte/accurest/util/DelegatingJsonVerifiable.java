package io.codearte.accurest.util;

import com.toomuchcoding.jsonassert.JsonVerifiable;

import java.util.LinkedList;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

/**
 * @author Marcin Grzejszczak
 */
class DelegatingJsonVerifiable implements MethodBufferingJsonVerifiable {

	private final JsonVerifiable delegate;
	private final LinkedList<String> methodsBuffer;

	DelegatingJsonVerifiable(JsonVerifiable delegate,
							 LinkedList<String> methodsBuffer) {
		this.delegate = delegate;
		this.methodsBuffer = new LinkedList<String>(methodsBuffer);
	}

	DelegatingJsonVerifiable(JsonVerifiable delegate) {
		this.delegate = delegate;
		this.methodsBuffer = new LinkedList<String>();
	}

	private static String stringWithEscapedQuotes(Object object) {
		String stringValue = object.toString();
		return stringValue.replaceAll("\"", "\\\\\"");
	}

	private static String wrapValueWithQuotes(Object value) {
		return value instanceof String ?
				"\"" + stringWithEscapedQuotes(value).replaceAll("\\$", "\\\\\\$") + "\"" :
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
		LinkedList<String> queue = new LinkedList<String>(methodsBuffer);
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
		return methodsBuffer != null ?
				methodsBuffer.equals(that.methodsBuffer) :
				that.methodsBuffer == null;

	}

	@Override
	public int hashCode() {
		int result = delegate != null ? delegate.hashCode() : 0;
		result = 31 * result + (methodsBuffer != null ? methodsBuffer.hashCode() : 0);
		return result;
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
