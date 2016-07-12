/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.stubrunner.util.StubsParser

@CompileStatic
class StubRunnerOptionsBuilder {
	private static final String DELIMITER = ':'
	private LinkedList<String> stubs = new LinkedList<>()
	private Map<StubConfiguration, Integer> stubIdsToPortMapping = [:]

	private Integer minPortValue = 10000
	private Integer maxPortValue = 15000
	private String stubRepositoryRoot
	private boolean workOffline = false
	private String stubsClassifier = 'stubs'

	StubRunnerOptionsBuilder() {
	}

	StubRunnerOptionsBuilder(StubRunnerOptions options) {
		withOptions(options)
	}

	StubRunnerOptionsBuilder withStubs(String stubs) {
		addStub(stubsToList(stubs))
		return this
	}

	StubRunnerOptionsBuilder withStubs(List<String> stubs) {
		for (String stub : stubs) {
			withStubs(stub)
		}
		return this
	}

	StubRunnerOptionsBuilder withMinMaxPort(Integer minPortValue, Integer maxPortValue) {
		this.minPortValue = minPortValue
		this.maxPortValue = maxPortValue
		return this
	}

	StubRunnerOptionsBuilder withMinPort(int minPortValue) {
		this.minPortValue = minPortValue
		return this
	}

	StubRunnerOptionsBuilder withMaxPort(int maxPortValue) {
		this.maxPortValue = maxPortValue
		return this
	}

	StubRunnerOptionsBuilder withStubRepositoryRoot(String stubRepositoryRoot) {
		this.stubRepositoryRoot = stubRepositoryRoot
		return this
	}

	StubRunnerOptionsBuilder withWorkOffline(boolean workOffline) {
		this.workOffline = workOffline
		return this
	}
	
	StubRunnerOptionsBuilder withStubsClassifier(String stubsClassifier) {
		this.stubsClassifier = stubsClassifier
		return this
	}

	StubRunnerOptionsBuilder withPort(Integer port) {
		String lastStub = stubs.peekLast()
		println "PORT  $lastStub -> $port"
		addPort(lastStub + DELIMITER + port)
		return this
	}

	StubRunnerOptionsBuilder withOptions(StubRunnerOptions options) {
		this.minPortValue = options.minPortValue
		this.maxPortValue = options.maxPortValue
		this.stubRepositoryRoot = options.stubRepositoryRoot
		this.workOffline = options.workOffline
		this.stubsClassifier = options.stubsClassifier
		return this
	}

	StubRunnerOptions build() {
		return new StubRunnerOptions(minPortValue, maxPortValue, stubRepositoryRoot, workOffline, stubsClassifier, buildDependencies(), stubIdsToPortMapping)
	}

	private Collection<StubConfiguration> buildDependencies() {
		return StubsParser.fromString(stubs, stubsClassifier)
	}

	private static List<String> stubsToList(String stubIdsToPortMapping) {
		return stubIdsToPortMapping.split(',').collect { it } as List<String>
	}

	private void addStub(List<String> notations) {
		for (String notation : notations) {
			addStub(notation)
		}
	}

	private void addStub(String notation) {
		if (StubsParser.hasPort(notation)) {
			addPort(notation)
			stubs.add(StubsParser.ivyFromStringWithPort(notation))
		} else {
			stubs.add(notation)
		}
	}

	private void addPort(String notation) {
		putStubIdsToPortMapping(StubsParser.fromStringWithPort(notation))
	}

	private void putStubIdsToPortMapping(Map<StubConfiguration, Integer> stubIdsToPortMapping) {
		this.stubIdsToPortMapping.putAll(stubIdsToPortMapping)
	}

}
