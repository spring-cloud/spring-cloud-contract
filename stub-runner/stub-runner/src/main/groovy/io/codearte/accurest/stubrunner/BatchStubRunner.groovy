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
	boolean trigger(String ivyNotation, String labelName) {
		boolean triggered = stubRunners.inject(false) { boolean acc, StubRunner stubRunner ->
			boolean success = stubRunner.trigger(ivyNotation, labelName)
			if (acc) {
				return true
			}
			return success
		}
		if (!triggered) {
			throw new IllegalArgumentException("No label with name [$labelName] for " +
					"dependency [$ivyNotation] was found. Here you have the list of dependencies " +
					"and their labels [${ivyToLabels()}")
		}
		return triggered
	}

	private String ivyToLabels() {
		return (getAccurestContracts().collectEntries {
			[(it.key.toColonSeparatedDependencyNotation()) : it.value.collect { it.label }]
		} as Map<String, List<String>>).entrySet().collect {
			"Dependency [${it.key}] has labels ${it.value}]"
		}.join('\n')
	}

	@Override
	boolean trigger(String labelName) {
		boolean triggered = stubRunners.inject(false) { boolean acc, StubRunner stubRunner ->
			boolean success = stubRunner.trigger(labelName)
			if (acc) {
				return true
			}
			return success
		}
		if (!triggered) {
			throw new IllegalArgumentException("No label with name [$labelName] was found. " +
					"Here you have the list of dependencies and their labels [${ivyToLabels()}")
		}
		return triggered
	}

	@Override
	boolean trigger() {
		return stubRunners.inject(false) { boolean acc, StubRunner stubRunner ->
			boolean success = stubRunner.trigger()
			if (acc) {
				return true
			}
			return success
		}
	}

	@Override
	void close() throws IOException {
		stubRunners.each {
			it.close()
		}
	}
}
