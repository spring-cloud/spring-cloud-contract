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

package org.springframework.cloud.contract.stubrunner.spring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.contract.stubrunner.ResourceResolver;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 *
 */
@ConfigurationProperties("stubrunner")
public class StubRunnerProperties {

	/**
	 * Min value of a port for the automatically started WireMock server
	 */
	private int minPort = 10000;

	/**
	 * Max value of a port for the automatically started WireMock server
	 */
	private int maxPort = 15000;

	/**
	 * The repository root to use (where the stubs should be downloaded from)
	 */
	private Resource repositoryRoot;

	/**
	 * The string representation of repository root, before it got modified to a Resource
	 */
	private String originalRepositoryRoot;

	/**
	 * The ids of the stubs to run in "ivy" notation ([groupId]:artifactId:[version]:[classifier][:port]).
	 * {@code groupId}, {@code classifier}, {@code version} and {@code port} can be optional.
	 */
	private String[] ids = new String[0];

	/**
	 * The classifier to use by default in ivy co-ordinates for a stub.
	 */
	private String classifier = "stubs";

	/**
	 * Repository username
	 */
	private String username;

	/**
	 * Repository password
	 */
	private String password;

	/**
	 * Repository proxy port
	 */
	private Integer proxyPort;

	/**
	 * Repository proxy host
	 */
	private String proxyHost;

	/**
	 * Should only stubs for this particular consumer get registered in HTTP server stub.
	 */
	private boolean stubsPerConsumer;

	/**
	 * You can override the default {@code spring.application.name} of this field by setting a value to this parameter.
	 */
	private String consumerName;

	/**
	 * Dumps the mappings of each HTTP server to the selected folder
	 */
	private String mappingsOutputFolder;

	/**
	 * Pick where the stubs should come from
	 */
	private StubsMode stubsMode;

	/**
	 * If set to {@code true} will not assert whether the downloaded stubs / contract
	 * JAR was downloaded from a remote location or a local one
	 */
	private boolean snapshotCheckSkip;

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary
	 * folder after running tests
	 */
	private boolean deleteStubsAfterTest = true;

	/**
	 * Map of properties that can be passed to custom {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}
	 */
	private Map<String, String> properties = new HashMap<>();

	/**
	 * An enumeration stub modes.
	 */
	public enum StubsMode {

		/**
		 * Pick the stubs from classpath
		 */
		CLASSPATH,

		/**
		 * Fetch the stubs from local .m2
		 */
		LOCAL,

		/**
		 * Fetch the stubs from a remote location
		 */
		REMOTE,
	}

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
		this.originalRepositoryRoot = repositoryRoot;
		this.repositoryRoot = ResourceResolver.resource(repositoryRoot);
	}

	public String getOriginalRepositoryRoot() {
		return StringUtils.hasText(this.originalRepositoryRoot) ?
				this.originalRepositoryRoot : "";
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

	public void setProperties(String[] properties) {
		Properties elements = StringUtils
				.splitArrayElementsIntoProperties(properties, "=");
		if (elements == null) {
			return;
		}
		for (String key : elements.stringPropertyNames()) {
			this.properties.put(key, elements.getProperty(key));
		}
	}

	@Override public String toString() {
		return "StubRunnerProperties{" + "minPort=" + this.minPort + ", maxPort=" + this.maxPort
				+ ", repositoryRoot=" + this.repositoryRoot
				+ ", ids=" + Arrays.toString(this.ids) + ", classifier='" + this.classifier + '\''
				+ ", setStubsPerConsumer='" + this.stubsPerConsumer + "', consumerName='" + this.consumerName + '\''
				+ ", stubsMode='" + this.stubsMode + '\''
				+ ", snapshotCheckSkip='" + this.snapshotCheckSkip + '\''
				+ ", size of properties=" + this.properties.size()
				+ '}';
	}
}
