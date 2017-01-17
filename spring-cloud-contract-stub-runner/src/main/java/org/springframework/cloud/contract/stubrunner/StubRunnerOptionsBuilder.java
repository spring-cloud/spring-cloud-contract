/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.contract.stubrunner.util.StubsParser;
import org.springframework.util.StringUtils;

public class StubRunnerOptionsBuilder {

	private static final String DELIMITER = ":";
	private LinkedList<String> stubs = new LinkedList<>();
	private Map<StubConfiguration, Integer> stubIdsToPortMapping = new LinkedHashMap<>();

	private Integer minPortValue = 10000;
	private Integer maxPortValue = 15000;
	private String stubRepositoryRoot;
	private boolean workOffline = false;
	private String stubsClassifier = "stubs";
	private String username;
	private String password;
	private StubRunnerOptions.StubRunnerProxyOptions stubRunnerProxyOptions;
	private String privateKeyPathname;
	private String privateKeyPassphrase;

	public StubRunnerOptionsBuilder() {
	}

	public StubRunnerOptionsBuilder(StubRunnerOptions options) {
		withOptions(options);
	}

	public StubRunnerOptionsBuilder withStubs(String... stubs) {
		addStub(stubsToList(stubs));
		return this;
	}

	public StubRunnerOptionsBuilder withStubs(List<String> stubs) {
		for (String stub : stubs) {
			withStubs(stub);
		}
		return this;
	}

	public StubRunnerOptionsBuilder withMinMaxPort(Integer minPortValue, Integer maxPortValue) {
		this.minPortValue = minPortValue;
		this.maxPortValue = maxPortValue;
		return this;
	}

	public StubRunnerOptionsBuilder withMinPort(int minPortValue) {
		this.minPortValue = minPortValue;
		return this;
	}

	public StubRunnerOptionsBuilder withMaxPort(int maxPortValue) {
		this.maxPortValue = maxPortValue;
		return this;
	}

	public StubRunnerOptionsBuilder withStubRepositoryRoot(String stubRepositoryRoot) {
		this.stubRepositoryRoot = stubRepositoryRoot;
		return this;
	}

	public StubRunnerOptionsBuilder withWorkOffline(boolean workOffline) {
		this.workOffline = workOffline;
		return this;
	}

	public StubRunnerOptionsBuilder withStubsClassifier(String stubsClassifier) {
		this.stubsClassifier = stubsClassifier;
		return this;
	}

	public StubRunnerOptionsBuilder withPort(Integer port) {
		String lastStub = this.stubs.peekLast();
		addPort(lastStub + DELIMITER + port);
		return this;
	}

	public StubRunnerOptionsBuilder withPrivateKeyPathname(String privateKeyPathname) {
		this.privateKeyPathname = privateKeyPathname;
		return this;
	}

	public StubRunnerOptionsBuilder withPrivateKeyPassphrase(String privateKeyPassphrase) {
		this.privateKeyPassphrase = privateKeyPassphrase;
		return this;
	}

	/**
	 * @deprecated there is no context path for the stub server
	 */
	@Deprecated
	public StubRunnerOptionsBuilder withContextPath(String contextPath) {
		return this;
	}

	public StubRunnerOptionsBuilder withOptions(StubRunnerOptions options) {
		this.minPortValue = options.minPortValue;
		this.maxPortValue = options.maxPortValue;
		this.stubRepositoryRoot = options.stubRepositoryRoot;
		this.workOffline = options.workOffline;
		this.stubsClassifier = options.stubsClassifier;
		return this;
	}

	public StubRunnerOptions build() {
		return new StubRunnerOptions(this.minPortValue, this.maxPortValue, this.stubRepositoryRoot,
				this.workOffline, this.stubsClassifier, buildDependencies(), this.stubIdsToPortMapping,
				this.username, this.password, this.privateKeyPathname, this.privateKeyPassphrase,
				this.stubRunnerProxyOptions);
	}

	private Collection<StubConfiguration> buildDependencies() {
		return StubsParser.fromString(this.stubs, this.stubsClassifier);
	}

	private static List<String> stubsToList(String[] stubIdsToPortMapping) {
		List<String> list = new ArrayList<>();
		for (String stub : stubIdsToPortMapping) {
			list.addAll(StringUtils.commaDelimitedListToSet(stub));
		}
		return list;
	}

	private void addStub(List<String> notations) {
		for (String notation : notations) {
			addStub(notation);
		}
	}

	private void addStub(String notation) {
		if (StubsParser.hasPort(notation)) {
			addPort(notation);
			this.stubs.add(StubsParser.ivyFromStringWithPort(notation));
		} else {
			this.stubs.add(notation);
		}
	}

	private void addPort(String notation) {
		putStubIdsToPortMapping(StubsParser.fromStringWithPort(notation));
	}

	private void putStubIdsToPortMapping(
			Map<StubConfiguration, Integer> stubIdsToPortMapping) {
		this.stubIdsToPortMapping.putAll(stubIdsToPortMapping);
	}

	public StubRunnerOptionsBuilder withUsername(final String username) {
		this.username = username;
		return this;
	}

	public StubRunnerOptionsBuilder withPassword(final String password) {
		this.password = password;
		return this;
	}

	public StubRunnerOptionsBuilder withProxy(final String proxyHost, final int proxyPort) {
		this.stubRunnerProxyOptions = new StubRunnerOptions.StubRunnerProxyOptions(proxyHost, proxyPort);
		return this;
	}
}
