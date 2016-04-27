package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.codearte.accurest.stubrunner.util.StubsParser

/**
 * Technical options related to running StubRunner
 */
@ToString(includeNames = true)
@CompileStatic
class StubRunnerOptions {

	/**
	 * min port value of the WireMock instance for the given collaborator
	 */
	Integer minPortValue = 10000

	/**
	 * max port value of the WireMock instance for the given collaborator
	 */
	Integer maxPortValue = 15000

	/**
	 * root URL from where the JAR with stub mappings will be downloaded
	 */
	String stubRepositoryRoot

	/**
	 * avoids local repository in dependency resolution
	 */
	boolean workOffline = false

	/**
	 * colon separated list of ids to the desired port
	 */
	Map<StubConfiguration, Integer> stubIdsToPortMapping = [:]

	/**
	 * stub definition suffix
	 */
	String stubsClassifier = "stubs"

	StubRunnerOptions(Integer minPortValue, Integer maxPortValue, String stubRepositoryRoot,
					  boolean workOffline, String stubsClassifier, String stubIdsToPortMapping) {
		this.minPortValue = minPortValue
		this.maxPortValue = maxPortValue
		this.stubRepositoryRoot = stubRepositoryRoot
		this.workOffline = workOffline
		this.stubsClassifier = stubsClassifier
		this.stubIdsToPortMapping = stubIdsWithPortsFromString(stubIdsToPortMapping)
	}

	StubRunnerOptions(Integer minPortValue, Integer maxPortValue, String stubRepositoryRoot,
					  boolean workOffline, String stubsClassifier) {
		this.minPortValue = minPortValue
		this.maxPortValue = maxPortValue
		this.stubRepositoryRoot = stubRepositoryRoot
		this.workOffline = workOffline
		this.stubsClassifier = stubsClassifier
	}

	StubRunnerOptions(String stubRepositoryRoot) {
		this.stubRepositoryRoot = stubRepositoryRoot
	}

	StubRunnerOptions() {}

	Map<StubConfiguration, Integer> stubIdsWithPortsFromString(String stubIdsToPortMapping) {
		return stubIdsToPortMapping.split(',').collectEntries { String entry ->
			return StubsParser.fromStringWithPort(entry)
		}
	}

	Integer port(StubConfiguration stubConfiguration) {
		return stubIdsToPortMapping[stubConfiguration]
	}

	void setStubIdsToPortMapping(Map<StubConfiguration, Integer> stubIdsToPortMapping) {
		this.stubIdsToPortMapping = stubIdsToPortMapping
	}

	void putStubIdsToPortMapping(Map<StubConfiguration, Integer> stubIdsToPortMapping) {
		this.stubIdsToPortMapping.putAll(stubIdsToPortMapping)
	}

	void setStubIdsToPortMapping(String stubIdsToPortMapping) {
		this.stubIdsToPortMapping = stubIdsWithPortsFromString(stubIdsToPortMapping)
	}
}
