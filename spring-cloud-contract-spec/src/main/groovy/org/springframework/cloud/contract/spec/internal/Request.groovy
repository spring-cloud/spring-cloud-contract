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
 * Represents the request side of the HTTP communication
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@Commons
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Request extends Common {

	@Delegate
	ClientPatternValueDslProperty property = new ClientPatternValueDslProperty()
	@Delegate
	HttpMethods httpMethods = new HttpMethods()

	DslProperty method
	Url url
	UrlPath urlPath
	Headers headers
	Cookies cookies
	Body body
	Multipart multipart
	BodyMatchers bodyMatchers

	Request() {
	}

	Request(Request request) {
		this.method = request.method
		this.url = request.url
		this.urlPath = request.urlPath
		this.headers = request.headers
		this.cookies = request.cookies
		this.body = request.body
		this.multipart = request.multipart
	}

	/**
	 * Name of the HTTP method
	 */
	void method(String method) {
		this.method = toDslProperty(method)
	}

	/**
	 * Name of the HTTP method
	 */
	void method(HttpMethods.HttpMethod httpMethod) {
		this.method = toDslProperty(httpMethod.toString())
	}

	/**
	 * Name of the HTTP method
	 */
	void method(DslProperty method) {
		this.method = toDslProperty(method)
	}

	/**
	 * URL to which the request will be sent
	 */
	void url(Object url) {
		this.url = new Url(url)
	}

	/**
	 * URL to which the request will be sent
	 */
	void url(DslProperty url) {
		this.url = new Url(url)
	}

	void url(Object url, @DelegatesTo(UrlPath) Closure closure) {
		this.url = new Url(url)
		closure.delegate = this.url
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	void url(DslProperty url, @DelegatesTo(UrlPath) Closure closure) {
		this.url = new Url(url)
		closure.delegate = this.url
		closure()
	}

	/**
	 * URL to which the request will be sent
	 */
	void urlPath(String path) {
		this.urlPath = new UrlPath(path)
	}

	/**
	 * URL to which the request will be sent
	 */
	void urlPath(GString path) {
		this.urlPath = new UrlPath(path)
	}

	/**
	 * URL to which the request will be sent
	 */
	void urlPath(DslProperty path) {
		this.urlPath = new UrlPath(path)
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	void urlPath(String path, @DelegatesTo(UrlPath) Closure closure) {
		this.urlPath = new UrlPath(path)
		closure.delegate = urlPath
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	void urlPath(GString path, @DelegatesTo(UrlPath) Closure closure) {
		this.urlPath = new UrlPath(path)
		closure.delegate = urlPath
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	void urlPath(DslProperty path, @DelegatesTo(UrlPath) Closure closure) {
		this.urlPath = new UrlPath(path)
		closure.delegate = urlPath
		closure()
	}

	/**
	 * Allows to configure HTTP headers
	 */
	void headers(@DelegatesTo(RequestHeaders) Closure closure) {
		this.headers = new RequestHeaders()
		closure.delegate = headers
		closure()
	}

	/**
	 * Allows to configure HTTP cookies
	 */
	void cookies(@DelegatesTo(RequestCookies) Closure closure) {
		this.cookies = new RequestCookies()
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
	void body(DslProperty dslProperty) {
		this.body = new Body(dslProperty)
	}

	/**
	 * Allows set an HTTP body
	 */
	void body(Object bodyAsValue) {
		this.body = new Body(bodyAsValue)
	}

	Body getBody() {
		return body
	}

	/**
	 * Allows to set multipart via the map notation
	 */
	void multipart(Map<String, Object> body) {
		this.multipart = new Multipart(convertObjectsToDslProperties(body))
	}

	/**
	 * Allows to set multipart via lists
	 */
	void multipart(List multipartAsList) {
		this.multipart = new Multipart(convertObjectsToDslProperties(multipartAsList))
	}

	/**
	 * Allows to set multipart value
	 */
	void multipart(DslProperty dslProperty) {
		this.multipart = new Multipart(dslProperty)
	}

	/**
	 * Allows to set multipart value
	 */
	void multipart(Object multipartAsValue) {
		this.multipart = new Multipart(multipartAsValue)
	}

	/**
	 * Sets the equality check to the given query parameter
	 */
	MatchingStrategy equalTo(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO)
	}

	/**
	 * Sets the containing check to the given query parameter
	 */
	MatchingStrategy containing(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.CONTAINS)
	}

	/**
	 * Sets the matching check to the given query parameter
	 */
	MatchingStrategy matching(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.MATCHING)
	}

	/**
	 * Sets the not matching check to the given query parameter
	 */
	MatchingStrategy notMatching(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.NOT_MATCHING)
	}

	/**
	 * Sets the XML equality check to the body
	 */
	MatchingStrategy equalToXml(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_XML)
	}

	/**
	 * Sets the JSON equality check to the body
	 */
	MatchingStrategy equalToJson(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON)
	}

	/**
	 * Sets absence scheck to the given query parameter
	 */
	MatchingStrategy absent() {
		return new MatchingStrategy(true, MatchingStrategy.Type.ABSENT)
	}

	void assertThatSidesMatch(Object stubSide, OptionalProperty testSide) {
		throw new IllegalStateException("Optional can be used only for the stub side of the request!")
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	DslProperty value(ClientDslProperty client) {
		Object concreteValue = client.serverValue
		Object dynamicValue = client.clientValue
		if (dynamicValue instanceof RegexProperty && client.isSingleValue()) {
			return dynamicValue.dynamicClientEscapedConcreteProducer()
		}
		else if (concreteValue instanceof RegexProperty && !client.isSingleValue()) {
			concreteValue = dynamicValue
		}
		return new DslProperty(dynamicValue, concreteValue)
	}

	/**
	 * Allows to set a dynamic value for the given regular expression element
	 */
	DslProperty $(RegexProperty property) {
		return value(property)
	}

	/**
	 * Allows to set a dynamic value for the given regular expression element
	 */
	DslProperty value(RegexProperty property) {
		return value(client(property))
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	DslProperty $(ClientDslProperty client) {
		return value(client)
	}

	/**
	 * Allows to set a dynamic value for the Pattern element
	 */
	DslProperty value(Pattern client) {
		return value(new RegexProperty(client))
	}

	/**
	 * Allows to set a dynamic value for the given Pattern element
	 */
	DslProperty $(Pattern client) {
		return value(client)
	}

	@Override
	RegexProperty regexProperty(Object object) {
		return new RegexProperty(object).dynamicClientConcreteProducer()
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	void stubMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		log.warn("stubMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(closure)
	}

	/**
	 * Allows to set matchers for the body
	 */
	void bodyMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		this.bodyMatchers = new BodyMatchers()
		closure.delegate = this.bodyMatchers
		closure()
	}

	/**
	 * Allows to set a dynamic value for client and server side
	 */
	@Override
	DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		if (server.clientValue instanceof RegexProperty) {
			throw new IllegalStateException("You can't have a regular expression for the request on the server side")
		}
		return super.value(client, server)
	}

	/**
	 * Allows to set a dynamic value for client and server side
	 */
	@Override
	DslProperty value(ServerDslProperty server, ClientDslProperty client) {
		if (server.clientValue instanceof RegexProperty) {
			throw new IllegalStateException("You can't have a regular expression for the request on the server side")
		}
		return super.value(server, client)
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class ServerRequest extends Request {
		ServerRequest(Request request) {
			super(request)
		}
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class ClientRequest extends Request {
		ClientRequest(Request request) {
			super(request)
		}
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class RequestHeaders extends Headers {

		@Override
		DslProperty matching(String value) {
			return $(c(regex("${RegexpUtils.escapeSpecialRegexWithSingleEscape(value)}.*")),
					p(value))
		}
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class RequestCookies extends Cookies {

		@Override
		DslProperty matching(String value) {
			return $(c(regex("${RegexpUtils.escapeSpecialRegexWithSingleEscape(value)}.*")),
					p(value))
		}
	}

	@CompileStatic
	@EqualsAndHashCode(includeFields = true)
	@ToString(includePackage = false)
	private class ClientPatternValueDslProperty extends PatternValueDslProperty<ClientDslProperty> {

		@Override
		protected ClientDslProperty createProperty(Pattern pattern, Object generatedValue) {
			return new ClientDslProperty(pattern, generatedValue)
		}
	}
}
