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

import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.ClientDslProperty
import org.springframework.cloud.contract.spec.internal.Common
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.PatternValueDslProperty
import org.springframework.cloud.contract.spec.internal.RegexProperty

/**
 * Represents an input for messaging. The input can be a message or some
 * action inside the application.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@Commons
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class InputGroovy extends Common {

	void messageHeaders(@DelegatesTo(Headers) Closure closure) {
		this.messageHeaders = new Headers()
		closure.delegate = messageHeaders
		closure()
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	void stubMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		log.warn("stubMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(closure)
	}

	void bodyMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		this.bodyMatchers = new BodyMatchers()
		closure.delegate = this.bodyMatchers
		closure()
	}

}


