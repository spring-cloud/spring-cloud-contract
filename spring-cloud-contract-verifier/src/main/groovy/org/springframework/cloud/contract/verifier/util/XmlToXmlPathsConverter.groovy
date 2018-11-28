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

import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.BodyMatchers

import static javax.xml.xpath.XPathConstants.NODE
import static org.w3c.dom.Node.CDATA_SECTION_NODE
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

	private static void removeNode(Object node) {
		if (isValidNode(node)) {
			node.getParentNode().removeChild(node)
		}
		else removeNode(node.getParentNode())
	}

	private static boolean isValidNode(Object node) {
		return node instanceof Node && ![TEXT_NODE, CDATA_SECTION_NODE].contains(node.nodeType)
	}

	private static String xmlToString(Document parsedXml) {
		Transformer transformer = TransformerFactory.newInstance().newTransformer()
		StringWriter writer = new StringWriter()
		StreamResult result = new StreamResult(writer)
		transformer.transform(new DOMSource(parsedXml), result)
		return writer.toString()
	}
}
