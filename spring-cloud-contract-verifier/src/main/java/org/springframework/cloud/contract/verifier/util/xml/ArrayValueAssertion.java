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

package org.springframework.cloud.contract.verifier.util.xml;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;

class ArrayValueAssertion extends FieldAssertion implements XmlArrayVerifiable {

	private final boolean checkingPrimitiveType;

	ArrayValueAssertion(XmlCachedObjects cachedObjects, LinkedList<String> xPathBuffer,
			LinkedList<String> specialCaseXPathBuffer, Object arrayName,
			XmlAsserterConfiguration xmlAsserterConfiguration) {
		super(cachedObjects, xPathBuffer, specialCaseXPathBuffer, arrayName,
				xmlAsserterConfiguration);
		this.checkingPrimitiveType = true;
	}

	private ArrayValueAssertion(XmlCachedObjects cachedObjects,
			LinkedList<String> xPathBuffer, LinkedList<String> specialCaseXPathBuffer,
			Object arrayName, XmlAsserterConfiguration xmlAsserterConfiguration,
			boolean checkingPrimitiveType) {
		super(cachedObjects, xPathBuffer, specialCaseXPathBuffer, arrayName,
				xmlAsserterConfiguration);
		this.checkingPrimitiveType = checkingPrimitiveType;
	}

	private ArrayValueAssertion(XmlAsserter asserter, boolean checkingPrimitiveType) {
		super(asserter);
		this.checkingPrimitiveType = checkingPrimitiveType;
	}

	@Override
	public XmlArrayVerifiable contains(String value) {
		return new ArrayValueAssertion(this.cachedObjects, this.xPathBuffer,
				this.specialCaseXPathBuffer, value, this.xmlAsserterConfiguration, false);
	}

	@Override
	public XmlArrayVerifiable hasSize(int size) {
		String xPath = "count(" + createXPathString() + ")";
		ArrayValueAssertion verifiable = new ArrayValueAssertion(this,
				this.checkingPrimitiveType);
		verifiable.specialCaseXPathBuffer.clear();
		verifiable.specialCaseXPathBuffer.add(xPath);
		String xPathString = verifiable.createSpecialCaseXPathString();
		ResultSequence sequence = verifiable.resultSequence(xPathString);
		Iterator<Item> iterator = sequence.iterator();
		if (!iterator.hasNext()) {
			throw new IllegalStateException(
					"Parsed XML [" + this.cachedObjects.xmlAsString
							+ "] doesn't match the XPath <" + xPathString + ">");
		}
		int retrievedSize = Integer.valueOf(iterator.next().getStringValue());
		if (retrievedSize != size) {
			throw new IllegalStateException("Parsed XML ["
					+ this.cachedObjects.xmlAsString + "] has size [" + retrievedSize
					+ "] and not [" + size + "] for XPath <" + xPathString + "> ");
		}
		return verifiable;
	}

	@Override
	public FieldAssertion node(String value) {
		FieldAssertion assertion = super.node(value);
		return new ArrayValueAssertion(assertion, false);
	}

	@Override
	public FieldAssertion node(String... nodeNames) {
		FieldAssertion assertion = super.node(nodeNames);
		return new ArrayValueAssertion(assertion, false);
	}

	@Override
	protected void removeLastFieldElement(XmlAsserter readyToCheck) {
		readyToCheck.xPathBuffer.removeLast();
	}

	@Override
	public XmlVerifiable isEqualTo(String value) {
		if (!this.checkingPrimitiveType) {
			return super.isEqualTo(value);
		}
		return equalityOnAPrimitive("[text()=" + escapeText(value) + "]");
	}

	@Override
	public XmlVerifiable isEqualTo(Number value) {
		if (!this.checkingPrimitiveType) {
			return super.isEqualTo(value);
		}
		return equalityOnAPrimitive("[number()=" + String.valueOf(value) + "]");
	}

	private XmlVerifiable equalityOnAPrimitive(String xPath) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(this.cachedObjects,
				this.xPathBuffer, this.fieldName, this.xmlAsserterConfiguration);
		readyToCheck.xPathBuffer.removeLast();
		readyToCheck.xPathBuffer.offer(xPath);
		readyToCheck.checkBufferedXPathString();
		return readyToCheck;
	}

	@Override
	public XmlVerifiable matches(String value) {
		if (!this.checkingPrimitiveType) {
			return super.matches(value);
		}
		return equalityOnAPrimitive(
				"[matches(text(), " + escapeText(escapeRegex(value)) + ")]");
	}

	@Override
	public XmlVerifiable isEqualTo(Boolean value) {
		if (!this.checkingPrimitiveType) {
			return super.isEqualTo(value);
		}
		return isEqualTo(String.valueOf(value));
	}

	@Override
	public boolean isAssertingAValueInArray() {
		return true;
	}

}
