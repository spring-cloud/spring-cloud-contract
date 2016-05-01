package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import io.codearte.accurest.stubrunner.util.StubsParser

@CompileStatic
class StubRunnerOptionsBuilder {
	private static final String DELIMITER = ":";
	private LinkedList<String> stubs = new LinkedList<>()
	private Map<StubConfiguration, Integer> stubIdsToPortMapping = [:]

	private Integer minPortValue = 10000
	private Integer maxPortValue = 15000
	private String stubRepositoryRoot
	private boolean workOffline = false
	private String stubsClassifier = "stubs"

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
			withStubs(stub);
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
		String lastStub = stubs.peekLast();
		println "PORT  $lastStub -> $port"
		addPort(lastStub + DELIMITER + port);
		return this;
	}

	StubRunnerOptions build() {
		return new StubRunnerOptions(minPortValue, maxPortValue, stubRepositoryRoot, workOffline, stubsClassifier, buildDependencies(), stubIdsToPortMapping)
	}

	void withOptions(StubRunnerOptions options) {
		this.minPortValue = options.minPortValue
		this.maxPortValue = options.maxPortValue
		this.stubRepositoryRoot = options.stubRepositoryRoot
		this.workOffline = options.workOffline
		this.stubsClassifier = options.stubsClassifier
	}

	private Collection<StubConfiguration> buildDependencies() {
		return StubsParser.fromString(stubs, stubsClassifier);
	}

	private static List<String> stubsToList(String stubIdsToPortMapping) {
		return stubIdsToPortMapping.split(',').collect { it }
	}

	private void addStub(List<String> notations) {
		for (String notation : notations) {
			addStub(notation);
		}
	}

	private void addStub(String notation) {
		if (StubsParser.hasPort(notation)) {
			addPort(notation);
			stubs.add(StubsParser.ivyFromStringWithPort(notation));
		} else {
			stubs.add(notation);
		}
	}

	private void addPort(String notation) {
		putStubIdsToPortMapping(StubsParser.fromStringWithPort(notation));
	}

	private void putStubIdsToPortMapping(Map<StubConfiguration, Integer> stubIdsToPortMapping) {
		this.stubIdsToPortMapping.putAll(stubIdsToPortMapping)
	}

}
