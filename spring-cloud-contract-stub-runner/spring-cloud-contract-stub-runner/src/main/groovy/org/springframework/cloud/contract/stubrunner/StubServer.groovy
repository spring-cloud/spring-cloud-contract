/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.verifier.dsl.Contract

@CompileStatic
@Slf4j
@PackageScope
class StubServer {
	private WireMockServer wireMockServer
	final StubConfiguration stubConfiguration
	final Collection<WiremockMappingDescriptor> mappings
	final Collection<Contract> contracts

	StubServer(int port, StubConfiguration stubConfiguration, Collection<WiremockMappingDescriptor> mappings,
			   Collection<Contract> contracts) {
		this.stubConfiguration = stubConfiguration
		this.mappings = mappings
		this.wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port))
		this.contracts = contracts
	}

	StubServer start() {
		wireMockServer.start()
		log.info("Started stub server for project ${stubConfiguration.toColonSeparatedDependencyNotation()} on port ${wireMockServer.port()}")
		registerStubMappings()
		return this
	}

	void stop() {
		wireMockServer.stop()
	}

	int getPort() {
		return wireMockServer.port()
	}

	URL getStubUrl() {
		return new URL("http://localhost:$port")
	}

	private void registerStubMappings() {
		WireMock wireMock = new WireMock('localhost', wireMockServer.port())
		registerDefaultHealthChecks(wireMock)
		registerStubs(mappings, wireMock)
	}

	private void registerDefaultHealthChecks(WireMock wireMock) {
		registerHealthCheck(wireMock, '/ping')
		registerHealthCheck(wireMock, '/health')
	}

	private void registerStubs(Collection<WiremockMappingDescriptor> sortedMappings, WireMock wireMock) {
		sortedMappings.each { WiremockMappingDescriptor mappingDescriptor ->
			try {
				wireMock.register(mappingDescriptor.mapping)
				log.debug("Registered stub mappings from $mappingDescriptor.descriptor")
			} catch (Exception e) {
				log.warn("Failed to register the stub mapping [$mappingDescriptor]", e)
			}
		}
	}

	private void registerHealthCheck(WireMock wireMock, String url, String body = 'OK') {
		wireMock.register(WireMock.get(WireMock.urlEqualTo(url)).willReturn(WireMock.aResponse().withBody(body).withStatus(200)))
	}
}
