package org.springframework.cloud.contract.stubrunner;

import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.util.ClassUtils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * @author Marcin Grzejszczak
 */
class WireMockHttpServerStub implements HttpServerStub {

	private final WireMockServer wireMockServer;

	WireMockHttpServerStub(int port) {
		this.wireMockServer = new WireMockServer(config().port(port));
	}

	private WireMockConfiguration config() {
		if (ClassUtils.isPresent("org.springframework.cloud.contract.wiremock.WireMockSpring", null)) {
			return WireMockSpring.options();
		}
		return new WireMockConfiguration();
	}

	@Override
	public int port() {
		return this.wireMockServer.port();
	}

	@Override
	public boolean isRunning() {
		return this.wireMockServer.isRunning();
	}

	@Override
	public void start() {
		this.wireMockServer.start();
	}

	@Override
	public void stop() {
		this.wireMockServer.stop();
	}
}
