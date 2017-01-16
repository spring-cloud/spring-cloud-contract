/*
 *  Copyright 2013-2017 the original author or authors.
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

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.springframework.cloud.contract.spec.Contract;

public interface StubFinder extends StubTrigger {
	/**
	 * For the given groupId and artifactId tries to find the matching
	 * URL of the running stub.
	 *
	 * @param groupId - might be null. In that case a search only via artifactId takes place
	 * @return URL of a running stub or throws exception if not found
	 */
	URL findStubUrl(String groupId, String artifactId) throws StubNotFoundException;

	/**
	 * For the given Ivy notation {@code [groupId]:artifactId:[version]:[classifier]} tries to
	 * find the matching URL of the running stub. You can also pass only {@code artifactId}.
	 *
	 * @param ivyNotation - Ivy representation of the Maven artifact
	 * @return URL of a running stub or throws exception if not found
	 */
	URL findStubUrl(String ivyNotation) throws StubNotFoundException;

	/**
	 * Returns all running stubs
	 */
	RunningStubs findAllRunningStubs();

	/**
	 * Returns the list of Contracts
	 */
	Map<StubConfiguration, Collection<Contract>> getContracts();
}