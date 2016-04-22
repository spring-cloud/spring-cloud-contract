package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import io.codearte.accurest.dsl.GroovyDsl
/**
 * Manages lifecycle of multiple {@link StubRunner} instances.
 *
 * @see StubRunner
 */
@CompileStatic
class BatchStubRunner implements StubRunning {

	private final Iterable<StubRunner> stubRunners

	BatchStubRunner(Iterable<StubRunner> stubRunners) {
		this.stubRunners = stubRunners
	}

	@Override
	RunningStubs runStubs() {
		Map<StubConfiguration, Integer> appsAndPorts = stubRunners.inject([:]) { Map<StubConfiguration, Integer> acc, StubRunner value ->
			acc.putAll(value.runStubs().namesAndPorts)
			return acc
		} as Map<StubConfiguration, Integer>
		return new RunningStubs(appsAndPorts)
	}

	@Override
	URL findStubUrl(String groupId, String artifactId) {
		return stubRunners.findResult(null) { StubRunner stubRunner ->
			return stubRunner.findStubUrl(groupId, artifactId)
		} as URL
	}

	@Override
	URL findStubUrl(String ivyNotation) {
		String[] splitString = ivyNotation.split(":")
		if (splitString.length > 3) {
			throw new IllegalArgumentException("$ivyNotation is invalid")
		} else if (splitString.length == 2) {
			return findStubUrl(splitString[0], splitString[1])
		}
		return findStubUrl(null, splitString[0])
	}

	@Override
	RunningStubs findAllRunningStubs() {
		return new RunningStubs(stubRunners.collect { StubRunner runner -> runner.findAllRunningStubs() })
	}

	@Override
	Map<StubConfiguration, Collection<GroovyDsl>> getAccurestContracts() {
		return stubRunners.inject([:]) { Map<StubConfiguration, Collection<GroovyDsl>> map, StubRunner stubRunner ->
			map.putAll(stubRunner.accurestContracts)
			return map
		} as Map<StubConfiguration, Collection<GroovyDsl>>
	}

	@Override
	void trigger(String ivyNotation, String labelName) {
		stubRunners.each { it.trigger(ivyNotation, labelName) }
	}

	@Override
	void trigger(String labelName) {
		stubRunners.each { it.trigger(labelName) }
	}

	@Override
	void trigger() {
		stubRunners.each { it.trigger() }
	}

	@Override
	void close() throws IOException {
		stubRunners.each {
			it.close()
		}
	}
}
