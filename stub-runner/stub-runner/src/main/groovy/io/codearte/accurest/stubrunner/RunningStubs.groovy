package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

/**
 * Structure representing executed stubs. Contains the configuration of each stub
 * together with the port on which its executed.
 */
@EqualsAndHashCode
@CompileStatic
class RunningStubs {
	final Map<StubConfiguration, Integer> namesAndPorts

	RunningStubs(Map<StubConfiguration, Integer> map) {
		this.namesAndPorts = map
	}

	@Override
	String toString() {
		return namesAndPorts.collect {
			"Stub [${it.key.toColonSeparatedDependencyNotation()}] is running on port [${it.value}]"
		}.join("\n")
	}
}