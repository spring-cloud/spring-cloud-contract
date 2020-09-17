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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.wst.xml.xpath2.processor.DOMLoader;
import org.eclipse.wst.xml.xpath2.processor.XercesLoader;
import org.w3c.dom.Document;

/**
 * Entry point for assertions. Use the static factory method and you're ready to go!
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 * @see XmlVerifiable
 */
public final class XmlAssertion {

	private static final Map<String, XmlCachedObjects> CACHE = new ConcurrentHashMap<>();

	private final XmlCachedObjects cachedObjects;

	private final LinkedList<String> xPathBuffer = new LinkedList<>();

	private final LinkedList<String> specialCaseXPathBuffer = new LinkedList<>();

	private final XmlAsserterConfiguration xmlAsserterConfiguration = new XmlAsserterConfiguration();

	private XmlAssertion(Document parsedXml) {
		this.cachedObjects = new XmlCachedObjects(parsedXml);
	}

	private XmlAssertion(String xml) {
		XmlCachedObjects cachedObjects = CACHE.get(xml);
		if (cachedObjects == null && !empty(xml)) {
			try {
				InputStream inputXml = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
				DOMLoader loader = new XercesLoader();
				Document document = loader.load(inputXml);
				cachedObjects = new XmlCachedObjects(document, xml);
			}
			catch (Exception e) {
				throw new IllegalStateException("Exception occurred while trying to parse the XML", e);
			}
			CACHE.put(xml, cachedObjects);
		}
		this.cachedObjects = cachedObjects;
	}

	/**
	 * Starts assertions for the XML provided as {@link String}.
	 * @param xml xml to assert
	 * @return {@link XmlVerifiable}
	 */
	public static XmlVerifiable assertThat(String xml) {
		return new XmlAssertion(xml).root();
	}

	/**
	 * Starts assertions for the XML provided as {@link Document}.
	 * @param parsedXml xml to assert
	 * @return {@link XmlVerifiable}
	 */
	public static XmlVerifiable assertThat(Document parsedXml) {
		return new XmlAssertion(parsedXml).root();
	}

	/**
	 * Helper method so that there are no clashes with other static methods of that name.
	 * @param body xml to assert
	 * @return {@link XmlVerifiable}
	 * @see XmlAssertion#assertThat(String)
	 */
	public static XmlVerifiable assertThatXml(String body) {
		return assertThat(body);
	}

	/**
	 * Helper method so that there are no clashes with other static methods of that name.
	 * @param parsedXml xml to assert
	 * @return {@link XmlVerifiable}
	 * @see XmlAssertion#assertThat(Document)
	 */
	public static XmlVerifiable assertThatXml(Document parsedXml) {
		return assertThat(parsedXml);
	}

	private boolean empty(String text) {
		return text == null || text.length() == 0 || text.matches("^\\s*$");
	}

	private XmlVerifiable root() {
		return new FieldAssertion(this.cachedObjects, this.xPathBuffer, this.specialCaseXPathBuffer, "",
				this.xmlAsserterConfiguration).node("");
	}

}
