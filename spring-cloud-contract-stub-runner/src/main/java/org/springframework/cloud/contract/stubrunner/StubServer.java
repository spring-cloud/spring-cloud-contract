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

package org.springframework.cloud.contract.stubrunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.spec.Contract;

import com.github.tomakehurst.wiremock.client.WireMock;

class StubServer {

	private static final Logger log = LoggerFactory.getLogger(StubServer.class);

	private final HttpServerStub httpServerStub;
	final StubConfiguration stubConfiguration;
	final Collection<WiremockMappingDescriptor> mappings;
	final Collection<Contract> contracts;

	StubServer(StubConfiguration stubConfiguration, Collection<WiremockMappingDescriptor> mappings,
			Collection<Contract> contracts, HttpServerStub httpServerStub) {
		this.stubConfiguration = stubConfiguration;
		this.mappings = mappings;
		this.httpServerStub = httpServerStub;
		this.contracts = contracts;
	}

	public StubServer start() {
		this.httpServerStub.start();
		log.info("Started stub server for project [" + this.stubConfiguration.toColonSeparatedDependencyNotation()
				+ "] on port " + this.httpServerStub.port());
		registerStubMappings();
		return this;
	}

	public void stop() {
		this.httpServerStub.stop();
	}

	public int getPort() {
		if (this.httpServerStub.isRunning()) {
			return this.httpServerStub.port();
		}
		if (log.isDebugEnabled()) {
			log.debug("The HTTP Server stub is not running... That means that the "
					+ "artifact is running a messaging module. Returning back -1 value of the port.");
		}
		return -1;
	}

	public URL getStubUrl() {
		try {
			return new URL("http://localhost:" + getPort());
		}
		catch (MalformedURLException e) {
			throw new IllegalStateException("Cannot parse URL", e);
		}
	}

	public StubConfiguration getStubConfiguration() {
		return this.stubConfiguration;
	}

	public Collection<Contract> getContracts() {
		return this.contracts;
	}

	private void registerStubMappings() {
		WireMock wireMock = new WireMock("localhost", this.httpServerStub.port(), "");
		registerDefaultHealthChecks(wireMock);
		registerStubs(this.mappings, wireMock);
	}

	private void registerDefaultHealthChecks(WireMock wireMock) {
		registerHealthCheck(wireMock, "/ping");
		registerHealthCheck(wireMock, "/health");
	}

	private void registerStubs(Collection<WiremockMappingDescriptor> sortedMappings, WireMock wireMock) {
		for (WiremockMappingDescriptor mappingDescriptor : sortedMappings) {
			try {
				wireMock.register(mappingDescriptor.getMapping());
				if (log.isDebugEnabled()) {
					log.debug("Registered stub mappings from [" + mappingDescriptor.descriptor + "]");
				}
			}
			catch (Exception e) {
				log.warn("Failed to register the stub mapping [" + mappingDescriptor + "]", e);
			}
		}
	}

	private void registerHealthCheck(WireMock wireMock, String url) {
		registerHealthCheck(wireMock, url, "OK");
	}

	private void registerHealthCheck(WireMock wireMock, String url, String body) {
		wireMock.register(
				WireMock.get(WireMock.urlEqualTo(url)).willReturn(WireMock.aResponse().withBody(body).withStatus(200)));
	}
}
