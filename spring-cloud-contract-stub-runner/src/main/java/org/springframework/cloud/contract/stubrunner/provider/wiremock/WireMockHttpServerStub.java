package org.springframework.cloud.contract.stubrunner.provider.wiremock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.stubrunner.HttpServerStub;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.util.ClassUtils;
import org.springframework.util.SocketUtils;
import org.springframework.util.StreamUtils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * Abstraction over WireMock as a HTTP Server Stub
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class WireMockHttpServerStub implements HttpServerStub {

	private static final Logger log = LoggerFactory.getLogger(WireMockHttpServerStub.class);
	private static final int INVALID_PORT = -1;

	private WireMockServer wireMockServer;

	private WireMockConfiguration config() {
		if (ClassUtils.isPresent("org.springframework.cloud.contract.wiremock.WireMockSpring", null)) {
			return WireMockSpring.options();
		}
		return new WireMockConfiguration();
	}

	@Override
	public int port() {
		return isRunning() ? this.wireMockServer.port() : INVALID_PORT;
	}

	@Override
	public boolean isRunning() {
		return this.wireMockServer != null && this.wireMockServer.isRunning();
	}

	@Override
	public HttpServerStub start() {
		if (isRunning()) {
			log.info("The server is already running at port [" + port() + "]");
			return this;
		}
		return start(SocketUtils.findAvailableTcpPort());
	}

	@Override
	public HttpServerStub start(int port) {
		this.wireMockServer = new WireMockServer(config().port(port));
		this.wireMockServer.start();
		return this;
	}

	@Override
	public HttpServerStub stop() {
		if (!isRunning()) {
			log.warn("Trying to stop a non started server!");
			return this;
		}
		this.wireMockServer.stop();
		return this;
	}

	@Override
	public HttpServerStub registerMappings(Collection<File> stubFiles) {
		if (!isRunning()) {
			throw new IllegalStateException("Server not started!");
		}
		registerStubMappings(stubFiles);
		return this;
	}

	@Override
	public boolean isAccepted(File file) {
		return file.getName().endsWith(".json");
	}

	StubMapping getMapping(File file) {
		try {
			return StubMapping.buildFrom(StreamUtils.copyToString(
					new FileInputStream(file), Charset.forName("UTF-8")));
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot read file", e);
		}
	}

	private void registerStubMappings(Collection<File> stubFiles) {
		WireMock wireMock = new WireMock("localhost", port(), "");
		registerDefaultHealthChecks(wireMock);
		registerStubs(stubFiles, wireMock);
	}

	private void registerDefaultHealthChecks(WireMock wireMock) {
		registerHealthCheck(wireMock, "/ping");
		registerHealthCheck(wireMock, "/health");
	}

	private void registerStubs(Collection<File> sortedMappings, WireMock wireMock) {
		for (File mappingDescriptor : sortedMappings) {
			try {
				wireMock.register(getMapping(mappingDescriptor));
				if (log.isDebugEnabled()) {
					log.debug("Registered stub mappings from [" + mappingDescriptor + "]");
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
