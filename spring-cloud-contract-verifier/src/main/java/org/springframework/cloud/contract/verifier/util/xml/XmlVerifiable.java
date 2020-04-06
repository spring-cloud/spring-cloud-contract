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
 * Contract to match a parsed XML via XPath.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public interface XmlVerifiable extends IteratingOverArray, XmlReader {

	/**
	 * Field assertion. Adds a XPath entry for a single node.
	 * @param nodeName name of the node
	 * @return new {@code XmlVerifiable}
	 */
	XmlVerifiable node(String nodeName);

	/**
	 * Field assertion. Adds a attribute to the currently checked node. NOTE: If you want
	 * to both check equality and attributes you have to first check the equality and then
	 * attributes. E.g. having such an XML
	 *
	 * <p>
	 *
	 * {@code
	    <?xml version="1.0" encoding="UTF-8" ?>
	&lt;some&gt;
	&lt;nested&gt;
	&lt;json&gt;with &amp;quot;val&amp;apos;ue&lt;/json&gt;
	&lt;anothervalue&gt;4&lt;/anothervalue&gt;
	&lt;withattr id=&quot;a&quot; id2=&quot;b&quot;&gt;foo&lt;/withattr&gt;
	&lt;withlist&gt;
	&lt;name&gt;name1&lt;/name&gt;
	&lt;/withlist&gt;
	&lt;withlist&gt;
	&lt;name&gt;name2&lt;/name&gt;
	&lt;/withlist&gt;
	&lt;withlist&gt;
	8
	&lt;/withlist&gt;
	&lt;withlist&gt;
	&lt;name id=&quot;10&quot; surname=&quot;kowalski&quot;&gt;name3&lt;/name&gt;
	&lt;/withlist&gt;
	&lt;/nested&gt;
	&lt;/some&gt;
	 * }
	 *
	 * <p>
	 *
	 * In order to check the values of the attributes of the {@code withlist} element with
	 * value {@code name3} you'd have to call:
	 *
	 * <p>
	 *
	 * {@code assertThat(xml1).node("some").node("nested")
	 *     .array("withlist").contains("name").isEqualTo("name3")
	 *     .withAttribute("id", "10").withAttribute("surname", "kowalski")}
	 *
	 * <p>
	 *
	 * The following XPath would be created:
	 * {@code /some/nested/withlist[name='name3']/name[@id='10'][@surname='kowalski'] }
	 * @param attribute attribute name
	 * @param attributeValue attribute value
	 * @return new {@code XmlVerifiable}
	 */
	XmlVerifiable withAttribute(String attribute, String attributeValue);

	/**
	 * Adds attribute query to xPath without comparing with any provided value.
	 * @param attribute AttributeName
	 * @return new {@code XmlVerifiable}
	 */
	XmlVerifiable withAttribute(String attribute);

	/**
	 * Adds a {@code text()} call to xPath.
	 * @return new {@code XmlVerifiable}
	 */
	XmlVerifiable text();

	/**
	 * Adds an index to xPath.
	 * @param index index of xPath
	 * @return new {@code XmlVerifiable}
	 */
	XmlVerifiable index(int index);

	/**
	 * Field assertions. Traverses through the list of nodes and adds a XPath entry for
	 * each one.
	 * @param nodeNames names of nodes
	 * @return new {@code XmlVerifiable}
	 */
	XmlVerifiable node(String... nodeNames);

	/**
	 * When you want to assert values in a array with a given name, e.g.
	 *
	 * <p>
	 *
	 * {@code
	&lt;list&gt;
	&lt;element&gt;foo&lt;/element&gt;
	&lt;element&gt;bar&lt;/element&gt;
	&lt;complexElement&gt;
	&lt;param&gt;baz&lt;/param&gt;
	&lt;/complexElement&gt;
	&lt;/list&gt;
	 * }
	 *
	 * <p>
	 * The code to check it would look like this:
	 * <p>
	 *
	 * {@code array("list").contains("element").isEqualTo("foo")}
	 * {@code array("list").contains("complexElement").node("param").isEqualTo("baz")}
	 *
	 * <p>
	 * The generated XPaths would be
	 * <p>
	 *
	 * {@code /list/element[text()='foo']} {@code /list/complexElement[param='baz']}
	 * @param value element
	 * @return new {@code XmlVerifiable}
	 */
	XmlArrayVerifiable array(String value);

	/**
	 * Equality comparison with String.
	 * @param value element
	 * @return new {@code XmlVerifiable}
	 * @throws IllegalStateException - if XPath is not matched for the parsed XML
	 */
	XmlVerifiable isEqualTo(String value) throws IllegalStateException;

	/**
	 * Equality comparison with any object.
	 * @param value element
	 * @return new {@code XmlVerifiable}
	 * @throws IllegalStateException - if XPath is not matched for the parsed XML
	 */
	XmlVerifiable isEqualTo(Object value) throws IllegalStateException;

	/**
	 * Equality comparison with a Number.
	 * @param value element
	 * @return new {@code XmlVerifiable}
	 * @throws IllegalStateException - if XPath is not matched for the parsed XML
	 */
	XmlVerifiable isEqualTo(Number value) throws IllegalStateException;

	/**
	 * Equality comparison to null.
	 * @return new {@code XmlVerifiable}
	 * @throws IllegalStateException - if XPath is not matched for the parsed XML
	 */
	XmlVerifiable isNull() throws IllegalStateException;

	/**
	 * Regex matching for strings.
	 * @param value element
	 * @return new {@code XmlVerifiable}
	 * @throws IllegalStateException - if XPath is not matched for the parsed XML
	 */
	XmlVerifiable matches(String value) throws IllegalStateException;

	/**
	 * Equality comparison with a Boolean.
	 * @param value element
	 * @return new {@code XmlVerifiable}
	 * @throws IllegalStateException - if XPath is not matched for the parsed XML
	 */
	XmlVerifiable isEqualTo(Boolean value) throws IllegalStateException;

	/**
	 * Calling this method will setup the fluent interface to ignore any XPath
	 * verification.
	 * @return new {@code XmlVerifiable}
	 */
	XmlVerifiable withoutThrowingException();

	/**
	 * @return current XPath expression
	 */
	String xPath();

	/**
	 * Checks if the parsed document matches given XPath.
	 * @param xPath xpath value
	 */
	void matchesXPath(String xPath);

}
