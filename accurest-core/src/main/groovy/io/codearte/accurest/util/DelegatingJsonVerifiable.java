package io.codearte.accurest.util;

import com.blogspot.toomuchcoding.jsonassert.JsonVerifiable;

/**
 * @author Marcin Grzejszczak
 */
class DelegatingJsonVerifiable implements MethodBufferingJsonVerifiable {

	private final JsonVerifiable delegate;
	private final StringBuffer methodsBuffer;

	DelegatingJsonVerifiable(JsonVerifiable delegate,
			StringBuffer methodsBuffer) {
		this.delegate = delegate;
		this.methodsBuffer = new StringBuffer(methodsBuffer.toString());
	}

	DelegatingJsonVerifiable(JsonVerifiable delegate) {
		this.delegate = delegate;
		this.methodsBuffer = new StringBuffer();
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

	@Override
	public MethodBufferingJsonVerifiable contains(Object value) {
		DelegatingJsonVerifiable verifiable = new FinishedDelegatingJsonVerifiable(delegate.contains(value), methodsBuffer);
		verifiable.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
				.append(")");
		if (isAssertingAValueInArray()) {
			verifiable.methodsBuffer.append(".value()");
		}
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable field(Object value) {
		Object valueToPut = value instanceof ShouldTraverse ? ((ShouldTraverse) value).value : value;
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.field(valueToPut), methodsBuffer);
		if (delegate.isIteratingOverArray() && !(value instanceof ShouldTraverse)) {
			verifiable.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(valueToPut))
					.append(")");
		} else {
			verifiable.methodsBuffer.append(".field(").append(wrapValueWithQuotes(valueToPut))
					.append(")");
		}
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable array(Object value) {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.array(value), methodsBuffer);
		verifiable.methodsBuffer.append(".array(").append(wrapValueWithQuotes(value))
				.append(")");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable arrayField(Object value) {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.field(value).arrayField(), methodsBuffer);
		verifiable.methodsBuffer.append(".array(").append(wrapValueWithQuotes(value))
				.append(")");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable arrayField() {
		return new DelegatingJsonVerifiable(delegate.arrayField(), methodsBuffer);
	}

	@Override
	public MethodBufferingJsonVerifiable array() {
		DelegatingJsonVerifiable verifiable = new DelegatingJsonVerifiable(delegate.array(), methodsBuffer);
		verifiable.methodsBuffer.append(".array()");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonVerifiable iterationPassingArray() {
		return new DelegatingJsonVerifiable(delegate, methodsBuffer);
	}

	@Override
	public MethodBufferingJsonVerifiable isEqualTo(String value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.isEqualTo(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.append(".value()");
		} else {
			readyToCheck.methodsBuffer.append(".isEqualTo(")
					.append(wrapValueWithQuotes(value)).append(")");
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
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.append(".value()");
		} else {
			readyToCheck.methodsBuffer.append(".isEqualTo(").append(String.valueOf(value))
					.append(")");
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable isNull() {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.isNull(), methodsBuffer);
		readyToCheck.methodsBuffer.append(".isNull()");
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable matches(String value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.matches(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.append(".value()");
		} else {
			readyToCheck.methodsBuffer.append(".matches(").append(wrapValueWithQuotes(value))
					.append(")");
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonVerifiable isEqualTo(Boolean value) {
		DelegatingJsonVerifiable readyToCheck = new FinishedDelegatingJsonVerifiable(delegate.isEqualTo(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.append(".value()");
		} else {
			readyToCheck.methodsBuffer.append(".isEqualTo(").append(String.valueOf(value))
					.append(")");
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
		return methodsBuffer.toString();
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
}
