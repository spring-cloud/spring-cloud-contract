/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;

class StubServer {

	private static final Log log = LogFactory.getLog(StubServer.class);

	final StubConfiguration stubConfiguration;

	final Collection<File> mappings;

	final Collection<Contract> contracts;

	private final HttpServerStub httpServerStub;

	StubServer(StubConfiguration stubConfiguration, Collection<File> mappings,
			Collection<Contract> contracts, HttpServerStub httpServerStub) {
		this.stubConfiguration = stubConfiguration;
		this.mappings = mappings;
		this.httpServerStub = httpServerStub;
		this.contracts = contracts;
	}

	public StubServer start(HttpServerStubConfiguration configuration) {
		this.httpServerStub.start(configuration);
		return stubServer();
	}

	private StubServer stubServer() {
		log.info("Started stub server for project ["
				+ this.stubConfiguration.toColonSeparatedDependencyNotation()
				+ "] on port " + this.httpServerStub.port());
		this.httpServerStub.registerMappings(this.mappings);
		return this;
	}

	public boolean isRunning() {
		return this.httpServerStub.isRunning();
	}

	public void stop() {
		if (log.isDebugEnabled()) {
			log.debug("Stopping the server at port [" + this.getPort() + "]");
		}
		this.httpServerStub.stop();
	}

	public int getPort() {
		if (this.httpServerStub.isRunning()) {
			int httpsPort = this.httpServerStub.httpsPort();
			int httpPort = this.httpServerStub.port();
			if (log.isDebugEnabled()) {
				log.debug("Ports for this server are https [" + httpsPort + "] and http ["
						+ httpPort + "]");
			}
			return httpsPort != -1 ? httpsPort : httpPort;
		}
		if (log.isDebugEnabled()) {
			log.debug("The HTTP Server stub is not running... That means that the "
					+ "artifact is running a messaging module. Returning back -1 value of the port.");
		}
		return -1;
	}

	private boolean hasHttps() {
		int httpsPort = this.httpServerStub.httpsPort();
		return httpsPort != -1;
	}

	public URL getStubUrl() {
		try {
			return new URL(
					(hasHttps() ? "https:" : "http:") + "//localhost:" + getPort());
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

	String registeredMappings() {
		return this.httpServerStub.registeredMappings();
	}

	HttpServerStub httpServerStub() {
		return this.httpServerStub;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		StubServer that = (StubServer) o;
		return Objects.equals(this.stubConfiguration, that.stubConfiguration)
				&& Objects.equals(this.contracts, that.contracts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.stubConfiguration, this.contracts);
	}

	@Override
	public String toString() {
		return "StubServer{" + "stubConfiguration=" + this.stubConfiguration
				+ ", mappingsSize=" + this.mappings.size() + '}';
	}

}
