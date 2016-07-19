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
import java.util.Map;


/**
 * Technical options related to running StubRunner
 *
 * Use {@class StubRunnerOptionsBuilder} to build this object.
 *
 * @see StubRunnerOptionsBuilder
 */
public class StubRunnerOptions {

	/**
	 * min port value of the WireMock instance for the given collaborator
	 */
	final Integer minPortValue;

	/**
	 * max port value of the WireMock instance for the given collaborator
	 */
	final Integer maxPortValue;

	/**
	 * root URL from where the JAR with stub mappings will be downloaded
	 */
	final String stubRepositoryRoot;

	/**
	 * avoids local repository in dependency resolution
	 */
	final boolean workOffline;

	/**
	 * stub definition classifier
	 */
	final String stubsClassifier;

	final Collection<StubConfiguration> dependencies;

	/**
	 * colon separated list of ids to the desired port
	 */
	final Map<StubConfiguration, Integer> stubIdsToPortMapping;

	public StubRunnerOptions(Integer minPortValue, Integer maxPortValue, String stubRepositoryRoot,
	                  boolean workOffline, String stubsClassifier, Collection<StubConfiguration> dependencies, Map<StubConfiguration, Integer> stubIdsToPortMapping) {
		this.minPortValue = minPortValue;
		this.maxPortValue = maxPortValue;
		this.stubRepositoryRoot = stubRepositoryRoot;
		this.workOffline = workOffline;
		this.stubsClassifier = stubsClassifier;
		this.dependencies = dependencies;
		this.stubIdsToPortMapping = stubIdsToPortMapping;
	}

	public Integer port(StubConfiguration stubConfiguration) {
		if (stubIdsToPortMapping!=null) {
			return stubIdsToPortMapping.get(stubConfiguration);
		} else {
			return null;
		}
	}

	public Integer getMinPortValue() {
		return minPortValue;
	}

	public Integer getMaxPortValue() {
		return maxPortValue;
	}

	public String getStubRepositoryRoot() {
		return stubRepositoryRoot;
	}

	public boolean isWorkOffline() {
		return workOffline;
	}

	public String getStubsClassifier() {
		return stubsClassifier;
	}

	public Collection<StubConfiguration> getDependencies() {
		return dependencies;
	}

	public Map<StubConfiguration, Integer> getStubIdsToPortMapping() {
		return stubIdsToPortMapping;
	}

	@Override
	public String toString() {
		return "StubRunnerOptions [minPortValue=" + minPortValue + ", maxPortValue="
				+ maxPortValue + ", stubRepositoryRoot=" + stubRepositoryRoot
				+ ", workOffline=" + workOffline + ", stubsClassifier=" + stubsClassifier
				+ ", dependencies=" + dependencies + ", stubIdsToPortMapping="
				+ stubIdsToPortMapping + "]";
	}

}
