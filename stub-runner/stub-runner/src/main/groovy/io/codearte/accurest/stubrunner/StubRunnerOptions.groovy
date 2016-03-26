package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.ToString

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
	 * stub definition suffix
	 */
	String stubsClassifier = "stubs"

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
}
