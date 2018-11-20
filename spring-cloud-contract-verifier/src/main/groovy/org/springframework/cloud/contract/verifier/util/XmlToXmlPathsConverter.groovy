package org.springframework.cloud.contract.verifier.util


import org.springframework.cloud.contract.spec.internal.BodyMatchers

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class XmlToXmlPathsConverter {

	// TODO
	static Object removeMatchingXmlPaths(def body, BodyMatchers bodyMatchers) {
		def parsedXml = new XmlSlurper().parseText(body as String)

		throw new UnsupportedOperationException("Please implement me!")
	}
}
