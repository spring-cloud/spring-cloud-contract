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

	def "should successfully parse a WireMock 2.5.0 stub"() {
		expect:
			WireMockStubMapping.buildFrom(stub_2_5_1)
	}

	def "should successfully parse a WireMock 2.5.0 stub that contains transformers"() {
		expect:
			WireMockStubMapping.buildFrom(stub_2_5_1_with_transformer)
	}
}
