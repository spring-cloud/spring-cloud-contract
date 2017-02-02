package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class WireMock2_1_7_StubMappingSpec extends Specification {
	private static final String stub_2_1_7 = """
{
  "request" : {
    "method" : "GET"
  },
  "response" : {
    "status" : 200
  }
}
"""
	private static final String stub_2_5_1 = """
{
  "id" : "77514bd4-a102-4478-a3c0-0fda8b905591",
  "request" : {
    "method" : "GET"
  },
  "response" : {
    "status" : 200
  },
  "uuid" : "77514bd4-a102-4478-a3c0-0fda8b905591"
}
"""

	def "should successfully parse a WireMock 2.1.7 stub"() {
		when:
			StubMapping mapping = WireMock2_1_7_StubMapping.buildFrom(stub_2_1_7)
		then:
			JSONAssert.assertEquals(stub_2_1_7, mapping.toString(), false)
	}

	def "should successfully parse a WireMock 2.5.1 stub"() {
		when:
			StubMapping mapping = WireMock2_1_7_StubMapping.buildFrom(stub_2_5_1)
		then:
			JSONAssert.assertEquals(stub_2_1_7, mapping.toString(), false)
	}
}
