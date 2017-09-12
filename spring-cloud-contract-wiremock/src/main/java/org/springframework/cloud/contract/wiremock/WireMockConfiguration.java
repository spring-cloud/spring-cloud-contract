/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;

/**
 * Configuration and lifecycle for a Spring Application context that wants to run a
 * WireMock server. Can be used by adding
 * {@link AutoConfigureWireMock @AutoConfigureWireMock} to a Spring Boot JUnit test. To
 * configure the properties of the wiremock server you can use the AutoConfigureWireMock
 * annotation, or add a bean of type {@link Options} (via
 * {@link WireMockSpring#options()}) to your test context.
 * 
 * @author Dave Syer
 *
 */
@Configuration
@EnableConfigurationProperties(WireMockProperties.class)
public class WireMockConfiguration implements SmartLifecycle {

	private volatile boolean running;

	private WireMockServer server;

	@Autowired(required = false)
	private Options options;

	@Autowired
	private DefaultListableBeanFactory beanFactory;

	@Autowired
	private WireMockProperties wireMock;

	@Autowired
	private ResourceLoader resourceLoader;

	@PostConstruct
	public void init() throws IOException {
		if (this.options == null) {
			com.github.tomakehurst.wiremock.core.WireMockConfiguration factory = WireMockSpring.options();
			if (this.wireMock.getPort() != 8080) {
				factory.port(this.wireMock.getPort());
			}
			if (this.wireMock.getHttpsPort() != -1) {
				factory.httpsPort(this.wireMock.getHttpsPort());
			}
			registerFiles(factory);
			this.options = factory;
		}
		this.server = new WireMockServer(this.options);
		registerStubs();
		if (!this.beanFactory.containsBean("wireMockServer")) {
			this.beanFactory.registerSingleton("wireMockServer", this.server);
		}
	}

	private void registerStubs() throws IOException {
		for (String stubs : this.wireMock.getStubs()) {
			if (StringUtils.hasText(stubs)) {
				PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
						this.resourceLoader);
				String pattern = stubs;
				if (!pattern.contains("*")) {
					if (!pattern.endsWith("/")) {
						pattern = pattern + "/";
					}
					pattern = pattern + "**/*.json";
				}
				for (Resource resource : resolver.getResources(pattern)) {
					this.server.addStubMapping(WireMockStubMapping
							.buildFrom(StreamUtils.copyToString(resource.getInputStream(), Charset.forName("UTF-8"))));
				}
			}
		}
	}

	private void registerFiles(com.github.tomakehurst.wiremock.core.WireMockConfiguration factory) throws IOException {
		List<Resource> resources = new ArrayList<>();
		for (String files : this.wireMock.getFiles()) {
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
		ResourcesFileSource fileSource = new ResourcesFileSource(resources.toArray(new Resource[0]));
		factory.fileSource(fileSource);
	}

	@Override
	public void start() {
		this.server.start();
		WireMock.configureFor("localhost", this.server.port());
		this.running = true;
	}

	@Override
	public void stop() {
		if (this.running) {
			this.server.stop();
			this.running = false;
		}
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

}

@ConfigurationProperties("wiremock.server")
class WireMockProperties {
	private int port = 8080;

	private int httpsPort = -1;

	private String[] stubs;

	private String[] files;

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

}