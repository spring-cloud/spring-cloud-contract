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

package org.springframework.cloud.contract.verifier.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents an element of a DSL that can contain client or sever side values
 *
 * @since 1.0.0
 */
@CompileStatic
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeNames = true)
class DslProperty<T> {

	final T clientValue
	final T serverValue

	DslProperty(T clientValue, T serverValue) {
		this.clientValue = clientValue
		this.serverValue = serverValue
	}

	DslProperty(T singleValue) {
		this.clientValue = singleValue
		this.serverValue = singleValue
	}
}
