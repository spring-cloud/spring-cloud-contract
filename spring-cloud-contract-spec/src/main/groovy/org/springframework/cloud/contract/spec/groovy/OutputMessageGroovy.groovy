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

package org.springframework.cloud.contract.spec.groovy

import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Commons

import org.springframework.cloud.contract.spec.internal.ClientDslProperty
import org.springframework.cloud.contract.spec.internal.Common
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.OptionalProperty
import org.springframework.cloud.contract.spec.internal.PatternValueDslProperty
import org.springframework.cloud.contract.spec.internal.RegexProperty
import org.springframework.cloud.contract.spec.internal.ResponseBodyMatchers
import org.springframework.cloud.contract.spec.internal.ServerDslProperty

/**
 * Represents an output for messaging. Used for verifying
 * the body and headers that are sent.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@Commons
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class OutputMessageGroovy extends Common {

	void headers(@DelegatesTo(Headers) Closure closure) {
		this.headers = new Headers()
		closure.delegate = headers
		closure()
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	void testMatchers(@DelegatesTo(ResponseBodyMatchers) Closure closure) {
		log.warn("testMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(closure)
	}

	void bodyMatchers(@DelegatesTo(ResponseBodyMatchers) Closure closure) {
		this.bodyMatchers = new ResponseBodyMatchers()
		closure.delegate = this.bodyMatchers
		closure()
	}
}



