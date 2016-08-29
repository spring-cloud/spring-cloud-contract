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

package org.springframework.cloud.contract.stubrunner;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Structure representing executed stubs. Contains the configuration of each stub together
 * with the port on which its executed.
 */
public class RunningStubs {

	final private Map<StubConfiguration, Integer> namesAndPorts = new LinkedHashMap<>();

	public RunningStubs(Map<StubConfiguration, Integer> map) {
		this.namesAndPorts.putAll(map);
	}

	public RunningStubs(Collection<RunningStubs> runningStubs) {
		for (RunningStubs it : runningStubs) {
			this.namesAndPorts.putAll(it.namesAndPorts);
		}
	}

	public Integer getPort(String artifactId) {
		return getEntry(artifactId) == null ? null : getEntry(artifactId).getValue();
	}

	public Map.Entry<StubConfiguration, Integer> getEntry(String artifactId) {
		for (Entry<StubConfiguration, Integer> it : this.namesAndPorts.entrySet()) {
			if (it.getKey().matchesIvyNotation(artifactId)) {
				return it;
			}
		}
		return null;
	}

	public Integer getPort(String groupId, String artifactId) {
		for (Entry<StubConfiguration, Integer> it : this.namesAndPorts.entrySet()) {
			if (it.getKey().matchesIvyNotation(groupId + ":" + artifactId)) {
				return it.getValue();
			}
		}
		return null;
	}

	public boolean isPresent(String artifactId) {
		return getEntry(artifactId) != null;
	}

	public boolean isPresent(String groupId, String artifactId) {
		return getPort(groupId, artifactId) != null;
	}

	public Set<StubConfiguration> getAllServices() {
		return this.namesAndPorts.keySet();
	}

	public Set<String> getAllServicesNames() {
		Set<String> result = new LinkedHashSet<>();
		for (Entry<StubConfiguration, Integer> it : this.namesAndPorts.entrySet()) {
			result.add(it.getKey().artifactId);
		}
		return result;
	}

	public Map<String, Integer> toIvyToPortMapping() {
		Map<String, Integer> result = new LinkedHashMap<>();
		for (Entry<StubConfiguration, Integer> it : this.namesAndPorts.entrySet()) {
			result.put(it.getKey().toColonSeparatedDependencyNotation(), it.getValue());
		}
		return result;
	}

	public Map<StubConfiguration, Integer> validNamesAndPorts() {
		Map<StubConfiguration, Integer> result = new LinkedHashMap<>();
		for (Entry<StubConfiguration, Integer> it : this.namesAndPorts.entrySet()) {
			if (it.getValue() != null && it.getValue() >= 0) {
				result.put(it.getKey(), it.getValue());
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "RunningStubs [namesAndPorts=" + this.namesAndPorts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.namesAndPorts == null) ? 0 : this.namesAndPorts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RunningStubs other = (RunningStubs) obj;
		if (this.namesAndPorts == null) {
			if (other.namesAndPorts != null)
				return false;
		}
		else if (!this.namesAndPorts.equals(other.namesAndPorts))
			return false;
		return true;
	}

}