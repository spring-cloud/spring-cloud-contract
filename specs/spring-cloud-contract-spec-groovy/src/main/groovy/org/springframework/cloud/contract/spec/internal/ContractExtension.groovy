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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.util.logging.Commons

import org.springframework.cloud.contract.spec.Contract

@CompileStatic
@Commons
class ContractExtension {

	// CONTRACT
	/**
	 * The HTTP request part of the contract
	 */
	static void request(final Contract contract, @DelegatesTo(Request) Closure closure) {
		contract.request = new Request()
		closure.delegate = contract.request
		closure()
	}

	/**
	 * The HTTP response part of the contract
	 */
	static void response(final Contract contract, @DelegatesTo(Response) Closure closure) {
		contract.response = new Response()
		closure.delegate = contract.response
		closure()
	}

	static void input(final Contract contract, @DelegatesTo(Input) Closure closure) {
		contract.input = new Input()
		closure.delegate = contract.input
		closure()
	}

	/**
	 * The output side of a messaging contract.
	 */
	static void outputMessage(final Contract contract, @DelegatesTo(OutputMessage) Closure closure) {
		contract.outputMessage = new OutputMessage()
		closure.delegate = contract.outputMessage
		closure()
	}

	// INPUT
	static void messageHeaders(final Input input, @DelegatesTo(Headers) Closure closure) {
		input.messageHeaders = new Headers()
		closure.delegate = input.messageHeaders
		closure()
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	static void stubMatchers(final Input input, @DelegatesTo(BodyMatchers) Closure closure) {
		log.warn("stubMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(input, closure)
	}

	static void bodyMatchers(final Input input, @DelegatesTo(BodyMatchers) Closure closure) {
		input.bodyMatchers = new BodyMatchers()
		closure.delegate = input.bodyMatchers
		closure()
	}

	// OUTPUT
	static void headers(final OutputMessage output, @DelegatesTo(Headers) Closure closure) {
		output.headers = new Headers()
		closure.delegate = output.headers
		closure()
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	static void testMatchers(final OutputMessage output, @DelegatesTo(ResponseBodyMatchers) Closure closure) {
		log.warn("testMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(output, closure)
	}

	static void bodyMatchers(final OutputMessage output, @DelegatesTo(ResponseBodyMatchers) Closure closure) {
		output.bodyMatchers = new ResponseBodyMatchers()
		closure.delegate = output.bodyMatchers
		closure()
	}

	// REQUEST

	static void url(final Request request, Object url, @DelegatesTo(UrlPath) Closure closure) {
		request.url = new Url(url)
		closure.delegate = request.url
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	static void url(final Request request, DslProperty url, @DelegatesTo(UrlPath) Closure closure) {
		request.url = new Url(url)
		closure.delegate = request.url
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	static void urlPath(final Request request, String path, @DelegatesTo(UrlPath) Closure closure) {
		request.urlPath = new UrlPath(path)
		closure.delegate = request.urlPath
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	static void urlPath(final Request request, GString path, @DelegatesTo(UrlPath) Closure closure) {
		request.urlPath = new UrlPath(path)
		closure.delegate = request.urlPath
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	static void urlPath(final Request request, DslProperty path, @DelegatesTo(UrlPath) Closure closure) {
		request.urlPath = new UrlPath(path)
		closure.delegate = request.urlPath
		closure()
	}

	/**
	 * Allows to configure HTTP headers
	 */
	static void headers(final Request request, @DelegatesTo(Request.RequestHeaders) Closure closure) {
		request.headers = new Request.RequestHeaders()
		closure.delegate = request.headers
		closure()
	}

	/**
	 * Allows to configure HTTP cookies
	 */
	static void cookies(final Request request, @DelegatesTo(Request.RequestCookies) Closure closure) {
		request.cookies = new Request.RequestCookies()
		closure.delegate = request.cookies
		closure()
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	static void stubMatchers(final Request request, @DelegatesTo(BodyMatchers) Closure closure) {
		log.warn("stubMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(request, closure)
	}

	/**
	 * Allows to set matchers for the body
	 */
	static void bodyMatchers(final Request request, @DelegatesTo(BodyMatchers) Closure closure) {
		request.bodyMatchers = new BodyMatchers()
		closure.delegate = request.bodyMatchers
		closure()
	}

	// RESPONSE
	/**
	 * Allows to configure HTTP headers
	 */
	static void headers(final Response response, @DelegatesTo(Response.ResponseHeaders) Closure closure) {
		response.headers = new Response.ResponseHeaders()
		closure.delegate = response.headers
		closure()
	}

	/**
	 * Allows to configure HTTP cookies
	 */
	static void cookies(final Response response, @DelegatesTo(Response.ResponseCookies) Closure closure) {
		response.cookies = new Response.ResponseCookies()
		closure.delegate = response.cookies
		closure()
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	static void testMatchers(final Response response, @DelegatesTo(ResponseBodyMatchers) Closure closure) {
		log.warn("testMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(response, closure)
	}

	/**
	 * Allows to set matchers for the body
	 */
	static void bodyMatchers(final Response response, @DelegatesTo(ResponseBodyMatchers) Closure closure) {
		response.bodyMatchers = new ResponseBodyMatchers()
		closure.delegate = response.bodyMatchers
		closure()
	}

	// URL
	static void queryParameters(final Url url, @DelegatesTo(QueryParameters) Closure closure) {
		url.queryParameters = new QueryParameters()
		closure.delegate = url.queryParameters
		closure()
	}

	// BODY MATCHERS
	static MatchingTypeValue byType(final BodyMatchers bodyMatchers, @DelegatesTo(MatchingTypeValueHolder) Closure closure) {
		MatchingTypeValueHolder matchingTypeValue = new MatchingTypeValueHolder()
		closure.delegate = matchingTypeValue
		closure()
		return matchingTypeValue.matchingTypeValue
	}
}
