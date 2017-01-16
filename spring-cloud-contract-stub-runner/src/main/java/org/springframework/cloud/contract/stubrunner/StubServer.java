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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.spec.Contract;

class StubServer {

	private static final Logger log = LoggerFactory.getLogger(StubServer.class);

	private final HttpServerStub httpServerStub;
	final StubConfiguration stubConfiguration;
	final Collection<File> mappings;
	final Collection<Contract> contracts;

	StubServer(StubConfiguration stubConfiguration, Collection<File> mappings,
			Collection<Contract> contracts, HttpServerStub httpServerStub) {
		this.stubConfiguration = stubConfiguration;
		this.mappings = mappings;
		this.httpServerStub = httpServerStub;
		this.contracts = contracts;
	}

	public StubServer start() {
		this.httpServerStub.start();
		return stubServer();
	}

	public StubServer start(int port) {
		this.httpServerStub.start(port);
		return stubServer();
	}

	private StubServer stubServer() {
		log.info("Started stub server for project [" + this.stubConfiguration.toColonSeparatedDependencyNotation()
				+ "] on port " + this.httpServerStub.port());
		this.httpServerStub.registerMappings(this.mappings);
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


}
