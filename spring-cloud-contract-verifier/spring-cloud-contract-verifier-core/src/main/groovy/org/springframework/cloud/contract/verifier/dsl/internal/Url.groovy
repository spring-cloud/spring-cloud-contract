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

import static org.springframework.cloud.contract.verifier.util.ValidateUtils.validateServerValueIsAvailable

/**
 * Represents a URL that may contain query parameters
 *
 * @since 1.0.0
 */
@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class Url extends DslProperty {

	QueryParameters queryParameters

	Url(DslProperty prop) {
		super(prop.clientValue, prop.serverValue)
		validateServerValueIsAvailable(prop.serverValue, "Url")
	}

	Url(Object url) {
		super(url)
		validateServerValueIsAvailable(url, "Url")
	}

	void queryParameters(@DelegatesTo(QueryParameters) Closure closure) {
		this.queryParameters = new QueryParameters()
		closure.delegate = queryParameters
		closure()
	}

}
