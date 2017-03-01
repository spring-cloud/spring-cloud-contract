package org.springframework.cloud.contract.verifier.dsl.wiremock

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class WireMockStubMappingSpec extends Specification {
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
	private static final String stub_2_5_1_with_transformer = """
{
  "id" : "77514bd4-a102-4478-a3c0-0fda8b905591",
  "request" : {
    "method" : "GET"
  },
  "response" : {
    "status" : 200,
    "transformers": ["response-template"]
  },
  "uuid" : "77514bd4-a102-4478-a3c0-0fda8b905591"
}
"""

	def "should successfully parse a WireMock 2.1.7 stub"() {
		expect:
			WireMockStubMapping.buildFrom(stub_2_1_7)
	}

	def "should successfully parse a WireMock 2.5.1 stub"() {
		expect:
			WireMockStubMapping.buildFrom(stub_2_5_1)
	}

	def "should successfully parse a WireMock 2.5.1 stub that contains transformers"() {
		expect:
			WireMockStubMapping.buildFrom(stub_2_5_1_with_transformer)
	}
}
