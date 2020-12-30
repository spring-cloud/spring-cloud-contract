/*
 * Copyright 2020-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Chris Bono
 * @since 2.1.0
 */
public class ContractVerifierUtilTest {

	private final String unnamedXml = "<customer>\n" + "<email>customer@test.com</email>\n" + "</customer>";

	private final String namedComplexXml = "<ns1:customer xmlns:ns1=\"http://demo.com/customer\" xmlns:addr=\"http://demo.com/address\">\n"
			+ "<email>customer@test.com</email>\n" + "<contact-info xmlns=\"http://demo.com/contact-info\">\n"
			+ "<name>Krombopulous</name>" + "<address>" + "<addr:gps>" + "<lat>51</lat>" + "<addr:lon>50</addr:lon>"
			+ "</addr:gps>" + "</address>" + "</contact-info>\n" + "</ns1:customer>";

	@Test
	public void shouldGetValueFromXPath() {
		// Given
		Document parsedXml = parsedXml(unnamedXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml, "/customer/email/text()");
		// Then
		assertThat(value).isEqualTo("customer@test.com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionOnIllegalValueFromXPath() {
		// Given
		Document parsedXml = parsedXml(unnamedXml);
		// When
		ContractVerifierUtil.valueFromXPath(parsedXml, "/ns1:customer/email/text()");
	}

	@Test
	public void shouldGetEmptyValueFromXPathWithNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml, "/customer/email/text()");
		// Then
		assertThat(value).isEqualTo("");
	}

	@Test
	public void shouldGetValueFromXPathWithNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml, "/ns1:customer/email/text()");
		// Then
		assertThat(value).isEqualTo("customer@test.com");
	}

	@Test
	public void shouldGetNamespaceValueFromXPathWithNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml, "/ns1:customer/namespace::ns1");
		// Then
		assertThat(value).isEqualTo("http://demo.com/customer");
	}

	@Test
	public void shouldGetValueFromXPathWithDefaultNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml,
				"/ns1:customer/*[local-name()='contact-info' and namespace-uri()='http://demo.com/contact-info']/*[local-name()='name']/text()");
		// Then
		assertThat(value).isEqualTo("Krombopulous");
	}

	@Test
	public void shouldGetValueFromXPathWithNestedNamespaces1() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml,
				"/ns1:customer/*[local-name()='contact-info' and namespace-uri()='http://demo.com/contact-info']/*[local-name()='address']/addr:gps/*[local-name()='lat']/text()");
		// Then
		assertThat(value).isEqualTo("51");
	}

	@Test
	public void shouldGetValueFromXPathWithNestedNamespaces2() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml,
				"/ns1:customer/*[local-name()='contact-info' and namespace-uri()='http://demo.com/contact-info']/*[local-name()='address']/addr:gps/addr:lon/text()");
		// Then
		assertThat(value).isEqualTo("50");
	}

	@Test
	public void shouldGetEmptyValueFromXPathWithDefaultNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml, "/ns1:customer/contact-info/name/text()");
		// Then
		assertThat(value).isEqualTo("");
	}

	@Test
	public void shouldGetEmptyValueFromXPathWithNestedDefaultNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml,
				"/ns1:customer/*[local-name()='contact-info' and namespace-uri()='http://demo.com/contact-info']/name/text()");
		// Then
		assertThat(value).isEqualTo("");
	}

	@Test
	public void shouldGetNodeFromXPath() {
		// Given
		Document parsedXml = parsedXml(unnamedXml);
		// When
		Node node = ContractVerifierUtil.nodeFromXPath(parsedXml, "/customer/email/text()");
		// Then
		assertThat(node.getTextContent()).isEqualTo("customer@test.com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionOnIllegalNodeFromXPath() {
		// Given
		Document parsedXml = parsedXml(unnamedXml);
		// When
		ContractVerifierUtil.nodeFromXPath(parsedXml, "/ns1:customer/email/text()");
	}

	@Test
	public void shouldGetNodeFromXPathWithNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		Node node = ContractVerifierUtil.nodeFromXPath(parsedXml, "/ns1:customer/email/text()");
		// Then
		assertThat(node.getTextContent()).isEqualTo("customer@test.com");
	}

	@Test
	public void shouldGetNamespaceNodeFromXPathWithNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		Node node = ContractVerifierUtil.nodeFromXPath(parsedXml, "/ns1:customer/namespace::ns1");
		// Then
		assertThat(node.getTextContent()).isEqualTo("http://demo.com/customer");
	}

	@Test
	public void shouldGetEmptyNodeFromXPathWithNamespace() {
		// Given
		Document parsedXml = parsedXml(namedComplexXml);
		// When
		Node node = ContractVerifierUtil.nodeFromXPath(parsedXml, "/customer/email/text()");
		// Then
		assertThat(node).isNull();
	}

	@Test
	public void shouldSetPathWithoutPrefixAndWithSuffix() {
		assertThat(ContractVerifierUtil.fromRelativePath("validate_foo()")).isEqualTo("foo.yml");
		assertThat(ContractVerifierUtil.fromRelativePath("validate_foo")).isEqualTo("foo.yml");
		assertThat(ContractVerifierUtil.fromRelativePath("foo")).isEqualTo("foo.yml");
		assertThat(ContractVerifierUtil.fromRelativePath("foo.yml")).isEqualTo("foo.yml");
	}

	private Document parsedXml(String inputXml) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		try {
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
			return documentBuilder.parse(new InputSource(new StringReader(inputXml)));
		}
		catch (ParserConfigurationException | IOException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

}
