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

import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.ClientDslProperty
import org.springframework.cloud.contract.spec.internal.Common
import org.springframework.cloud.contract.spec.internal.Cookies
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.FromRequest
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.HttpStatus
import org.springframework.cloud.contract.spec.internal.OptionalProperty
import org.springframework.cloud.contract.spec.internal.PatternValueDslProperty
import org.springframework.cloud.contract.spec.internal.RegexProperty
import org.springframework.cloud.contract.spec.internal.ResponseBodyMatchers
import org.springframework.cloud.contract.spec.internal.ServerDslProperty
import org.springframework.cloud.contract.spec.util.RegexpUtils

/**
 * Represents the response side of the HTTP communication
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@Commons
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeFields = true)
class ResponseGroovy extends Common {

	/**
	 * Allows to configure HTTP headers
	 */
	void headers(@DelegatesTo(ResponseHeaders) Closure closure) {
		this.headers = new ResponseHeaders()
		closure.delegate = headers
		closure()
	}

	/**
	 * Allows to configure HTTP cookies
	 */
	void cookies(@DelegatesTo(ResponseCookies) Closure closure) {
		this.cookies = new ResponseCookies()
		closure.delegate = cookies
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

	/**
	 * Allows to set matchers for the body
	 */
	void bodyMatchers(@DelegatesTo(ResponseBodyMatchers) Closure closure) {
		this.bodyMatchers = new ResponseBodyMatchers()
		closure.delegate = this.bodyMatchers
		closure()
	}

	/**
	 * Allows to reference entries from the request
	 */
	FromRequest fromRequest() {
		return new FromRequest()
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	@Override
	DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		if (client.clientValue instanceof RegexProperty) {
			throw new IllegalStateException("You can't have a regular expression for the response on the client side")
		}
		return super.value(client, server)
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	@Override
	DslProperty value(ServerDslProperty server, ClientDslProperty client) {
		if (client.clientValue instanceof RegexProperty) {
			throw new IllegalStateException("You can't have a regular expression for the response on the client side")
		}
		return super.value(server, client)
	}

	@CompileStatic
	@EqualsAndHashCode(callSuper = true)
	@ToString(includePackage = false)
	private class ServerResponse extends ResponseGroovy {
		ServerResponse(ResponseGroovy request) {
			super(request)
		}
	}

	@CompileStatic
	@EqualsAndHashCode(callSuper = true)
	@ToString(includePackage = false)
	private class ClientResponse extends ResponseGroovy {
		ClientResponse(ResponseGroovy request) {
			super(request)
		}
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class ResponseHeaders extends Headers {

		@Override
		DslProperty matching(String value) {
			return $(p(notEscaped(Pattern.
					compile("${RegexpUtils.escapeSpecialRegexWithSingleEscape(value)}.*"))),
					c(value))
		}
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class ResponseCookies extends Cookies {

		@Override
		DslProperty matching(String value) {
			return $(p(regex("${RegexpUtils.escapeSpecialRegexWithSingleEscape(value)}.*")),
					c(value))
		}
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class ServerPatternValueDslProperty extends PatternValueDslProperty<ServerDslProperty> {

		@Override
		protected ServerDslProperty createProperty(Pattern pattern, Object generatedValue) {
			return new ServerDslProperty(pattern, generatedValue)
		}
	}
}

