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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.cloud.contract.spec.Contract;

/**
 * Manages lifecycle of multiple {@link StubRunner} instances.
 *
 * @see StubRunner
 */
public class BatchStubRunner implements StubRunning {

	private final Iterable<StubRunner> stubRunners;

	public BatchStubRunner(Iterable<StubRunner> stubRunners) {
		this.stubRunners = stubRunners;
	}

	@Override
	public RunningStubs runStubs() {
		Map<StubConfiguration, Integer> map = new LinkedHashMap<>();
		for (StubRunner value : this.stubRunners) {
			RunningStubs runningStubs = value.runStubs();
			map.putAll(runningStubs.validNamesAndPorts());
		}
		return new RunningStubs(map);
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) {
		for (StubRunner stubRunner : this.stubRunners) {
			try {
				return stubRunner.findStubUrl(groupId, artifactId);
			} catch (StubNotFoundException e) {}
		}
		throw new StubNotFoundException(groupId, artifactId);
	}

	@Override
	public URL findStubUrl(String ivyNotation) {
		for (StubRunner stubRunner : this.stubRunners) {
			try {
				return stubRunner.findStubUrl(ivyNotation);
			} catch (StubNotFoundException e) {}
		}
		throw new StubNotFoundException(ivyNotation);
	}

	@Override
	public RunningStubs findAllRunningStubs() {
		Collection<RunningStubs> running = new LinkedHashSet<>();
		for (StubRunner stubRunner : this.stubRunners) {
			running.add(stubRunner.findAllRunningStubs());
		}
		return new RunningStubs(running);
	}

	@Override
	public Map<StubConfiguration, Collection<Contract>> getContracts() {
		Map<StubConfiguration, Collection<Contract>> map = new LinkedHashMap<>();
		for (StubRunner stubRunner : this.stubRunners) {
			for (Entry<StubConfiguration, Collection<Contract>> entry : stubRunner
					.getContracts().entrySet()) {
				if (map.containsKey(entry.getKey())) {
					map.get(entry.getKey()).addAll(entry.getValue());
				}
				else {
					map.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
				}
			}
		}
		return map;
	}

	@Override
	public boolean trigger(String ivyNotation, String labelName) {
		boolean success = false;
		for (StubRunner stubRunner : this.stubRunners) {
			if (stubRunner.trigger(ivyNotation, labelName)) {
				success = true;
			}
		}
		if (!success) {
			throw new IllegalArgumentException("No label with name [" + labelName
					+ "] for " + "dependency [" + ivyNotation
					+ "] was found. Here you have the list of dependencies "
					+ "and their labels [" + ivyToLabels() + "]");
		}
		return success;
	}

	private String ivyToLabels() {
		StringBuilder builder = new StringBuilder();
		for (Entry<String, Collection<String>> entry : labels().entrySet()) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append("Dependency [").append(entry.getKey()).append("] has labels ")
					.append(entry.getValue());
		}
		return builder.toString();
	}

	@Override
	public boolean trigger(String labelName) {
		boolean success = false;
		for (StubRunner stubRunner : this.stubRunners) {
			if (stubRunner.trigger(labelName)) {
				success = true;
			}
		}
		if (!success) {
			throw new IllegalArgumentException(
					"No label with name [" + labelName + "] was found. "
							+ "Here you have the list of dependencies and their labels ["
							+ ivyToLabels() + "]");
		}
		return success;
	}

	@Override
	public boolean trigger() {
		boolean success = false;
		for (StubRunner stubRunner : this.stubRunners) {
			if (stubRunner.trigger()) {
				success = true;
			}
		}
		return success;
	}

	@Override
	public Map<String, Collection<String>> labels() {
		Map<String, Collection<String>> map = new LinkedHashMap<>();
		for (StubRunner stubRunner : this.stubRunners) {
			for (Entry<String, Collection<String>> entry : stubRunner.labels()
					.entrySet()) {
				if (map.containsKey(entry.getKey())) {
					map.get(entry.getKey()).addAll(entry.getValue());
				}
				else {
					map.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
				}
			}
		}
		return map;
	}

	@Override
	public void close() throws IOException {
		for (StubRunner stubRunner : this.stubRunners) {
			stubRunner.close();
		}
	}
}
