package io.coderate.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true)
class Response extends Common {

	private DslProperty status
	private Headers headers
	private Body body

	Response() {
	}

	Response(Response response) {
		this.status = response.status
		this.headers = response.headers
		this.body = response.body
	}

	void status(int status) {
		this.status = toDslProperty(status)
	}

	void status(DslProperty status) {
		this.status = toDslProperty(status)
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

	void body(Object bodyAsValue) {
		this.body = new Body(bodyAsValue)
	}

	Body getBody() {
		return body
	}

	DslProperty getStatus() {
		return status
	}

	Headers getHeaders() {
		return headers
	}
}

@CompileStatic
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false)
class ServerResponse extends Response {
	ServerResponse(Response request) {
		super(request)
	}
}

@CompileStatic
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false)
class ClientResponse extends Response {
	ClientResponse(Response request) {
		super(request)
	}
}