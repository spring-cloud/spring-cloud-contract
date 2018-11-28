package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE

import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.ABSENT
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.CASE_INSENSITIVE
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.CONTAINS
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.DOES_NOT_MATCH
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.EQUAL_TO
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.EQUAL_TO_XML
import static org.springframework.cloud.contract.spec.internal.XPathBodyMatcher.OPERATION_TYPE.MATCHES

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@CompileStatic
@PackageScope
class XPathBodyMatcherToWireMockValuePatternConverter {

	static StringValuePattern mapToPattern(OPERATION_TYPE operationType, String value) {
		switch (operationType) {
			case MATCHES: return WireMock.matching(value)
			case DOES_NOT_MATCH: return WireMock.notMatching(value)
			case CONTAINS: return WireMock.containing(value)
			case ABSENT: return WireMock.absent()
			case EQUAL_TO_XML: return WireMock.equalToXml(value)
			case EQUAL_TO: return WireMock.equalTo(value)
			case CASE_INSENSITIVE: return WireMock.equalToIgnoreCase(value)
		}
	}

}
