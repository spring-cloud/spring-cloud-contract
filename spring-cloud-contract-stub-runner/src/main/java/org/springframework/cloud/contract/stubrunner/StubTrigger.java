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

import java.util.Collection;
import java.util.Map;

public interface StubTrigger {

	/**
	 * Triggers an event by a given label for a given {@code groupid:artifactid} notation. You can use only {@code artifactId} too.
	 *
	 * Feature related to messaging.
	 *
	 * @return true - if managed to run a trigger
	 */
	boolean trigger(String ivyNotation, String labelName);

	/**
	 * Triggers an event by a given label.
	 *
	 * Feature related to messaging.
	 *
	 * @return true - if managed to run a trigger
	 */
	boolean trigger(String labelName);

	/**
	 * Triggers all possible events.
	 *
	 * Feature related to messaging.
	 *
	 * @return true - if managed to run a trigger
	 */
	boolean trigger();

	/**
	 * Returns a mapping of ivy notation of a dependency to all the labels it has.
	 *
	 * Feature related to messaging.
	 */
	Map<String, Collection<String>> labels();
}