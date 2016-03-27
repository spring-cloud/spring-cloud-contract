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
	final Map<StubConfiguration, Integer> namesAndPorts = [:]

	RunningStubs(Map<StubConfiguration, Integer> map) {
		this.namesAndPorts.putAll(map)
	}

	RunningStubs(Collection<RunningStubs> runningStubs) {
		runningStubs.each {
			this.namesAndPorts.putAll(it.namesAndPorts)
		}
	}

	Integer getPort(String artifactId) {
		def strings = artifactId.split(':')
		if (strings.length == 1) {
			return namesAndPorts.entrySet().find {
				it.key.artifactId == artifactId
			}?.value
		} else if(strings.length == 2) {
			return namesAndPorts.entrySet().find {
				it.key.groupId == strings[0] && it.key.artifactId == strings[1]
			}?.value
		}
		return namesAndPorts.entrySet().find {
			it.key.groupId == strings[0] &&
					it.key.artifactId == strings[1] &&
					it.key.classifier == strings[2]
		}?.value
	}

	Integer getPort(String groupId, String artifactId) {
		return namesAndPorts.entrySet().find {
			it.key.artifactId == artifactId && it.key.groupId == groupId
		}?.value
	}

	boolean isPresent(String artifactId) {
		def strings = artifactId.split(':')
		if (strings.length == 1) {
			return namesAndPorts.entrySet().find {
				it.key.artifactId == artifactId
			}
		} else if(strings.length == 2) {
			return namesAndPorts.entrySet().find {
				it.key.groupId == strings[0] && it.key.artifactId == strings[1]
			}
		}
		return namesAndPorts.entrySet().find {
			it.key.groupId == strings[0] &&
					it.key.artifactId == strings[1] &&
					it.key.classifier == strings[2]
		}
	}

	boolean isPresent(String groupId, String artifactId) {
		return namesAndPorts.entrySet().find {
			it.key.artifactId == artifactId && it.key.groupId == groupId
		}
	}

	@Override
	String toString() {
		return namesAndPorts.collect {
			"Stub [${it.key.toColonSeparatedDependencyNotation()}] is running on port [${it.value}]"
		}.join("\n")
	}
}