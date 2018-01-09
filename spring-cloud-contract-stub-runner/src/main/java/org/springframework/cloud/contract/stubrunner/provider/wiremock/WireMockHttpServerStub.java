package org.springframework.cloud.contract.stubrunner.provider.wiremock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.stubrunner.HttpServerStub;
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsEscapeHelper;
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsJsonPathHelper;
import org.springframework.cloud.contract.verifier.dsl.wiremock.DefaultResponseTransformer;
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockExtensions;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.SocketUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import wiremock.com.github.jknack.handlebars.Helper;

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
			return WireMockSpring.options()
					.extensions(responseTransformers());
		}
		return new WireMockConfiguration().extensions(responseTransformers());
	}

	private Extension[] responseTransformers() {
		List<WireMockExtensions> wireMockExtensions = SpringFactoriesLoader
				.loadFactories(WireMockExtensions.class, null);
		List<Extension> extensions = new ArrayList<>();
		if (!wireMockExtensions.isEmpty()) {
			for (WireMockExtensions wireMockExtension : wireMockExtensions) {
				extensions.addAll(wireMockExtension.extensions());
			}
		} else {
			extensions.add(new DefaultResponseTransformer(false, helpers()));
		}
		return extensions.toArray(new Extension[extensions.size()]);
	}

	/**
	 * Override this if you want to register your own helpers
	 *
	 * @deprecated - please use the {@link WireMockExtensions} mechanism and pass
	 * the helpers in your implementation
	 */
	@Deprecated
	protected Map<String, Helper> helpers() {
		Map<String, Helper> helpers = new HashMap<>();
		helpers.put(HandlebarsJsonPathHelper.NAME, new HandlebarsJsonPathHelper());
		helpers.put(HandlebarsEscapeHelper.NAME, new HandlebarsEscapeHelper());
		return helpers;
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
			if (log.isTraceEnabled()) {
				log.trace("The server is already running at port [" + port() + "]");
			}
			return this;
		}
		return start(SocketUtils.findAvailableTcpPort());
	}

	@Override
	public HttpServerStub start(int port) {
		this.wireMockServer = new WireMockServer(config().port(port)
				.notifier(new Slf4jNotifier(true)));
		this.wireMockServer.start();
		return this;
	}

	@Override
	public HttpServerStub stop() {
		if (!isRunning()) {
			if (log.isTraceEnabled()) {
				log.trace("Trying to stop a non started server!");
			}
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

	@Override public String registeredMappings() {
		Collection<String> mappings = new ArrayList<>();
		for (StubMapping stubMapping : this.wireMockServer.getStubMappings()) {
			mappings.add(stubMapping.toString());
		}
		return jsonArrayOfMappings(mappings);
	}

	private String jsonArrayOfMappings(Collection<String> mappings) {
		return "[" + StringUtils.collectionToDelimitedString(mappings, ",\n") + "]";
	}

	@Override
	public boolean isAccepted(File file) {
		return file.getName().endsWith(".json");
	}

	StubMapping getMapping(File file) {
		try (InputStream stream = Files.newInputStream(file.toPath())) {
			return StubMapping.buildFrom(
					StreamUtils.copyToString(stream, Charset.forName("UTF-8")));
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
				if (log.isDebugEnabled()) {
					log.debug("Failed to register the stub mapping [" + mappingDescriptor + "]", e);
				}
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
