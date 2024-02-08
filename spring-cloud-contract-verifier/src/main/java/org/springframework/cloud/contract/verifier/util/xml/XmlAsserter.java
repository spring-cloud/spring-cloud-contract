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

package org.springframework.cloud.contract.verifier.util.xml;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.XPath2Expression;
import org.eclipse.wst.xml.xpath2.processor.Engine;
import org.eclipse.wst.xml.xpath2.processor.internal.types.ElementType;
import org.eclipse.wst.xml.xpath2.processor.util.DynamicContextBuilder;

import org.springframework.util.StringUtils;

class XmlAsserter implements XmlVerifiable {

	private static final Log log = LogFactory.getLog(XmlAsserter.class);

	private final static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

	final XmlCachedObjects cachedObjects;

	final LinkedList<String> xPathBuffer;

	// for things like count(...)
	final LinkedList<String> specialCaseXPathBuffer;

	final Object fieldName;

	final XmlAsserterConfiguration xmlAsserterConfiguration;

	XmlAsserter(XmlCachedObjects cachedObjects, LinkedList<String> xPathBuffer,
			LinkedList<String> specialCaseXPathBuffer, Object fieldName,
			XmlAsserterConfiguration xmlAsserterConfiguration) {
		this.cachedObjects = cachedObjects;
		this.xPathBuffer = new LinkedList<>(xPathBuffer);
		this.specialCaseXPathBuffer = new LinkedList<>(specialCaseXPathBuffer);
		this.fieldName = fieldName;
		this.xmlAsserterConfiguration = xmlAsserterConfiguration;
	}

	XmlAsserter(XmlAsserter asserter) {
		this.cachedObjects = asserter.cachedObjects;
		this.xPathBuffer = new LinkedList<>(asserter.xPathBuffer);
		this.specialCaseXPathBuffer = new LinkedList<>(asserter.specialCaseXPathBuffer);
		this.fieldName = asserter.fieldName;
		this.xmlAsserterConfiguration = asserter.xmlAsserterConfiguration;
	}

	static String escapeText(Object object) {
		String string = String.valueOf(object);
		if (!string.contains("'")) {
			return wrapValueWithSingleQuotes(string);
		}
		String[] split = string.split("'");
		LinkedList<String> list = new LinkedList<String>();
		list.add("concat(");
		for (String splitString : split) {
			list.add("'" + splitString + "'");
			list.add(",");
			list.add("\"'\"");
			list.add(",");
		}
		// will remove the last ,', entries
		// removing last colon
		list.removeLast();
		// removing last escaped apostrophe
		list.removeLast();
		// removing last colon
		list.removeLast();
		list.add(")");
		return buildStringFromList(list);
	}

	static String escapeRegex(Object object) {
		return String.valueOf(object);
	}

	private static String escapeSpecialRegexChars(String str) {
		return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0");
	}

	private static String buildStringFromList(List<String> list) {
		StringBuilder builder = new StringBuilder();
		for (String string : list) {
			builder.append(string);
		}
		return builder.toString();
	}

	private static String wrapValueWithSingleQuotes(Object value) {
		return value instanceof String ? "'" + value + "'" : value.toString();
	}

	@Override
	public FieldAssertion node(final String value) {
		FieldAssertion asserter = new FieldAssertion(this.cachedObjects, this.xPathBuffer, this.specialCaseXPathBuffer,
				value, this.xmlAsserterConfiguration);
		asserter.xPathBuffer.offer(String.valueOf(value));
		asserter.xPathBuffer.offer("/");
		return asserter;
	}

	@Override
	public FieldAssertion nodeWithDefaultNamespace(final String value, String defaultNamespace) {
		FieldAssertion asserter = new FieldAssertion(this.cachedObjects, this.xPathBuffer, this.specialCaseXPathBuffer,
				value, this.xmlAsserterConfiguration);
		String path = String.format("*[local-name()='%s'", value);
		if (StringUtils.hasText(defaultNamespace)) {
			path += String.format(" and namespace-uri()='%s'", defaultNamespace);
		}
		path += "]";
		asserter.xPathBuffer.offer(path);
		asserter.xPathBuffer.offer("/");
		return asserter;
	}

	@Override
	public XmlVerifiable withAttribute(String attribute, String attributeValue) {
		FieldAssertion asserter = new FieldAssertion(this.cachedObjects, this.xPathBuffer, this.specialCaseXPathBuffer,
				this.fieldName, this.xmlAsserterConfiguration);
		if (asserter.xPathBuffer.peekLast().equals("/")) {
			asserter.xPathBuffer.removeLast();
		}
		if (isReadyToCheck()) {
			asserter.xPathBuffer.offer("/" + this.fieldName);
		}
		asserter.xPathBuffer.offer("[@" + String.valueOf(attribute) + "=" + escapeText(attributeValue) + "]");
		updateCurrentBuffer(asserter);
		asserter.checkBufferedXPathString();
		return asserter;
	}

	@Override
	public XmlVerifiable withAttribute(String attribute) {
		FieldAssertion asserter = new FieldAssertion(this.cachedObjects, this.xPathBuffer, this.specialCaseXPathBuffer,
				this.fieldName, this.xmlAsserterConfiguration);
		String path = "@" + attribute;
		if (attribute.startsWith("xmlns:")) {
			path = "namespace::" + attribute.substring("xmlns:".length());
		}
		asserter.xPathBuffer.offer(path);
		updateCurrentBuffer(asserter);
		asserter.checkBufferedXPathString();
		return asserter;
	}

	@Override
	public XmlVerifiable text() {
		FieldAssertion asserter = new FieldAssertion(this.cachedObjects, this.xPathBuffer, this.specialCaseXPathBuffer,
				this.fieldName, this.xmlAsserterConfiguration);
		asserter.xPathBuffer.offer("text()");
		return asserter;
	}

	@Override
	public XmlVerifiable index(int index) {
		FieldAssertion asserter = new FieldAssertion(this.cachedObjects, this.xPathBuffer, this.specialCaseXPathBuffer,
				this.fieldName, this.xmlAsserterConfiguration);
		if (asserter.xPathBuffer.peekLast().equals("/")) {
			asserter.xPathBuffer.removeLast();
		}
		asserter.xPathBuffer.offer("[" + index + "]");
		asserter.xPathBuffer.offer("/");
		return asserter;
	}

	@Override
	public FieldAssertion node(String... nodeNames) {
		FieldAssertion assertion = null;
		for (String field : nodeNames) {
			assertion = assertion == null ? node(field) : assertion.node(field);
		}
		return assertion;
	}

	@Override
	public XmlArrayVerifiable array(final String value) {
		ArrayValueAssertion asserter = new ArrayValueAssertion(this.cachedObjects, this.xPathBuffer,
				this.specialCaseXPathBuffer, value, this.xmlAsserterConfiguration);
		asserter.xPathBuffer.offer(String.valueOf(value));
		asserter.xPathBuffer.offer("/");
		return asserter;
	}

	@Override
	public XmlVerifiable isEqualTo(String value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(this.cachedObjects, this.xPathBuffer,
				this.fieldName, this.xmlAsserterConfiguration);
		removeLastFieldElement(readyToCheck);
		readyToCheck.xPathBuffer.offer("[" + this.fieldName + "=" + escapeText(value) + "]");
		updateCurrentBuffer(readyToCheck);
		readyToCheck.checkBufferedXPathString();
		return readyToCheck;
	}

	private void updateCurrentBuffer(XmlAsserter readyToCheck) {
		this.xPathBuffer.clear();
		this.xPathBuffer.addAll(readyToCheck.xPathBuffer);
	}

	@Override
	public XmlVerifiable isEqualTo(Object value) {
		if (value == null) {
			return isNull();
		}
		if (value instanceof Number) {
			return isEqualTo((Number) value);
		}
		else if (value instanceof Boolean) {
			return isEqualTo((Boolean) value);
		}
		else if (value instanceof Pattern) {
			return matches(((Pattern) value).pattern());
		}
		return isEqualTo(value.toString());
	}

	@Override
	public XmlVerifiable isEqualTo(Number value) {
		if (value == null) {
			return isNull();
		}
		return xmlVerifiableFromObject(value);
	}

	private XmlVerifiable xmlVerifiableFromObject(Object value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(this.cachedObjects, this.xPathBuffer,
				this.fieldName, this.xmlAsserterConfiguration);
		removeLastFieldElement(readyToCheck);
		readyToCheck.xPathBuffer.offer("[" + this.fieldName + "=" + String.valueOf(value) + "]");
		// and finally '/foo/bar[baz='sth']
		updateCurrentBuffer(readyToCheck);
		readyToCheck.checkBufferedXPathString();
		return readyToCheck;
	}

	protected void removeLastFieldElement(XmlAsserter readyToCheck) {
		// assuming /foo/bar/baz/
		// remove '/'
		readyToCheck.xPathBuffer.removeLast();
		// remove field name ('baz')
		readyToCheck.xPathBuffer.removeLast();
		// remove '/'
		readyToCheck.xPathBuffer.removeLast();
		// and then we get '/foo/bar'
	}

	@Override
	public XmlVerifiable isNull() {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(this.cachedObjects, this.xPathBuffer,
				this.fieldName, this.xmlAsserterConfiguration);
		String xpath = createXPathString();
		readyToCheck.xPathBuffer.clear();
		readyToCheck.xPathBuffer.offer("not(boolean(" + xpath + "/text()[1]))");
		updateCurrentBuffer(readyToCheck);
		readyToCheck.checkBufferedXPathString();
		return readyToCheck;
	}

	@Override
	public XmlVerifiable matches(String value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(this.cachedObjects, this.xPathBuffer,
				this.fieldName, this.xmlAsserterConfiguration);
		removeLastFieldElement(readyToCheck);
		readyToCheck.xPathBuffer.offer("[matches(" + this.fieldName + ", " + escapeText(escapeRegex(value)) + ")]");
		updateCurrentBuffer(readyToCheck);
		readyToCheck.checkBufferedXPathString();
		return readyToCheck;
	}

	@Override
	public XmlVerifiable isEqualTo(Boolean value) {
		if (value == null) {
			return isNull();
		}
		return isEqualTo(String.valueOf(value));
	}

	@Override
	public XmlVerifiable withoutThrowingException() {
		this.xmlAsserterConfiguration.ignoreXPathException = true;
		return this;
	}

	protected void check(String xPathString) {
		if (this.xmlAsserterConfiguration.ignoreXPathException) {
			log.trace(
					"WARNING!!! Overriding verification of the XPath. Your tests may pass even though they shouldn't");
			return;
		}
		ResultSequence expr = resultSequence(xPathString);
		boolean xpathMatched = !expr.empty();
		if (!xpathMatched) {
			throw new IllegalStateException("Parsed XML [" + this.cachedObjects.xmlAsString
					+ "] doesn't match the XPath <" + xPathString + ">");
		}
	}

	ResultSequence resultSequence(String xPathString) {
		return xPathExpression(xPathString);
	}

	private ResultSequence xPathExpression(String xPathString) {
		try {
			XPath2Expression expr = new Engine().parseExpression(xPathString, this.cachedObjects.xpathBuilder);
			return expr.evaluate(new DynamicContextBuilder(this.cachedObjects.xpathBuilder),
					new Object[] { this.cachedObjects.document });
		}
		catch (Exception e) {
			throw new XmlAsserterXpathException(xPath(), this.cachedObjects.xmlAsString, e);
		}
	}

	void checkBufferedXPathString() {
		check(createXPathString());
	}

	String createXPathString() {
		return createXPathString(this.xPathBuffer);
	}

	String createSpecialCaseXPathString() {
		return createXPathString(this.specialCaseXPathBuffer);
	}

	String createXPathString(LinkedList<String> buffer) {
		LinkedList<String> queue = new LinkedList<String>(buffer);
		StringBuilder stringBuffer = new StringBuilder();
		while (!queue.isEmpty()) {
			String value = queue.remove();
			if (!(queue.isEmpty() && value.equals("/"))) {
				stringBuffer.append(value);
			}
		}
		return stringBuffer.toString();
	}

	@Override
	public String xPath() {
		if (!this.specialCaseXPathBuffer.isEmpty()) {
			return createSpecialCaseXPathString();
		}
		return createXPathString();
	}

	@Override
	public void matchesXPath(String xPath) {
		check(xPath);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		XmlAsserter that = (XmlAsserter) o;
		if (!this.xPathBuffer.equals(that.xPathBuffer)) {
			return false;
		}
		return this.fieldName != null ? this.fieldName.equals(that.fieldName) : that.fieldName == null;

	}

	@Override
	public int hashCode() {
		int result = this.xPathBuffer.hashCode();
		result = 31 * result + (this.fieldName != null ? this.fieldName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "\\nAsserter{\n    " + "xPathBuffer=" + String.valueOf(this.xPathBuffer) + "\n}";
	}

	@Override
	public boolean isIteratingOverArray() {
		return false;
	}

	@Override
	public boolean isAssertingAValueInArray() {
		return false;
	}

	@Override
	public String read() {
		String xpath = xPath();
		ResultSequence expr = resultSequence(xpath);
		if (expr.empty()) {
			throw new XmlAsserterXpathException(xPath(), this.cachedObjects.xmlAsString);
		}
		if (expr instanceof ElementType) {
			return ((ElementType) expr).getStringValue();
		}
		throw new UnsupportedOperationException("Can't return values of complex types");
	}

	protected boolean isReadyToCheck() {
		return false;
	}

	private static class XmlAsserterXpathException extends RuntimeException {

		XmlAsserterXpathException(String xPath, String xmlAsString) {
			super("Exception occurred while trying to evaluate " + "XPath [" + xPath + "] from XML [" + xmlAsString
					+ "]");
		}

		XmlAsserterXpathException(String xPath, String xmlAsString, Exception e) {
			super("Exception occurred while trying to evaluate " + "XPath [" + xPath + "] from XML [" + xmlAsString
					+ "]", e);
		}

	}

}
