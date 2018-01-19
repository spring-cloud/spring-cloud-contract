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

package org.springframework.cloud.contract.verifier.util;

import java.util.LinkedList;

import com.toomuchcoding.jsonassert.JsonVerifiable;

/**
 * Helper class that represents a finished assertion of a JSON.
 * Contains a list of all necessary method calls to assert the JSON.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class FinishedDelegatingJsonVerifiable extends DelegatingJsonVerifiable {

	final String keyBeforeChecking;

	FinishedDelegatingJsonVerifiable(String keyBeforeChecking, JsonVerifiable delegate,
			LinkedList<String> methodsBuffer) {
		super(delegate, methodsBuffer);
		this.keyBeforeChecking = keyBeforeChecking;
	}

	FinishedDelegatingJsonVerifiable(JsonVerifiable delegate,
			LinkedList<String> methodsBuffer) {
		super(delegate, methodsBuffer);
		this.keyBeforeChecking = delegate.jsonPath();
	}

	@Override public String keyBeforeChecking() {
		return this.keyBeforeChecking;
	}
}
