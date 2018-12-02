package org.springframework.cloud.contract.verifier.util

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource

import org.springframework.cloud.contract.spec.internal.BodyMatchers

import static javax.xml.xpath.XPathConstants.NODE
import static org.w3c.dom.Node.ATTRIBUTE_NODE
import static org.w3c.dom.Node.CDATA_SECTION_NODE
import static org.w3c.dom.Node.COMMENT_NODE
import static org.w3c.dom.Node.DOCUMENT_TYPE_NODE
import static org.w3c.dom.Node.NOTATION_NODE
import static org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE
import static org.w3c.dom.Node.TEXT_NODE

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class XmlToXmlPathsConverter {

	static Object removeMatchingXmlPaths(def body, BodyMatchers bodyMatchers) {
		XPath xPath = XPathFactory.newInstance().newXPath()
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().
				newDocumentBuilder()
		Document parsedXml = documentBuilder.
				parse(new InputSource(new StringReader(body as String)))
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
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().
				newDocumentBuilder()
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
}
