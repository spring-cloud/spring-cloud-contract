package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.ToString

/**
 * Arguments passed to the {@link StubRunner} application
 *
 * @see StubRunner
 */
@CompileStatic
@ToString(includeNames = true)
@PackageScope
class Arguments {
	final StubRunnerOptions stubRunnerOptions
	final String context
	final String repositoryPath
	final StubConfiguration stub

	Arguments(StubRunnerOptions stubRunnerOptions, String repositoryPath = "", StubConfiguration stub = null) {
		this.stubRunnerOptions = stubRunnerOptions
		this.context = context
		this.repositoryPath = repositoryPath
		this.stub = stub
	}
}
