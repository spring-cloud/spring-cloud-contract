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

package org.springframework.cloud.contract.stubrunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.util.ClassUtils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class StubServer {
	
	private static final Logger log = LoggerFactory.getLogger(StubServer.class);
	
	private WireMockServer wireMockServer;
	final StubConfiguration stubConfiguration;
	final Collection<WiremockMappingDescriptor> mappings;
	final Collection<Contract> contracts;

	public StubServer(int port, StubConfiguration stubConfiguration, Collection<WiremockMappingDescriptor> mappings,
	Collection<Contract> contracts) {
		this.stubConfiguration = stubConfiguration;
		this.mappings = mappings;
		this.wireMockServer = new WireMockServer(config().port(port));
		this.contracts = contracts;
	}

	private WireMockConfiguration config() {
		if (ClassUtils.isPresent("org.springframework.cloud.contract.wiremock.WireMockSpring", null)) {
			return WireMockSpring.options();
		}
		return new WireMockConfiguration();
	}

	public StubServer start() {
		wireMockServer.start();
		log.info("Started stub server for project [" + stubConfiguration.toColonSeparatedDependencyNotation() +
				"] on port " + wireMockServer.port());
		registerStubMappings();
		return this;
	}

	public void stop() {
		wireMockServer.stop();
	}

	public int getPort() {
		if (wireMockServer.isRunning()) {
			return wireMockServer.port();
		}
		log.debug("The HTTP Server stub is not running... That means that the " +
				"artifact is running a messaging module. Returning back -1 value of the port.");
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

	public WireMockServer getWireMockServer() {
		return wireMockServer;
	}

	public StubConfiguration getStubConfiguration() {
		return stubConfiguration;
	}

	public Collection<WiremockMappingDescriptor> getMappings() {
		return mappings;
	}

	public Collection<Contract> getContracts() {
		return contracts;
	}

	private void registerStubMappings() {
		WireMock wireMock = new WireMock("localhost", wireMockServer.port());
		registerDefaultHealthChecks(wireMock);
		registerStubs(mappings, wireMock);
	}

	private void registerDefaultHealthChecks(WireMock wireMock) {
		registerHealthCheck(wireMock, "/ping");
		registerHealthCheck(wireMock, "/health");
	}

	private void registerStubs(Collection<WiremockMappingDescriptor> sortedMappings, WireMock wireMock) {
		for (WiremockMappingDescriptor mappingDescriptor : sortedMappings) {		
			try {
				wireMock.register(mappingDescriptor.getMapping());
				log.debug("Registered stub mappings from [" + mappingDescriptor.descriptor + "]");
			} catch (Exception e) {
				log.warn("Failed to register the stub mapping ["+ mappingDescriptor + "]", e);
			}
		}
	}

	private void registerHealthCheck(WireMock wireMock, String url) {
		registerHealthCheck(wireMock, url, "OK");
	}
	
	private void registerHealthCheck(WireMock wireMock, String url, String body) {
		wireMock.register(WireMock.get(WireMock.urlEqualTo(url)).willReturn(WireMock.aResponse().withBody(body).withStatus(200)));
	}
}
