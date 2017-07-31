/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.provider.wiremock

import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.junit.Rule
import spock.lang.Specification

import org.springframework.boot.test.rule.OutputCapture
import org.springframework.boot.test.web.client.TestRestTemplate

class WireMockHttpServerStubSpec extends Specification {
	public static
	final File MAPPING_DESCRIPTOR = new File('src/test/resources/repository/mappings/spring/cloud/ping/ping.json')

	@Rule OutputCapture capture = new OutputCapture()

	def 'should describe stub mapping'() {
		given:
		WireMockHttpServerStub mappingDescriptor = new WireMockHttpServerStub().start() as WireMockHttpServerStub

		when:
		StubMapping mapping = mappingDescriptor.getMapping(MAPPING_DESCRIPTOR)

		then:
		with(mapping) {
			assert request.method == RequestMethod.GET
			assert request.url == '/ping'
			assert response.status == 200
			assert response.body == 'pong'
			assert response.headers.contentTypeHeader.mimeTypePart() == 'text/plain'
		}

		cleanup:
		mappingDescriptor.stop()
	}

	def 'should make WireMock print out logs on INFO'() {
		given:
		WireMockHttpServerStub mappingDescriptor = new WireMockHttpServerStub().start() as WireMockHttpServerStub
		mappingDescriptor.registerMappings([
		        new File(WireMockHttpServerStubSpec.classLoader.getResource("simple.json").toURI())
		])

		when:
		String response = new TestRestTemplate().getForObject("http://localhost:${mappingDescriptor.port()}/foobar", String)

		then:
		response == "foo"
		capture.toString().contains("Matched response definition")

		cleanup:
		mappingDescriptor.stop()
	}
}
