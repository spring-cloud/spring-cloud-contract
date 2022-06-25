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

package org.springframework.cloud.contract.verifier.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;
import org.springframework.cloud.contract.verifier.util.xml.DOMNamespaceContext;

/**
 * Helper class for the generated tests.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public final class ContractVerifierUtil {

	/**
	 * Prefix for the generated test names.
	 */
	// TODO: Find a better place for this.
	public static final String TEST_METHOD_PREFIX = "validate_";

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
			try (InputStream inputStream = url.openStream()) {
				if (inputStream == null) {
					throw new FileNotFoundException(relativePath);
				}
				return StreamUtils.copyToByteArray(inputStream);
			}
		}
		catch (IOException ex) {
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
		Element documentElement = parsedXml.getDocumentElement();
		xPath.setNamespaceContext(new DOMNamespaceContext(documentElement));
		try {
			return xPath.evaluate(path, documentElement);
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
		Element documentElement = parsedXml.getDocumentElement();
		xPath.setNamespaceContext(new DOMNamespaceContext(documentElement));
		try {
			return (Node) xPath.evaluate(path, documentElement, XPathConstants.NODE);
		}
		catch (XPathExpressionException exception) {
			LOG.error("Incorrect xpath provided: " + path, exception);
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Helper method to convert a file to bytes.
	 * @param testClass - test class relative to which the file is stored
	 * @param relativePath - relative path to the file
	 * @return bytes of the file
	 * @since 3.0.0
	 */
	public static YamlContract contract(Object testClass, String relativePath) {
		String path = fromRelativePath(relativePath);
		byte[] bytes = fileToBytes(testClass, path);
		List<YamlContract> read = new YamlContractConverter().read(bytes);
		return read.isEmpty() ? null : read.get(0);
	}

	static String fromRelativePath(String relativePath) {
		String path = relativePath;
		if (path.startsWith(TEST_METHOD_PREFIX)) {
			path = path.substring(TEST_METHOD_PREFIX.length());
		}
		if (path.endsWith("()")) {
			path = path.replace("()", "");
		}
		if (!path.endsWith(".yml")) {
			path = path + ".yml";
		}
		return path;
	}

	/**
	 * @return a builder for map
	 */
	public static ContractVerifierMap map() {
		return new ContractVerifierMap();
	}

	/**
	 * A map with a fluent interface.
	 */
	public static class ContractVerifierMap implements Map<Object, Object> {

		private final Map<Object, Object> delegate = new HashMap<>();

		public ContractVerifierMap entry(String key, Object value) {
			put(key, value);
			return this;
		}

		@Override
		public int size() {
			return this.delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return this.delegate.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return this.delegate.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return this.delegate.containsValue(value);
		}

		@Override
		public Object get(Object key) {
			return this.delegate.get(key);
		}

		@Override
		public Object put(Object key, Object value) {
			return this.delegate.put(key, value);
		}

		@Override
		public Object remove(Object key) {
			return this.delegate.remove(key);
		}

		@Override
		public void putAll(Map<? extends Object, ?> m) {
			this.delegate.putAll(m);
		}

		@Override
		public void clear() {
			this.delegate.clear();
		}

		@Override
		public Set<Object> keySet() {
			return this.delegate.keySet();
		}

		@Override
		public Collection<Object> values() {
			return this.delegate.values();
		}

		@Override
		public Set<Entry<Object, Object>> entrySet() {
			return this.delegate.entrySet();
		}

		@Override
		public boolean equals(Object o) {
			return this.delegate.equals(o);
		}

		@Override
		public int hashCode() {
			return this.delegate.hashCode();
		}

	}

}
