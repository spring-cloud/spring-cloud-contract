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

package org.springframework.cloud.contract.verifier.util;

/**
 * Utility class that wraps an object that should be traversed
 * when building a list of methods to execute in the generated test.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class ShouldTraverse {
	final Object value;

	ShouldTraverse(Object value) {
		this.value = value;
	}
}
