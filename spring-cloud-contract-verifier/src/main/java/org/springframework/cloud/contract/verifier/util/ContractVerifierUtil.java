/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Helper class for the generated tests.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public final class ContractVerifierUtil {

	private static final Log LOG = LogFactory.getLog(ContractVerifierUtil.class);

	private ContractVerifierUtil() {
		throw new IllegalStateException("Can't instantiate utility class");
	}

	/**
	 * Helper method to convert a file to bytes.
	 * @param testClass - test class relative to which the file is stored
	 * @param relativePath - relative path to the file
	 * @return bytes of the file
	 * @since 2.1.0
	 */
	public static byte[] fileToBytes(Object testClass, String relativePath) {
		try {
			URL url = testClass.getClass().getResource(relativePath);
			if (url == null) {
				throw new FileNotFoundException(relativePath);
			}
			return Files.readAllBytes(Paths.get(url.toURI()));
		}
		catch (IOException | URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Helper method to retrieve XML node value with provided xPath.
	 * @param parsedXml - a {@link Document} object with parsed XML content
	 * @param path - the xPath expression to retrieve the value with
	 * @return {@link String} value of the XML node
	 * @since 2.1.0
	 */
	public static String valueFromXPath(Document parsedXml, String path) {
		XPath xPath = XPathFactory.newInstance().newXPath();
		try {
			return xPath.evaluate(path, parsedXml.getDocumentElement());
		}
		catch (XPathExpressionException exception) {
			LOG.error("Incorrect xpath provided: " + path, exception);
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Helper method to retrieve XML {@link Node} with provided xPath.
	 * @param parsedXml - a {@link Document} object with parsed XML content
	 * @param path - the xPath expression to retrieve the value with
	 * @return XML {@link Node} object
	 * @since 2.1.0
	 */
	public static Node nodeFromXPath(Document parsedXml, String path) {
		XPath xPath = XPathFactory.newInstance().newXPath();
		try {
			return (Node) xPath.evaluate(path, parsedXml.getDocumentElement(),
					XPathConstants.NODE);
		}
		catch (XPathExpressionException exception) {
			LOG.error("Incorrect xpath provided: " + path, exception);
			throw new IllegalArgumentException();
		}
	}

}
