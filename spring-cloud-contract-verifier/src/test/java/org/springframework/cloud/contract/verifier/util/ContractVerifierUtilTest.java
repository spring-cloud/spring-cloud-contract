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

public class ContractVerifierUtilTest {

	private final String unnamedXml = "<customer>\r\n"
			+ "      <email>customer@test.com</email>\r\n" + "    </customer>";

	private final String namedXml = "<ns1:customer xmlns:ns1=\"http://demo.com/testns\">\r\n"
			+ "      <email>customer@test.com</email>\r\n" + "    </ns1:customer>";

	@Test
	public void shouldGetValueFromXPath()
			throws ParserConfigurationException, IOException, SAXException {
		// Given
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(unnamedXml)));
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml,
				"/customer/email/text()");
		// Then
		assertThat(value).isEqualTo("customer@test.com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionOnIllegalValueFromXPath()
			throws ParserConfigurationException, IOException, SAXException {
		// Given
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(unnamedXml)));
		// When
		ContractVerifierUtil.valueFromXPath(parsedXml, "/ns1:customer/email/text()");
	}

	@Test
	public void shouldGetValueFromXPathWithNamespace()
			throws ParserConfigurationException, IOException, SAXException {
		// Given
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(namedXml)));
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml,
				"/ns1:customer/email/text()");
		// Then
		assertThat(value).isEqualTo("customer@test.com");
	}

	@Test
	public void shouldGetEmptyValueFromXPathWithNamespace()
			throws ParserConfigurationException, IOException, SAXException {
		// Given
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(namedXml)));
		// When
		String value = ContractVerifierUtil.valueFromXPath(parsedXml,
				"/customer/email/text()");
		// Then
		assertThat(value).isEqualTo("");
	}

	@Test
	public void shouldGetNodeFromXPath()
			throws ParserConfigurationException, IOException, SAXException {
		// Given
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(unnamedXml)));
		// When
		Node node = ContractVerifierUtil.nodeFromXPath(parsedXml,
				"/customer/email/text()");
		// Then
		assertThat(node.getTextContent()).isEqualTo("customer@test.com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionOnIllegalNodeFromXPath()
			throws ParserConfigurationException, IOException, SAXException {
		// Given
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(unnamedXml)));
		// When
		ContractVerifierUtil.nodeFromXPath(parsedXml, "/ns1:customer/email/text()");
	}

	@Test
	public void shouldGetNodeFromXPathWithNamespace()
			throws ParserConfigurationException, IOException, SAXException {
		// Given
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(namedXml)));
		// When
		Node node = ContractVerifierUtil.nodeFromXPath(parsedXml,
				"/ns1:customer/email/text()");
		// Then
		assertThat(node.getTextContent()).isEqualTo("customer@test.com");
	}

	@Test
	public void shouldGetEmptyNodeFromXPathWithNamespace()
			throws ParserConfigurationException, IOException, SAXException {
		// Given
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(namedXml)));
		// When
		Node node = ContractVerifierUtil.nodeFromXPath(parsedXml,
				"/customer/email/text()");
		// Then
		assertThat(node).isNull();
	}

	@Test
	public void should_set_path_without_prefix_and_with_suffix() {
		assertThat(ContractVerifierUtil.fromRelativePath("validate_foo()"))
				.isEqualTo("foo.yml");
		assertThat(ContractVerifierUtil.fromRelativePath("validate_foo"))
				.isEqualTo("foo.yml");
		assertThat(ContractVerifierUtil.fromRelativePath("foo")).isEqualTo("foo.yml");
		assertThat(ContractVerifierUtil.fromRelativePath("foo.yml")).isEqualTo("foo.yml");
	}

}
