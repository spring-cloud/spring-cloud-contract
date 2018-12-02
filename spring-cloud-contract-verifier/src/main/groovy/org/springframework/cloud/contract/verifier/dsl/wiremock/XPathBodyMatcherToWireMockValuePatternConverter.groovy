package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.internal.MatchingType

import static org.springframework.cloud.contract.spec.internal.MatchingType.EQUALITY
import static org.springframework.cloud.contract.spec.internal.MatchingType.XML_EQUALITY

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@CompileStatic
@PackageScope
class XPathBodyMatcherToWireMockValuePatternConverter {

	static StringValuePattern mapToPattern(MatchingType type, String value) {
		switch (type) {
			case EQUALITY: return WireMock.equalTo(value)
			case XML_EQUALITY: return WireMock.equalToXml(value)
			default: return WireMock.matching(value)
		}
	}

}
