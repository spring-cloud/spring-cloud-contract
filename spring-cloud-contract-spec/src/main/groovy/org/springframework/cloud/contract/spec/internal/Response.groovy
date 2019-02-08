/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.spec.internal

import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Commons

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
class Response extends Common {

	@Delegate
	ServerPatternValueDslProperty property = new ServerPatternValueDslProperty()
	@Delegate
	HttpStatus httpStatus = new HttpStatus()

	DslProperty status
	DslProperty delay
	Headers headers
	Cookies cookies
	Body body
	boolean async
	ResponseBodyMatchers bodyMatchers

	Response() {
	}

	Response(Response response) {
		this.status = response.status
		this.headers = response.headers
		this.cookies = response.cookies
		this.body = response.body
	}

	/**
	 * Allows to set the HTTP status
	 */
	void status(int status) {
		this.status = toDslProperty(status)
	}

	/**
	 * Allows to set the HTTP status
	 */
	void status(DslProperty status) {
		this.status = toDslProperty(status)
	}

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
	 * Allows set an HTTP body
	 */
	void body(Map<String, Object> body) {
		this.body = new Body(convertObjectsToDslProperties(body))
	}

	/**
	 * Allows set an HTTP body
	 */
	void body(List body) {
		this.body = new Body(convertObjectsToDslProperties(body))
	}

	/**
	 * Allows set an HTTP body
	 */
	void body(Object bodyAsValue) {
		if (bodyAsValue instanceof List) {
			body(bodyAsValue as List)
		}
		else {
			this.body = new Body(bodyAsValue)
		}
	}

	/**
	 * Allows to set a fixed delay of the response in milliseconds
	 */
	void fixedDelayMilliseconds(int timeInMilliseconds) {
		this.delay = toDslProperty(timeInMilliseconds)
	}

	/**
	 * Turns on the asynchronous mode for this contract. Used with MockMvc and the
	 * Servlet 3.0 features
	 */
	void async() {
		this.async = true
	}

	void assertThatSidesMatch(OptionalProperty stubSide, Object testSide) {
		throw new IllegalStateException("Optional can be used only in the test side of the response!")
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	DslProperty value(ServerDslProperty server) {
		Object dynamicValue = server.serverValue
		Object concreteValue = server.clientValue
		if (dynamicValue instanceof RegexProperty && server.isSingleValue()) {
			return ((RegexProperty) dynamicValue).concreteClientDynamicProducer()
		}
		return new DslProperty(concreteValue, dynamicValue)
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	DslProperty $(ServerDslProperty server) {
		return value(server)
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	DslProperty value(Pattern server) {
		return value(new RegexProperty(server))
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	DslProperty value(RegexProperty server) {
		return value(new ServerDslProperty(server))
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	DslProperty $(RegexProperty server) {
		return value(server)
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	DslProperty $(Pattern server) {
		return value(new RegexProperty(server))
	}

	@Override
	RegexProperty regexProperty(Object object) {
		return new RegexProperty(object).concreteClientDynamicProducer()
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
	private class ServerResponse extends Response {
		ServerResponse(Response request) {
			super(request)
		}
	}

	@CompileStatic
	@EqualsAndHashCode(callSuper = true)
	@ToString(includePackage = false)
	private class ClientResponse extends Response {
		ClientResponse(Response request) {
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

