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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.stubrunner.util.ResourceUtils;
import org.springframework.cloud.contract.stubrunner.util.StubsParser;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

public class StubRunnerOptionsBuilder {

	private static final String DELIMITER = ":";
	private LinkedList<String> stubs = new LinkedList<>();
	private Collection<StubConfiguration> stubConfigurations = new ArrayList<>();
	private Map<StubConfiguration, Integer> stubIdsToPortMapping = new LinkedHashMap<>();

	private Integer minPortValue = 10000;
	private Integer maxPortValue = 15000;
	private Resource stubRepositoryRoot;
	private String stubsClassifier = "stubs";
	private String username;
	private String password;
	private StubRunnerOptions.StubRunnerProxyOptions stubRunnerProxyOptions;
	private boolean stubsPerConsumer = false;
	private String consumerName;
	private String mappingsOutputFolder;
	private StubRunnerProperties.StubsMode stubsMode;
	private boolean snapshotCheckSkip = false;
	private boolean deleteStubsAfterTest = true;

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

	public StubRunnerOptionsBuilder withStubRepositoryRoot(Resource stubRepositoryRoot) {
		this.stubRepositoryRoot = stubRepositoryRoot;
		return this;
	}

	public StubRunnerOptionsBuilder withStubRepositoryRoot(String stubRepositoryRoot) {
		this.stubRepositoryRoot = ResourceUtils.resource(stubRepositoryRoot);
		return this;
	}

	public StubRunnerOptionsBuilder withStubsMode(StubRunnerProperties.StubsMode stubsMode) {
		this.stubsMode = stubsMode;
		return this;
	}

	public StubRunnerOptionsBuilder withStubsMode(String stubsMode) {
		this.stubsMode = StubRunnerProperties.StubsMode.valueOf(stubsMode);
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

	public StubRunnerOptionsBuilder withOptions(StubRunnerOptions options) {
		this.minPortValue = options.minPortValue;
		this.maxPortValue = options.maxPortValue;
		this.stubRepositoryRoot = options.stubRepositoryRoot;
		this.stubsMode = options.stubsMode;
		this.stubsClassifier = options.stubsClassifier;
		this.username = options.username;
		this.password = options.password;
		this.stubRunnerProxyOptions = options.getStubRunnerProxyOptions();
		this.stubsPerConsumer = options.isStubsPerConsumer();
		this.consumerName = options.getConsumerName();
		this.mappingsOutputFolder = options.getMappingsOutputFolder();
		this.stubConfigurations = options.dependencies != null ?
				options.dependencies : new ArrayList<StubConfiguration>();
		this.stubIdsToPortMapping = options.stubIdsToPortMapping != null ?
				options.stubIdsToPortMapping : new LinkedHashMap<StubConfiguration, Integer>();
		this.snapshotCheckSkip = options.isSnapshotCheckSkip();
		this.deleteStubsAfterTest = options.isDeleteStubsAfterTest();
		return this;
	}

	public StubRunnerOptionsBuilder withMappingsOutputFolder(String mappingsOutputFolder) {
		this.mappingsOutputFolder = mappingsOutputFolder;
		return this;
	}

	public StubRunnerOptionsBuilder withSnapshotCheckSkip(boolean snapshotCheckSkip) {
		this.snapshotCheckSkip = snapshotCheckSkip;
		return this;
	}

	public StubRunnerOptionsBuilder withDeleteStubsAfterTest(boolean deleteStubsAfterTest) {
		this.deleteStubsAfterTest = deleteStubsAfterTest;
		return this;
	}

	public StubRunnerOptions build() {
		return new StubRunnerOptions(this.minPortValue, this.maxPortValue, this.stubRepositoryRoot,
				this.stubsMode, this.stubsClassifier, buildDependencies(), this.stubIdsToPortMapping,
				this.username, this.password, this.stubRunnerProxyOptions, this.stubsPerConsumer, this.consumerName,
				this.mappingsOutputFolder, this.snapshotCheckSkip, this.deleteStubsAfterTest);
	}

	private Collection<StubConfiguration> buildDependencies() {
		List<StubConfiguration> stubConfigurations = StubsParser
				.fromString(this.stubs, this.stubsClassifier);
		this.stubConfigurations.addAll(stubConfigurations);
		return this.stubConfigurations;
	}

	private static List<String> stubsToList(String[] stubIdsToPortMapping) {
		List<String> list = new ArrayList<>();
		if (stubIdsToPortMapping.length == 1 && !containsRange(stubIdsToPortMapping[0])) {
			list.addAll(StringUtils.commaDelimitedListToSet(stubIdsToPortMapping[0]));
			return list;
		} else if (stubIdsToPortMapping.length == 1 && containsRange(stubIdsToPortMapping[0])) {
			LinkedList<String> linkedList = new LinkedList<>();
			String[] split = stubIdsToPortMapping[0].split(",");
			for (String string : split) {
				if (containsClosingRange(string)) {
					String last = linkedList.pop();
					linkedList.push(last + "," + string);
				} else {
					linkedList.push(string);
				}
			}
			list.addAll(linkedList);
			return list;
		}
		Collections.addAll(list, stubIdsToPortMapping);
		return list;
	}

	private static boolean containsRange(String s) {
		return s.contains("[") || s.contains("(");
	}

	private static boolean containsClosingRange(String s) {
		return s.contains("]") || s.contains(")");
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

	public StubRunnerOptionsBuilder withStubPerConsumer(boolean stubPerConsumer) {
		this.stubsPerConsumer = stubPerConsumer;
		return this;
	}

	public StubRunnerOptionsBuilder withConsumerName(String consumerName) {
		this.consumerName = consumerName;
		return this;
	}
}
