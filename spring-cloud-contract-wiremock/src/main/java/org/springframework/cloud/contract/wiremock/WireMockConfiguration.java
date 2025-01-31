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

package org.springframework.cloud.contract.wiremock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.contract.wiremock.file.ResourcesFileSource;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * Configuration and lifecycle for a Spring Application context that wants to run a
 * WireMock server. Can be used by adding
 * {@link AutoConfigureWireMock @AutoConfigureWireMock} to a Spring Boot JUnit test. To
 * configure the properties of the wiremock server you can use the AutoConfigureWireMock
 * annotation, or add a bean of type {@link Options} (via
 * {@link WireMockSpring#options()}) to your test context.
 *
 * @author Dave Syer
 * @author Matt Garner
 * @author Waldemar Panas
 *
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WireMockProperties.class)
public class WireMockConfiguration implements SmartLifecycle {

	static final String WIREMOCK_SERVER_BEAN_NAME = "wireMockServer";

	private static final Log log = LogFactory.getLog(WireMockConfiguration.class);

	@Autowired
	WireMockProperties wireMock;

	private volatile boolean running;

	private WireMockServer server;

	@Autowired(required = false)
	private Options options;

	@Autowired(required = false)
	private ObjectProvider<WireMockConfigurationCustomizer> customizers;

	@Autowired
	private DefaultListableBeanFactory beanFactory;

	@Autowired
	private ResourceLoader resourceLoader;

	@PostConstruct
	public void init() throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Running initialization of the WireMock configuration");
		}
		if (this.options == null) {
			com.github.tomakehurst.wiremock.core.WireMockConfiguration factory = WireMockSpring.options();
			if (this.wireMock.getServer().getPort() != 8080) {
				factory.port(this.wireMock.getServer().getPort());
			}
			if (this.wireMock.getServer().getHttpsPort() != -1) {
				factory.httpsPort(this.wireMock.getServer().getHttpsPort());
			}
			registerFiles(factory);
			factory.notifier(new Slf4jNotifier(true));
			if (this.wireMock.getPlaceholders().isEnabled()) {
				factory.extensions(new ResponseTemplateTransformer(false));
			}
			this.options = factory;
			if (this.customizers != null) {
				this.customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
			}
		}
		reRegisterServerWithResetMappings();
		reRegisterBeans();
		updateCurrentServer();
	}

	void initIfNotRunning() throws IOException {
		if (!this.running) {
			init();
		}
	}

	private void reRegisterBeans() {
		if (!this.beanFactory.containsBean(WIREMOCK_SERVER_BEAN_NAME)) {
			if (log.isDebugEnabled()) {
				printRegistrationLog();
			}
			this.beanFactory.registerSingleton(WIREMOCK_SERVER_BEAN_NAME, this.server);
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("Destroying WireMock [" + this.beanFactory.getBean(WIREMOCK_SERVER_BEAN_NAME) + "] instance");
			}
			this.beanFactory.destroySingleton(WIREMOCK_SERVER_BEAN_NAME);
			if (log.isDebugEnabled()) {
				printRegistrationLog();
			}
			this.beanFactory.registerSingleton(WIREMOCK_SERVER_BEAN_NAME, this.server);
		}
	}

	private void printRegistrationLog() {
		log.debug("Registering WireMock [" + this.server + "] at http port [" + httpPort() + "] and https port ["
				+ httpsPort() + "]");
	}

	private void reRegisterServer() {
		if (log.isTraceEnabled()) {
			log.trace("Creating a new server at http port [" + this.wireMock.getServer().getPort() + "] and "
					+ "https port [" + this.wireMock.getServer().getHttpsPort() + "]");
		}
		if (this.isRunning()) {
			if (log.isDebugEnabled()) {
				log.debug("Stopping server [" + this.server + "] at port [" + port(this.server) + "]");
			}
			stop();
		}
		else if (this.server == null) {
			this.server = new WireMockServer(this.options);
			if (log.isDebugEnabled()) {
				log.debug("Created new server [" + this.server + "] at http port [" + httpPort() + "] and https port ["
						+ httpsPort() + "]");
			}
		}
		start();
		if (log.isDebugEnabled()) {
			log.debug("Started server [" + this.server + "] at http port [" + httpPort() + "] and https port ["
					+ httpsPort() + "]");
		}
		logRegisteredMappings();
	}

	private void logRegisteredMappings() {
		if (log.isDebugEnabled()) {
			log.debug("WireMock server has [" + this.server.getStubMappings().size() + "] stubs registered");
		}
	}

	void reRegisterServerWithResetMappings() {
		reRegisterServer();
		resetMappings();
		if (this.server.isRunning()) {
			updateCurrentServer();
		}
	}

	void resetMappings() {
		if (this.server.isRunning()) {
			this.server.resetAll();
			this.server.resetRequests();
			this.server.resetScenarios();
			WireMock.reset();
			WireMock.resetAllRequests();
			WireMock.resetAllScenarios();
			registerStubs();
			logRegisteredMappings();
		}
	}

	private void registerStubs() {
		if (log.isDebugEnabled()) {
			log.debug("Will register [" + this.wireMock.getServer().getStubs().length + "] stub locations");
		}
		for (String stubs : this.wireMock.getServer().getStubs()) {
			if (StringUtils.hasText(stubs)) {
				PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
						this.resourceLoader);
				StringBuilder pattern = new StringBuilder(stubs);
				if (!stubs.contains("*")) {
					if (!stubs.endsWith("/")) {
						pattern.append("/");
					}
					pattern.append("**/*.json");
				}
				try {
					for (Resource resource : resolver.getResources(pattern.toString())) {
						try (InputStream inputStream = resource.getInputStream()) {
							StubMapping stubMapping = WireMockStubMapping
								.buildFrom(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
							this.server.addStubMapping(stubMapping);
						}
					}
				}
				catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			}
		}
	}

	private void registerFiles(com.github.tomakehurst.wiremock.core.WireMockConfiguration factory) throws IOException {
		List<Resource> resources = new ArrayList<>();
		for (String files : this.wireMock.getServer().getFiles()) {
			if (StringUtils.hasText(files)) {
				PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
						this.resourceLoader);
				for (Resource resource : resolver.getResources(files)) {
					if (resource.exists()) {
						resources.add(resource);
					}
				}
			}
		}
		if (!resources.isEmpty()) {
			ResourcesFileSource fileSource = new ResourcesFileSource(resources.toArray(new Resource[0]));
			factory.fileSource(fileSource);
		}
	}

	@Override
	public void start() {
		if (isRunning()) {
			if (log.isDebugEnabled()) {
				log.debug("Server [" + this.server + "] is already running at http port [" + httpPort()
						+ "] / https port [" + httpsPort() + "]");
			}
			updateCurrentServer();
			return;
		}
		this.server.start();
		updateCurrentServer();
	}

	private int httpPort() {
		return this.server.isRunning() ? this.server.port() : -1;
	}

	private void updateCurrentServer() {
		WireMock.configureFor(new WireMock(this.server));
		this.running = true;
		if (log.isDebugEnabled() && this.server.isRunning()) {
			log.debug("Server [" + this.server + "] is already running at http port [" + httpPort() + "] / https port ["
					+ httpsPort() + "]. It has [" + this.server.getStubMappings().size() + "] mappings registered");
		}
	}

	private int httpsPort() {
		return this.server.isRunning() && this.server.getOptions().httpsSettings().enabled() ? this.server.httpsPort()
				: -1;
	}

	@Override
	public void stop() {
		if (this.running) {
			WireMockServer server = this.server;
			int port = port(server);
			this.server.stop();
			this.running = false;
			if (log.isDebugEnabled()) {
				log.debug("Stopped WireMock [" + server + "] instance port [" + port + "]");
			}
		}
		else if (log.isDebugEnabled()) {
			log.debug("Server already stopped");
		}
	}

	private int port(WireMockServer server) {
		return server.isRunning() ? (server.getOptions().httpsSettings().enabled() ? server.httpsPort() : server.port())
				: -1;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	static class Slf4jNotifier implements Notifier {

		private static final Logger log = LoggerFactory.getLogger("WireMock");

		private final boolean verbose;

		Slf4jNotifier(boolean verbose) {
			this.verbose = verbose;
		}

		@Override
		public void info(String message) {
			if (verbose) {
				log.info(message);
			}
		}

		@Override
		public void error(String message) {
			log.error(message);
		}

		@Override
		public void error(String message, Throwable t) {
			log.error(message, t);
		}

	}

}

@ConfigurationProperties("wiremock")
class WireMockProperties {

	private Server server = new Server();

	private Placeholders placeholders = new Placeholders();

	private boolean restTemplateSslEnabled;

	private boolean resetMappingsAfterEachTest;

	public boolean isRestTemplateSslEnabled() {
		return this.restTemplateSslEnabled;
	}

	public void setRestTemplateSslEnabled(boolean restTemplateSslEnabled) {
		this.restTemplateSslEnabled = restTemplateSslEnabled;
	}

	public boolean isResetMappingsAfterEachTest() {
		return this.resetMappingsAfterEachTest;
	}

	public void setResetMappingsAfterEachTest(boolean resetMappingsAfterEachTest) {
		this.resetMappingsAfterEachTest = resetMappingsAfterEachTest;
	}

	public Server getServer() {
		return this.server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Placeholders getPlaceholders() {
		return this.placeholders;
	}

	public void setPlaceholders(Placeholders placeholders) {
		this.placeholders = placeholders;
	}

	public class Placeholders {

		/**
		 * Flag to indicate that http URLs in generated wiremock stubs should be filtered
		 * to add or resolve a placeholder for a dynamic port.
		 */
		private boolean enabled = true;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

	public static class Server {

		private int port = 8080;

		private int httpsPort = -1;

		private String[] stubs = new String[0];

		private String[] files = new String[0];

		private boolean portDynamic = false;

		private boolean httpsPortDynamic = false;

		public int getPort() {
			return this.port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public int getHttpsPort() {
			return this.httpsPort;
		}

		public void setHttpsPort(int httpsPort) {
			this.httpsPort = httpsPort;
		}

		public String[] getStubs() {
			return this.stubs;
		}

		public void setStubs(String[] stubs) {
			this.stubs = stubs;
		}

		public String[] getFiles() {
			return this.files;
		}

		public void setFiles(String[] files) {
			this.files = files;
		}

		public boolean isPortDynamic() {
			return this.portDynamic;
		}

		public void setPortDynamic(boolean portDynamic) {
			this.portDynamic = portDynamic;
		}

		public boolean isHttpsPortDynamic() {
			return this.httpsPortDynamic;
		}

		public void setHttpsPortDynamic(boolean httpsPortDynamic) {
			this.httpsPortDynamic = httpsPortDynamic;
		}

	}

}
