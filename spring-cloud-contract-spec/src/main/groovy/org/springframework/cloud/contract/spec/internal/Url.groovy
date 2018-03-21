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

import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.codehaus.groovy.runtime.GStringImpl
import repackaged.nl.flotsam.xeger.Xeger

import static org.springframework.cloud.contract.spec.util.ValidateUtils.validateServerValueIsAvailable

/**
 * Represents a URL that may contain query parameters
 *
 * @since 1.0.0
 */
@ToString(includePackage = false, includeNames = true, includeSuper = true)
@EqualsAndHashCode(includeFields = true, callSuper = true)
@CompileStatic
class Url extends DslProperty {

	QueryParameters queryParameters

	Url(DslProperty prop) {
		super(prop.clientValue, prop.serverValue)
		validateServerValueIsAvailable(prop.serverValue, "Url")
	}

	Url(Object url) {
		super(url, testUrl(url))
		validateServerValueIsAvailable(url, "Url")
	}

	private static Object testUrl(Object url) {
		if (url instanceof GString) {
			boolean anyPattern = url.values.any { it instanceof Pattern }
			if (!anyPattern) {
				return url
			}
			String newUrl = new GStringImpl(
					url.values.collect { it instanceof Pattern ?
							new Xeger(it.pattern()).generate() : it
					} as String[],
					url.strings.clone() as String[]
			).toString()
			return new Url(newUrl)
		}
		return url
	}

	void queryParameters(@DelegatesTo(QueryParameters) Closure closure) {
		this.queryParameters = new QueryParameters()
		closure.delegate = queryParameters
		closure()
	}

}
