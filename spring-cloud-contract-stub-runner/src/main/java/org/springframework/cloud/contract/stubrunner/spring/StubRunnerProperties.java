/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.spring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.contract.stubrunner.HttpServerStubConfigurer;
import org.springframework.cloud.contract.stubrunner.HttpServerStubConfigurer.NoOpHttpServerStubConfigurer;
import org.springframework.cloud.contract.stubrunner.ResourceResolver;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 */
@ConfigurationProperties("stubrunner")
public class StubRunnerProperties {

	/**
	 * Min value of a port for the automatically started WireMock server.
	 */
	private int minPort = 10000;

	/**
	 * Max value of a port for the automatically started WireMock server.
	 */
	private int maxPort = 15000;

	/**
	 * The repository root to use (where the stubs should be downloaded from).
	 */
	private Resource repositoryRoot;

	/**
	 * The ids of the stubs to run in "ivy" notation
	 * ([groupId]:artifactId:[version]:[classifier][:port]). {@code groupId},
	 * {@code classifier}, {@code version} and {@code port} can be optional.
	 */
	private String[] ids = new String[0];

	/**
	 * The classifier to use by default in ivy co-ordinates for a stub.
	 */
	private String classifier = "stubs";

	/**
	 * Repository username.
	 */
	private String username;

	/**
	 * Repository password.
	 */
	private String password;

	/**
	 * Repository proxy port.
	 */
	private Integer proxyPort;

	/**
	 * Repository proxy host.
	 */
	private String proxyHost;

	/**
	 * Should only stubs for this particular consumer get registered in HTTP server stub.
	 */
	private boolean stubsPerConsumer;

	/**
	 * You can override the default {@code spring.application.name} of this field by
	 * setting a value to this parameter.
	 */
	private String consumerName;

	/**
	 * Dumps the mappings of each HTTP server to the selected folder.
	 */
	private String mappingsOutputFolder;

	/**
	 * Pick where the stubs should come from.
	 */
	private StubsMode stubsMode;

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary folder after running
	 * tests.
	 */
	private boolean deleteStubsAfterTest = true;

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
	private Map<String, String> properties = new HashMap<>();

	/**
	 * Configuration for an HTTP server stub.
	 */
	private Class<? extends HttpServerStubConfigurer> httpServerStubConfigurer = NoOpHttpServerStubConfigurer.class;

	/**
	 *
	 */
	private String serverId;

	public int getMinPort() {
		return this.minPort;
	}

	public void setMinPort(int minPort) {
		this.minPort = minPort;
	}

	public int getMaxPort() {
		return this.maxPort;
	}

	public void setMaxPort(int maxPort) {
		this.maxPort = maxPort;
	}

	public Resource getRepositoryRoot() {
		return this.repositoryRoot;
	}

	public void setRepositoryRoot(String repositoryRoot) {
		this.repositoryRoot = ResourceResolver.resource(repositoryRoot);
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public Integer getProxyPort() {
		return this.proxyPort;
	}

	public void setProxyPort(final Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyHost() {
		return this.proxyHost;
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String[] getIds() {
		return this.ids;
	}

	public void setIds(String[] ids) {
		this.ids = ids;
	}

	public String getClassifier() {
		return this.classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
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

	public String getMappingsOutputFolder() {
		return this.mappingsOutputFolder;
	}

	public void setMappingsOutputFolder(String mappingsOutputFolder) {
		this.mappingsOutputFolder = mappingsOutputFolder;
	}

	public StubsMode getStubsMode() {
		return this.stubsMode;
	}

	public void setStubsMode(StubsMode stubsMode) {
		this.stubsMode = stubsMode;
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

	public void setProperties(String[] properties) {
		Properties elements = StringUtils.splitArrayElementsIntoProperties(properties,
				"=");
		if (elements == null) {
			return;
		}
		for (String key : elements.stringPropertyNames()) {
			this.properties.put(key, elements.getProperty(key));
		}
	}

	public boolean isGenerateStubs() {
		return this.generateStubs;
	}

	public void setGenerateStubs(boolean generateStubs) {
		this.generateStubs = generateStubs;
	}

	public boolean isFailOnNoStubs() {
		return this.failOnNoStubs;
	}

	public void setFailOnNoStubs(boolean failOnNoStubs) {
		this.failOnNoStubs = failOnNoStubs;
	}

	public Class getHttpServerStubConfigurer() {
		return this.httpServerStubConfigurer;
	}

	public void setHttpServerStubConfigurer(Class httpServerStubConfigurer) {
		this.httpServerStubConfigurer = httpServerStubConfigurer;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	@Override
	public String toString() {
		return "StubRunnerProperties{" + "minPort=" + this.minPort + ", maxPort="
				+ this.maxPort + ", repositoryRoot=" + this.repositoryRoot + ", ids="
				+ Arrays.toString(this.ids) + ", classifier='" + this.classifier + '\''
				+ ", setStubsPerConsumer='" + this.stubsPerConsumer + "', consumerName='"
				+ this.consumerName + '\'' + ", stubsMode='" + this.stubsMode + '\''
				+ ", size of properties=" + this.properties.size() + '}';
	}

	/**
	 * An enumeration stub modes.
	 */
	public enum StubsMode {

		/**
		 * Pick the stubs from classpath.
		 */
		CLASSPATH,

		/**
		 * Fetch the stubs from local .m2.
		 */
		LOCAL,

		/**
		 * Fetch the stubs from a remote location.
		 */
		REMOTE,

	}

}
