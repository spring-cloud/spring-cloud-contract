/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util.xml;

/**
 * Helper interface describing the process of current iteration.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public interface IteratingOverArray {

	/**
	 * @return {@code true} if is in progress of iterating over an array.
	 */
	boolean isIteratingOverArray();

	/**
	 * @return {@code true} if current element is a particular value on which concrete
	 * assertion will take place.
	 */
	boolean isAssertingAValueInArray();

}
