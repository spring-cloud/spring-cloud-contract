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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

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
	 * Should the stubs be checked for presence only locally
	 */
	private boolean workOffline;

	/**
	 * The repository root to use (where the stubs should be downloaded from)
	 */
	private Resource repositoryRoot;

	/**
	 * The ids of the stubs to run in "ivy" notation ([groupId]:artifactId:[classifier]:[version][:port]).
	 * {@code groupId}, {@code classifier}, {@code version} and {@code port} can be optional.
	 */
	private String[] ids = new String[0];

	/**
	 * The classifier to use by default in ivy co-ordinates for a stub.
	 */
	private String classifier = "stubs";

	/**
	 * The context path that the stub server will run under.
	 */
	@Deprecated
	private String contextPath = "";

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

	public boolean isWorkOffline() {
		return this.workOffline;
	}

	public void setWorkOffline(boolean workOffline) {
		this.workOffline = workOffline;
	}

	public Resource getRepositoryRoot() {
		return this.repositoryRoot;
	}

	public void setRepositoryRoot(String repositoryRoot) {
		this.repositoryRoot = new DefaultResourceLoader().getResource(repositoryRoot);
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

	public String getContextPath() {
		return this.contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
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

	@Override public String toString() {
		return "StubRunnerProperties{" + "minPort=" + this.minPort + ", maxPort=" + this.maxPort
				+ ", workOffline=" + this.workOffline + ", repositoryRoot=" + this.repositoryRoot
				+ ", ids=" + Arrays.toString(this.ids) + ", classifier='" + this.classifier + '\''
				+ ", setStubsPerConsumer='" + this.stubsPerConsumer + "', consumerName='" + this.consumerName + '\''
				+ '}';
	}
}
