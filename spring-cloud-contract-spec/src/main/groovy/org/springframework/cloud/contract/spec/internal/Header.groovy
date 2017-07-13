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

package org.springframework.cloud.contract.spec.internal
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a header of a request / response or a message
 *
 * @since 1.0.0
 */
@EqualsAndHashCode(includeFields = true, callSuper = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true, includeSuper = true)
@CompileStatic
class Header extends DslProperty {

	String name

	Header(String name, DslProperty dslProperty) {
		super(dslProperty.clientValue, dslProperty.serverValue)
		this.name = name
	}

	Header(String name, MatchingStrategy value) {
		super(value)
		this.name = name
	}

	Header(String name, Object value) {
		super(value)
		this.name = name
	}

}
