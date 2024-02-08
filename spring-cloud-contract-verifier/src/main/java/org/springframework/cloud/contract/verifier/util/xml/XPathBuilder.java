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

/**
 * Builder of XPaths.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 * @see XmlVerifiable
 * @see XmlAssertion
 */
public final class XPathBuilder {

	private XPathBuilder() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	/**
	 * @return a builder of {@link XmlVerifiable} with which you can build your XPath.
	 * Once finished just call {@link XmlVerifiable#xPath()} to get XPath as String.
	 */
	public static XmlVerifiable builder() {
		return XmlAssertion.assertThat("").withoutThrowingException();
	}

	/**
	 * Using a XPath builder for the given XML you can read its value.
	 * @param xml xml to assert
	 * @return builder of {@link XmlVerifiable}
	 */
	public static XmlVerifiable builder(String xml) {
		return XmlAssertion.assertThat(xml).withoutThrowingException();
	}

}
