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

package org.springframework.cloud.contract.stubrunner.provider.wiremock

import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.junit.Rule
import spock.lang.Ignore
import spock.lang.Specification

import org.springframework.boot.test.system.OutputCaptureRule
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.cloud.contract.stubrunner.HttpServerStubConfiguration
import org.springframework.cloud.contract.stubrunner.HttpServerStubConfigurer
import org.springframework.cloud.test.TestSocketUtils
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

class WireMockHttpServerStubSpec extends Specification {
	public static
	final File MAPPING_DESCRIPTOR = new File('src/test/resources/transformers.json')
	public static final File ARBITRARY_JSON = new File('src/test/resources/sample_response.json')
	public static final File PDF = new File('src/test/resources/request.pdf')
	public static final File BROKEN_MAPPING = new File('src/test/resources/broken.json')

	@Rule
	OutputCaptureRule capture = new OutputCaptureRule()

	def 'should describe stub mapping'() {
		given:
			WireMockHttpServerStub mappingDescriptor = new WireMockHttpServerStub().start(new HttpServerStubConfiguration(HttpServerStubConfigurer.NoOpHttpServerStubConfigurer.INSTANCE, null,
					null, TestSocketUtils.findAvailableTcpPort())) as WireMockHttpServerStub
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
		when:
			mappingDescriptor.registerMappings([MAPPING_DESCRIPTOR])
		then:
			noExceptionThrown()
		expect:
			URI uri = new URI("http://localhost:" + mappingDescriptor.port() + "/ping")
			"surprise!" == new RestTemplate().exchange(uri, HttpMethod.GET, (HttpEntity)null, String.class)
					.getHeaders().getFirst("X-My-Header")
		cleanup:
			mappingDescriptor?.stop()
	}

	@Ignore("There's sth wrong with SLF4J versions")
	def 'should make WireMock print out logs on INFO'() {
		given:
			WireMockHttpServerStub mappingDescriptor = new WireMockHttpServerStub().start(new HttpServerStubConfiguration(HttpServerStubConfigurer.NoOpHttpServerStubConfigurer.INSTANCE, null,
					null, TestSocketUtils.findAvailableTcpPort())) as WireMockHttpServerStub
			mappingDescriptor.registerMappings([
					new File(WireMockHttpServerStubSpec.classLoader.getResource("simple.json").toURI())
			])

		when:
			String response = new TestRestTemplate().getForObject("http://localhost:${mappingDescriptor.port()}/foobar", String)

		then:
			response == "foo"
			capture.toString().contains("Matched response definition")

		cleanup:
			mappingDescriptor?.stop()
	}

	def 'should accept a valid mapping'() {
		given:
			WireMockHttpServerStub httpServerStub = new WireMockHttpServerStub()
		when:
			boolean accepted = httpServerStub.isAccepted(MAPPING_DESCRIPTOR)
		then:
			accepted
	}

	def 'should not accept an arbitrary JSON file'() {
		given:
			WireMockHttpServerStub httpServerStub = new WireMockHttpServerStub()
		when:
			boolean accepted = httpServerStub.isAccepted(ARBITRARY_JSON)
		then:
			!accepted
	}

	def 'should not accept a broken mapping file'() {
		given:
			WireMockHttpServerStub httpServerStub = new WireMockHttpServerStub()
		when:
			boolean accepted = httpServerStub.isAccepted(BROKEN_MAPPING)
		then:
			!accepted
	}

	def 'should not accept a non-JSON file'() {
		given:
			WireMockHttpServerStub httpServerStub = new WireMockHttpServerStub()
		when:
			boolean accepted = httpServerStub.isAccepted(PDF)
		then:
			!accepted
	}
}
