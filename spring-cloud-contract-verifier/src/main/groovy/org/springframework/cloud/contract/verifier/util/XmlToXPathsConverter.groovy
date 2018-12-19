/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util


import java.util.stream.IntStream

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

import com.toomuchcoding.xmlassert.XPathBuilder
import com.toomuchcoding.xmlassert.XmlVerifiable
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.MatchingTypeValue
import org.springframework.cloud.contract.spec.internal.PathBodyMatcher

import static java.util.stream.Collectors.toList
import static javax.xml.xpath.XPathConstants.NODE
import static org.apache.logging.log4j.util.Strings.isBlank
import static org.w3c.dom.Node.ATTRIBUTE_NODE
import static org.w3c.dom.Node.CDATA_SECTION_NODE
import static org.w3c.dom.Node.COMMENT_NODE
import static org.w3c.dom.Node.DOCUMENT_NODE
import static org.w3c.dom.Node.DOCUMENT_TYPE_NODE
import static org.w3c.dom.Node.NOTATION_NODE
import static org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE
import static org.w3c.dom.Node.TEXT_NODE

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class XmlToXPathsConverter {

	static Object removeMatchingXmlPaths(def body, BodyMatchers bodyMatchers) {
		XPath xPath = XPathFactory.newInstance().newXPath()
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
		Document parsedXml = documentBuilder
				.parse(new InputSource(new StringReader(body as String)))
		bodyMatchers?.matchers()?.each({
			Object node = xPath.evaluate(it.path(), parsedXml.documentElement, NODE)
			removeNode(node)
		})
		parsedXml.normalizeDocument()
		return xmlToString(parsedXml)
	}

	static String retrieveValueFromBody(String path, Object body) {
		Node node = getNode(path, body)
		if (!(isValueNode(node) || isAttributeNode(node))) {
			throw new IllegalArgumentException("Only data nodes can be used to match.")
		}
		return node.getNodeValue()
	}

	private static Node getNode(String path, Object body) {
		XPath xPath = XPathFactory.newInstance().newXPath()
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
		Document parsedXml = documentBuilder.
				parse(new InputSource(new StringReader(body as String)))
		return xPath.evaluate(path, parsedXml.documentElement, NODE) as Node
	}

	private static void removeNode(Object node) {
		Optional.ofNullable(node).ifPresent() {
			if (isValueNode(node as Node)) {
				node.getParentNode().removeChild(node)
			}
			else removeNode(node.getParentNode())
		}
	}

	private static boolean isValueNode(Node node) {
		return [TEXT_NODE,
				CDATA_SECTION_NODE,
				COMMENT_NODE,
				DOCUMENT_TYPE_NODE,
				PROCESSING_INSTRUCTION_NODE,
				NOTATION_NODE].contains(node.nodeType)
	}

	private static boolean isAttributeNode(Node node) {
		return ATTRIBUTE_NODE == node.nodeType
	}

	private static String xmlToString(Node parsedXml) {
		Transformer transformer = TransformerFactory.newInstance().newTransformer()
		StringWriter writer = new StringWriter()
		StreamResult result = new StreamResult(writer)
		transformer.transform(new DOMSource(parsedXml), result)
		return writer.toString()
	}

	static List<BodyMatcher> mapToMatchers(Object xml) {
		DocumentBuilder documentBuilder = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
		Document parsedXml = documentBuilder.
				parse(new InputSource(new StringReader(xml as String)))
		List<List<Node>> valueNodes = getValueNodesWithParents(parsedXml)
		List<BodyMatcher> matchers = []
		valueNodes.each {
			matchers << new PathBodyMatcher(buildXPath(it),
					new MatchingTypeValue(MatchingType.EQUALITY,
							it.get(0).nodeValue))
		}
		return matchers
	}

	static String buildXPath(List<Node> nodes) {
		XmlVerifiable xmlVerifiable = XPathBuilder.builder()
		if (!nodes) {
			return xmlVerifiable.xPath()
		}
		List<Node> reverted = nodes.reverse()
		reverted.subList(0, reverted.size() - 1).each {
			xmlVerifiable = processNode(xmlVerifiable, it)
		}
		Node closingNode = reverted.get(reverted.size() - 1)
		xmlVerifiable = processClosingNode(xmlVerifiable, closingNode)
		return xmlVerifiable.xPath()
	}

	private static XmlVerifiable processNode(XmlVerifiable xmlVerifiable, Node node) {
		return xmlVerifiable.node(node.nodeName)
	}

	private static XmlVerifiable processNode(XmlVerifiable xmlVerifiable, Attr attribute) {
		return xmlVerifiable.withAttribute(attribute.nodeName)
	}

	private static XmlVerifiable processClosingNode(XmlVerifiable xmlVerifiable, Node node) {
		return xmlVerifiable.text()
	}

	private static XmlVerifiable processClosingNode(XmlVerifiable xmlVerifiable, Attr attribute) {
		return processNode(xmlVerifiable, attribute)
	}

	private static List<List<Node>> getValueNodesWithParents(Node node) {
		List<List<Node>> valueNodes = []
		List<Node> attributes = []
		addValueNodes(node, valueNodes, attributes)
		attributes.each {
			valueNodes << withParents(it)
		}
		return valueNodes
	}

	private static List<Node> addValueNodes(Node node, List<List<Node>> valueNodes, List<Node> attributes) {
		getChildNodesAsList(node).each {
			attributes.addAll(getAttributesAsList(node))
			if (isValueNode(it) && !isBlank(it.nodeValue)) {
				valueNodes << withParents(it)
			}
			else {
				addValueNodes(it, valueNodes, attributes)
			}
		}
	}

	private static List<Node> getChildNodesAsList(Node node) {
		NodeList nodeList = node.getChildNodes()
		return getNodeCollectionElements(nodeList)
	}

	private static List<Node> getAttributesAsList(Node node) {
		NamedNodeMap nodeMap = node.getAttributes()
		return nodeMap != null ? getNodeCollectionElements(nodeMap) : []
	}

	private static List<Node> getNodeCollectionElements(def nodeCollection) {
		return IntStream.range(0, nodeCollection.getLength())
				.mapToObj({ nodeCollection.item(it) })
				.collect(toList())
	}

	private static List<Node> withParents(Node node) {
		List<Node> nodeList = new ArrayList<>()
		nodeList << node
		return addParents(node, nodeList)
	}

	private static List<Node> withParents(Attr attribute) {
		List<Node> nodeList = new ArrayList<>()
		nodeList << attribute
		Node ownerNode = attribute.getOwnerElement()
		return addParents(ownerNode, nodeList)
	}

	private static List<Node> addParents(Node node, nodeList) {
		Node parentNode = node.getParentNode()
		if (parentNode != null && DOCUMENT_NODE != parentNode.nodeType) {
			nodeList << parentNode
			return addParents(parentNode, nodeList)
		}
		return nodeList
	}
}
