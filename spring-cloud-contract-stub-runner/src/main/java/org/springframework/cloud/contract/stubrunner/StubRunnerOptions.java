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

package org.springframework.cloud.contract.stubrunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Technical options related to running StubRunner
 *
 * Use {@link StubRunnerOptionsBuilder} to build this object.
 *
 * @author Marcin Grzejszczak
 * @author Eddú Meléndez
 * @see StubRunnerOptionsBuilder
 */
public class StubRunnerOptions {

	private static final Log log = LogFactory.getLog(StubRunnerOptions.class);

	/**
	 * min port value of the WireMock instance for the given collaborator.
	 */
	final Integer minPortValue;

	/**
	 * max port value of the WireMock instance for the given collaborator.
	 */
	final Integer maxPortValue;

	/**
	 * root URL from where the JAR with stub mappings will be downloaded.
	 */
	final Resource stubRepositoryRoot;

	/**
	 * stub definition classifier.
	 */
	final String stubsClassifier;

	final Collection<StubConfiguration> dependencies;

	/**
	 * colon separated list of ids to the desired port.
	 */
	final Map<StubConfiguration, Integer> stubIdsToPortMapping;

	/**
	 * Optional username for authorization header.
	 */
	final String username;

	/**
	 * Optional password for authorization header.
	 */
	final String password;

	final StubRunnerProperties.StubsMode stubsMode;

	/**
	 * Optional proxy settings.
	 */
	private final StubRunnerProxyOptions stubRunnerProxyOptions;

	/**
	 * Configuration for an HTTP server stub class that allows to perform additional HTTP
	 * server stub configuration.
	 */
	private final Class<? extends HttpServerStubConfigurer> httpServerStubConfigurer;

	/**
	 * Should only stubs applicable for the given consumer get registered.
	 */
	private boolean stubsPerConsumer = false;

	/**
	 * Name of the consumer. If not set should default to {@code spring.application.name}.
	 */
	private String consumerName;

	/**
	 * For debugging purposes you can output the registered mappings to a given folder.
	 * Each HTTP server stub will have its own subfolder where all the mappings will get
	 * stored.
	 */
	private String mappingsOutputFolder;

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary folder after running
	 * tests.
	 */
	private boolean deleteStubsAfterTest;

	/**
	 * When enabled, this flag will tell stub runner to not load the generated stubs, but
	 * convert the found contracts at runtime to a stub format and run those stubs.
	 */
	private boolean generateStubs;

	/**
	 * When enabled, this flag will tell stub runner to throw an exception when no stubs /
	 * contracts were found.
	 */
	private boolean failOnNoStubs = true;

	/**
	 * Map of properties that can be passed to custom
	 * {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}.
	 */
	private Map<String, String> properties;

	/**
	 *
	 */
	final String serverId;

	StubRunnerOptions(Integer minPortValue, Integer maxPortValue, Resource stubRepositoryRoot,
			StubRunnerProperties.StubsMode stubsMode, String stubsClassifier,
			Collection<StubConfiguration> dependencies, Map<StubConfiguration, Integer> stubIdsToPortMapping,
			String username, String password, final StubRunnerProxyOptions stubRunnerProxyOptions,
			boolean stubsPerConsumer, String consumerName, String mappingsOutputFolder, boolean deleteStubsAfterTest,
			boolean generateStubs, boolean failOnNoStubs, Map<String, String> properties,
			Class<? extends HttpServerStubConfigurer> httpServerStubConfigurer, String serverId) {
		this.minPortValue = minPortValue;
		this.maxPortValue = maxPortValue;
		this.stubRepositoryRoot = stubRepositoryRoot;
		this.stubsMode = stubsMode != null ? stubsMode : StubRunnerProperties.StubsMode.CLASSPATH;
		this.stubsClassifier = stubsClassifier;
		this.dependencies = dependencies;
		this.stubIdsToPortMapping = stubIdsToPortMapping;
		this.username = username;
		this.password = password;
		this.stubRunnerProxyOptions = stubRunnerProxyOptions;
		this.stubsPerConsumer = stubsPerConsumer;
		this.consumerName = consumerName;
		this.mappingsOutputFolder = mappingsOutputFolder;
		this.deleteStubsAfterTest = deleteStubsAfterTest;
		this.generateStubs = generateStubs;
		this.failOnNoStubs = failOnNoStubs;
		this.properties = properties;
		this.httpServerStubConfigurer = httpServerStubConfigurer;
		this.serverId = serverId;
	}

	public static StubRunnerOptions fromSystemProps() {
		StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()
				.withMinPort(Integer.valueOf(System.getProperty("stubrunner.port.range.min", "10000")))
				.withMaxPort(Integer.valueOf(System.getProperty("stubrunner.port.range.max", "15000")))
				.withStubRepositoryRoot(ResourceResolver.resource(System.getProperty("stubrunner.repository.root", "")))
				.withStubsMode(System.getProperty("stubrunner.stubs-mode", "LOCAL"))
				.withStubsClassifier(System.getProperty("stubrunner.classifier", "stubs"))
				.withStubs(System.getProperty("stubrunner.ids", ""))
				.withUsername(System.getProperty("stubrunner.username"))
				.withPassword(System.getProperty("stubrunner.password"))
				.withStubPerConsumer(Boolean.parseBoolean(System.getProperty("stubrunner.stubs-per-consumer", "false")))
				.withConsumerName(System.getProperty("stubrunner.consumer-name"))
				.withMappingsOutputFolder(System.getProperty("stubrunner.mappings-output-folder"))
				.withDeleteStubsAfterTest(
						Boolean.parseBoolean(System.getProperty("stubrunner.delete-stubs-after-test", "true")))
				.withGenerateStubs(Boolean.parseBoolean(System.getProperty("stubrunner.generate-stubs", "false")))
				.withFailOnNoStubs(Boolean.parseBoolean(System.getProperty("stubrunner.fail-on-no-stubs", "false")))
				.withProperties(stubRunnerProps()).withServerId(System.getProperty("stubrunner.server-id", ""));
		builder = httpStubConfigurer(builder);
		String proxyHost = System.getProperty("stubrunner.proxy.host");
		if (proxyHost != null) {
			builder.withProxy(proxyHost, Integer.parseInt(System.getProperty("stubrunner.proxy.port")));
		}
		return builder.build();
	}

	private static StubRunnerOptionsBuilder httpStubConfigurer(StubRunnerOptionsBuilder builder) {
		String classProperty = System.getProperty("stubrunner.http-server-stub-configurer",
				HttpServerStubConfigurer.NoOpHttpServerStubConfigurer.class.getName());
		try {
			Class clazz = Class.forName(classProperty);
			return builder.withHttpServerStubConfigurer(clazz);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Class [" + classProperty + "] not found", ex);
		}
	}

	private static Map<String, String> stubRunnerProps() {
		Map<String, String> map = new HashMap<>();
		Properties properties = System.getProperties();
		Set<String> propertyNames = properties.stringPropertyNames();
		propertyNames.stream()
				// stubrunner.properties.foo.bar=baz
				.filter(s -> s.toLowerCase().startsWith("stubrunner.properties"))
				// foo.bar=baz
				.forEach(s -> map.put(s.substring("stubrunner.properties".length() + 1), System.getProperty(s)));
		return map;
	}

	public Integer port(StubConfiguration stubConfiguration) {
		if (this.stubIdsToPortMapping != null) {
			return this.stubIdsToPortMapping.get(stubConfiguration);
		}
		else {
			return null;
		}
	}

	public Integer getMinPortValue() {
		return this.minPortValue;
	}

	public Integer getMaxPortValue() {
		return this.maxPortValue;
	}

	public Collection<StubConfiguration> getDependencies() {
		return this.dependencies;
	}

	public Map<StubConfiguration, Integer> getStubIdsToPortMapping() {
		return this.stubIdsToPortMapping;
	}

	public Resource getStubRepositoryRoot() {
		return this.stubRepositoryRoot;
	}

	public String getStubRepositoryRootAsString() {
		if (this.stubRepositoryRoot == null) {
			return "";
		}
		try {
			return this.stubRepositoryRoot.getURI().toString();
		}
		catch (FileNotFoundException f) {
			if (log.isDebugEnabled()) {
				log.debug("File not found", f);
			}
			return "";
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public StubRunnerProperties.StubsMode getStubsMode() {
		return this.stubsMode;
	}

	public String getStubsClassifier() {
		return this.stubsClassifier;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public StubRunnerProxyOptions getStubRunnerProxyOptions() {
		return this.stubRunnerProxyOptions;
	}

	public StubRunnerProxyOptions getProxyOptions() {
		return this.stubRunnerProxyOptions;
	}

	public boolean isStubsPerConsumer() {
		return this.stubsPerConsumer;
	}

	@Deprecated
	public void setStubsPerConsumer(boolean stubsPerConsumer) {
		this.stubsPerConsumer = stubsPerConsumer;
	}

	public String getConsumerName() {
		return this.consumerName;
	}

	@Deprecated
	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	public boolean hasMappingsOutputFolder() {
		return StringUtils.hasText(this.mappingsOutputFolder);
	}

	public String getMappingsOutputFolder() {
		return this.mappingsOutputFolder;
	}

	@Deprecated
	public void setMappingsOutputFolder(String mappingsOutputFolder) {
		this.mappingsOutputFolder = mappingsOutputFolder;
	}

	public boolean isDeleteStubsAfterTest() {
		return this.deleteStubsAfterTest;
	}

	@Deprecated
	public void setDeleteStubsAfterTest(boolean deleteStubsAfterTest) {
		this.deleteStubsAfterTest = deleteStubsAfterTest;
	}

	public boolean isGenerateStubs() {
		return this.generateStubs;
	}

	public boolean isFailOnNoStubs() {
		return this.failOnNoStubs;
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	@Deprecated
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public String getServerId() {
		return this.serverId;
	}

	public Class<? extends HttpServerStubConfigurer> getHttpServerStubConfigurer() {
		return this.httpServerStubConfigurer;
	}

	@Override
	public String toString() {
		return "StubRunnerOptions{" + "minPortValue=" + this.minPortValue + ", maxPortValue=" + this.maxPortValue
				+ ", stubRepositoryRoot='" + this.stubRepositoryRoot + '\'' + ", stubsMode='" + this.stubsMode
				+ "', stubsClassifier='" + this.stubsClassifier + '\'' + ", dependencies=" + this.dependencies
				+ ", stubIdsToPortMapping=" + this.stubIdsToPortMapping + ", username='" + obfuscate(this.username)
				+ '\'' + ", password='" + obfuscate(this.password) + '\'' + ", stubRunnerProxyOptions='"
				+ this.stubRunnerProxyOptions + "', stubsPerConsumer='" + this.stubsPerConsumer + '\''
				+ ", httpServerStubConfigurer='" + this.httpServerStubConfigurer + '\'' + ", serverId='" + this.serverId
				+ '\'' + '}';
	}

	private String obfuscate(String string) {
		return StringUtils.hasText(string) ? "****" : "";
	}

	/**
	 * Options for a proxy.
	 */
	public static class StubRunnerProxyOptions {

		private final String proxyHost;

		private final int proxyPort;

		public StubRunnerProxyOptions(final String proxyHost, final int proxyPort) {
			this.proxyHost = proxyHost;
			this.proxyPort = proxyPort;
		}

		public String getProxyHost() {
			return this.proxyHost;
		}

		public int getProxyPort() {
			return this.proxyPort;
		}

		@Override
		public String toString() {
			return "StubRunnerProxyOptions{" + "proxyHost='" + this.proxyHost + '\'' + ", proxyPort=" + this.proxyPort
					+ '}';
		}

	}

}
