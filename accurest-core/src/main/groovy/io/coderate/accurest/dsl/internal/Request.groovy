package io.coderate.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false)
class Request extends Common {

	DslProperty method
	DslProperty url
	DslProperty urlPattern
	DslProperty urlPath
	Headers headers
	Body body

	Request() {
	}

	Request(Request request) {
		this.method = request.method
		this.url = request.url
		this.urlPattern = request.urlPattern
		this.urlPath = request.urlPath
		this.headers = request.headers
		this.body = request.body
	}

	void method(String method) {
		this.method = toDslProperty(method)
	}

	void method(DslProperty method) {
		this.method = toDslProperty(method)
	}

	void url(String url) {
		this.url = toDslProperty(url)
	}

	void url(DslProperty url) {
		this.url = toDslProperty(url)
	}

	void urlPattern(String urlPattern) {
		this.urlPattern = toDslProperty(urlPattern)
	}

	void urlPattern(DslProperty urlPattern) {
		this.urlPattern = toDslProperty(urlPattern)
	}

	void headers(@DelegatesTo(Headers) Closure closure) {
		this.headers = new Headers()
		closure.delegate = headers
		closure()
	}

	void urlPath(String urlPath) {
		this.urlPath = toDslProperty(urlPath)
	}

	void urlPath(DslProperty urlPath) {
		this.urlPath = toDslProperty(urlPath)
	}

	void body(Map<String, Object> body) {
		this.body = new Body(convertObjectsToDslProperties(body))
	}

	void body(List body) {
		this.body = new Body(convertObjectsToDslProperties(body))
	}

	void body(Object bodyAsValue) {
		this.body = new Body(bodyAsValue)
	}

	Body getBody() {
		return body
	}
}

@CompileStatic
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false)
class ServerRequest extends Request {
	ServerRequest(Request request) {
		super(request)
	}
}

@CompileStatic
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false)
class ClientRequest extends Request {
	ClientRequest(Request request) {
		super(request)
	}
}
