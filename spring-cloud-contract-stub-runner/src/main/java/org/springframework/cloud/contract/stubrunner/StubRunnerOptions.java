/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.cloud.contract.stubrunner.util.StringUtils;
import org.springframework.core.io.Resource;

/**
 * Technical options related to running StubRunner
 *
 * Use {@see StubRunnerOptionsBuilder} to build this object.
 *
 * @see StubRunnerOptionsBuilder
 */
public class StubRunnerOptions {

	private static final Log log = LogFactory.getLog(StubRunnerOptions.class);

	/**
	 * min port value of the WireMock instance for the given collaborator
	 */
	final Integer minPortValue;

	/**
	 * max port value of the WireMock instance for the given collaborator
	 */
	final Integer maxPortValue;

	/**
	 * root URL from where the JAR with stub mappings will be downloaded
	 */
	final Resource stubRepositoryRoot;

	/**
	 * stub definition classifier
	 */
	final String stubsClassifier;

	final Collection<StubConfiguration> dependencies;

	/**
	 * colon separated list of ids to the desired port
	 */
	final Map<StubConfiguration, Integer> stubIdsToPortMapping;

	/**
	 * Optional username for authorization header
	 */
	final String username;

	/**
	 * Optional password for authorization header
	 */
	final String password;

	/**
	 * Optional proxy settings
	 */
	private final StubRunnerProxyOptions stubRunnerProxyOptions;

	/**
	 * Should only stubs applicable for the given consumer get registered
	 */
	private boolean stubsPerConsumer = false;

	/**
	 * Name of the consumer. If not set should default to {@code spring.application.name}
	 */
	private String consumerName;

	/**
	 * For debugging purposes you can output the registered mappings to a given folder. Each HTTP server
	 * stub will have its own subfolder where all the mappings will get stored.
	 */
	private String mappingsOutputFolder;

	final StubRunnerProperties.StubsMode stubsMode;

	/**
	 * If set to {@code true} will not assert whether the downloaded stubs / contract
	 * JAR was downloaded from a remote location or a local one
	 */
	private boolean snapshotCheckSkip;

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary
	 * folder after running tests
	 */
	private boolean deleteStubsAfterTest;

	/**
	 * Map of properties that can be passed to custom {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}
	 */
	private Map<String, String> properties = new HashMap<>();


	StubRunnerOptions(Integer minPortValue, Integer maxPortValue,
			Resource stubRepositoryRoot, StubRunnerProperties.StubsMode stubsMode, String stubsClassifier,
			Collection<StubConfiguration> dependencies,
			Map<StubConfiguration, Integer> stubIdsToPortMapping,
			String username, String password, final StubRunnerProxyOptions stubRunnerProxyOptions,
			boolean stubsPerConsumer, String consumerName, String mappingsOutputFolder, boolean snapshotCheckSkip,
			boolean deleteStubsAfterTest, Map<String, String> properties) {
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
		this.snapshotCheckSkip = snapshotCheckSkip;
		this.deleteStubsAfterTest = deleteStubsAfterTest;
		this.properties = properties;
	}

	public Integer port(StubConfiguration stubConfiguration) {
		if (this.stubIdsToPortMapping != null) {
			return this.stubIdsToPortMapping.get(stubConfiguration);
		}
		else {
			return null;
		}
	}

	public static StubRunnerOptions fromSystemProps() {
		StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()
				.withMinPort(Integer.valueOf(System.getProperty("stubrunner.port.range.min", "10000")))
				.withMaxPort(Integer.valueOf(System.getProperty("stubrunner.port.range.max", "15000")))
				.withStubRepositoryRoot(ResourceResolver
						.resource(System.getProperty("stubrunner.repository.root", "")))
				.withStubsMode(System.getProperty("stubrunner.stubs-mode", "LOCAL"))
				.withStubsClassifier(System.getProperty("stubrunner.classifier", "stubs"))
				.withStubs(System.getProperty("stubrunner.ids", ""))
				.withUsername(System.getProperty("stubrunner.username"))
				.withPassword(System.getProperty("stubrunner.password"))
				.withStubPerConsumer(Boolean.parseBoolean(System.getProperty("stubrunner.stubs-per-consumer", "false")))
				.withConsumerName(System.getProperty("stubrunner.consumer-name"))
				.withMappingsOutputFolder(System.getProperty("stubrunner.mappings-output-folder"))
				.withSnapshotCheckSkip(Boolean.parseBoolean(System.getProperty("stubrunner.snapshot-check-skip", "false")))
				.withDeleteStubsAfterTest(Boolean.parseBoolean(System.getProperty("stubrunner.delete-stubs-after-test", "true")))
				.withProperties(stubRunnerProps());
		String proxyHost = System.getProperty("stubrunner.proxy.host");
		if (proxyHost != null) {
			builder.withProxy(proxyHost, Integer.parseInt(System.getProperty("stubrunner.proxy.port")));
		}
		return builder.build();
	}

	private static Map<String, String> stubRunnerProps() {
		Map<String, String> map = new HashMap<>();
		Properties properties = System.getProperties();
		Set<String> propertyNames = properties.stringPropertyNames();
		propertyNames
				.stream()
				// stubrunner.properties.foo.bar=baz
				.filter(s -> s.toLowerCase().startsWith("stubrunner.properties"))
				// foo.bar=baz
				.forEach(s -> map.put(s.substring("stubrunner.properties".length() + 1), System.getProperty(s)));
		return map;
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

	public void setStubsPerConsumer(boolean stubsPerConsumer) {
		this.stubsPerConsumer = stubsPerConsumer;
	}

	public String getConsumerName() {
		return this.consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	public boolean hasMappingsOutputFolder() {
		return StringUtils.hasText(this.mappingsOutputFolder);
	}

	public String getMappingsOutputFolder() {
		return this.mappingsOutputFolder;
	}

	public void setMappingsOutputFolder(String mappingsOutputFolder) {
		this.mappingsOutputFolder = mappingsOutputFolder;
	}

	public boolean isSnapshotCheckSkip() {
		return this.snapshotCheckSkip;
	}

	public void setSnapshotCheckSkip(boolean snapshotCheckSkip) {
		this.snapshotCheckSkip = snapshotCheckSkip;
	}

	public boolean isDeleteStubsAfterTest() {
		return this.deleteStubsAfterTest;
	}

	public void setDeleteStubsAfterTest(boolean deleteStubsAfterTest) {
		this.deleteStubsAfterTest = deleteStubsAfterTest;
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

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

		@Override public String toString() {
			return "StubRunnerProxyOptions{" + "proxyHost='" + this.proxyHost + '\''
					+ ", proxyPort=" + this.proxyPort + '}';
		}
	}

	@Override public String toString() {
		return "StubRunnerOptions{" + "minPortValue=" + this.minPortValue + ", maxPortValue="
				+ this.maxPortValue + ", stubRepositoryRoot='" + this.stubRepositoryRoot + '\''
				+ ", stubsMode='" + this.stubsMode + "', stubsClassifier='" + this.stubsClassifier
				+ '\'' + ", dependencies=" + this.dependencies + ", stubIdsToPortMapping="
				+ this.stubIdsToPortMapping + ", username='" + obfuscate(this.username) + '\'' + ", password='"
				+ obfuscate(this.password) + '\'' + ", stubRunnerProxyOptions='" + this.stubRunnerProxyOptions + "', stubsPerConsumer='"
				+ this.stubsPerConsumer
				+ '\'' + ", stubsPerConsumer='" + this.stubsPerConsumer + '\''
				+ '}';
	}

	private String obfuscate(String string) {
		return StringUtils.hasText(string) ? "****" : "";
	}
}
