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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import repackaged.nl.flotsam.xeger.Xeger

import java.util.regex.Pattern
/**
 * Represents the request side of the HTTP communication
 *
 * @since 1.0.0
 */
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Request extends Common {

	DslProperty method
	Url url
	UrlPath urlPath
	Headers headers
	Body body
	Multipart multipart

	Request() {
	}

	Request(Request request) {
		this.method = request.method
		this.url = request.url
		this.urlPath = request.urlPath
		this.headers = request.headers
		this.body = request.body
		this.multipart = request.multipart
	}

	void method(String method) {
		this.method = toDslProperty(method)
	}

	void method(DslProperty method) {
		this.method = toDslProperty(method)
	}

	void url(Object url) {
		this.url = new Url(url)
	}

	void url(DslProperty url) {
		this.url = new Url(url)
	}

	void url(Object url, @DelegatesTo(UrlPath) Closure closure) {
		this.url = new Url(url)
		closure.delegate = this.url
		closure()
	}

	void url(DslProperty url, @DelegatesTo(UrlPath) Closure closure) {
		this.url = new Url(url)
		closure.delegate = this.url
		closure()
	}

	void urlPath(String path) {
		this.urlPath = new UrlPath(path)
	}

	void urlPath(DslProperty path) {
		this.urlPath = new UrlPath(path)
	}

	void urlPath(String path, @DelegatesTo(UrlPath) Closure closure) {
		this.urlPath = new UrlPath(path)
		closure.delegate = urlPath
		closure()
	}

	void urlPath(DslProperty path, @DelegatesTo(UrlPath) Closure closure) {
		this.urlPath = new UrlPath(path)
		closure.delegate = urlPath
		closure()
	}

	void headers(@DelegatesTo(Headers) Closure closure) {
		this.headers = new Headers()
		closure.delegate = headers
		closure()
	}

	void body(Map<String, Object> body) {
		this.body = new Body(convertObjectsToDslProperties(body))
	}

	void body(List body) {
		this.body = new Body(convertObjectsToDslProperties(body))
	}

	void body(DslProperty dslProperty) {
		this.body = new Body(dslProperty)
	}

	void body(Object bodyAsValue) {
		this.body = new Body(bodyAsValue)
	}

	Body getBody() {
		return body
	}

	void multipart(Map<String, Object> body) {
		this.multipart = new Multipart(convertObjectsToDslProperties(body))
	}

	void multipart(List multipartAsList) {
		this.multipart = new Multipart(convertObjectsToDslProperties(multipartAsList))
	}

	void multipart(DslProperty dslProperty) {
		this.multipart = new Multipart(dslProperty)
	}

	void multipart(Object multipartAsValue) {
		this.multipart = new Multipart(multipartAsValue)
	}

	MatchingStrategy equalTo(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO)
	}

	MatchingStrategy containing(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.CONTAINS)
	}

	MatchingStrategy matching(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.MATCHING)
	}

	MatchingStrategy notMatching(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.NOT_MATCHING)
	}

	MatchingStrategy equalToXml(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_XML)
	}

	MatchingStrategy equalToJson(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON)
	}

	MatchingStrategy absent() {
		return new MatchingStrategy(true, MatchingStrategy.Type.ABSENT)
	}

	void assertThatSidesMatch(Object stubSide, OptionalProperty testSide) {
		throw new IllegalStateException("Optional can be used only for the stub side of the request!")
	}

	DslProperty value(ClientDslProperty client) {
		Object clientValue = client.clientValue
		if (client.clientValue instanceof Pattern) {
			clientValue = new Xeger(((Pattern)client.clientValue).pattern()).generate()
		}
		return new DslProperty(client.clientValue, clientValue)
	}

	DslProperty $(ClientDslProperty client) {
		return value(client)
	}

	@Override
	DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		if (server.clientValue instanceof Pattern) {
			throw new IllegalStateException("You can't have a regular expression for the request on the server side")
		}
		return super.value(client, server)
	}

	@Override
	DslProperty value(ServerDslProperty server, ClientDslProperty client) {
		if (server.clientValue instanceof Pattern) {
			throw new IllegalStateException("You can't have a regular expression for the request on the server side")
		}
		return super.value(server, client)
	}

}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ServerRequest extends Request {
	ServerRequest(Request request) {
		super(request)
	}
}

@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class ClientRequest extends Request {
	ClientRequest(Request request) {
		super(request)
	}
}
