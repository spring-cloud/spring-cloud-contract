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
		return getEntry(artifactId)?.value
	}

	Map.Entry<StubConfiguration, Integer> getEntry(String artifactId) {
		return namesAndPorts.entrySet().find {
			it.key.matchesIvyNotation(artifactId)
		}
	}

	Integer getPort(String groupId, String artifactId) {
		return namesAndPorts.entrySet().find {
			it.key.matchesIvyNotation("$groupId:$artifactId")
		}?.value
	}

	boolean isPresent(String artifactId) {
		return getEntry(artifactId)
	}

	boolean isPresent(String groupId, String artifactId) {
		return namesAndPorts.entrySet().find {
			it.key.matchesIvyNotation("$groupId:$artifactId")
		}
	}

	Set<StubConfiguration> getAllServices() {
		return namesAndPorts.keySet()
	}

	Set<String> getAllServicesNames() {
		return namesAndPorts.keySet().collect { it.artifactId } as Set
	}

	Map<String, Integer> toIvyToPortMapping() {
		return namesAndPorts.collectEntries { [(it.key.toColonSeparatedDependencyNotation()) : it.value]  } as Map<String, Integer>
	}

	@Override
	String toString() {
		return namesAndPorts.collect {
			"Stub [${it.key.toColonSeparatedDependencyNotation()}] is running on port [${it.value}]"
		}.join("\n")
	}
}