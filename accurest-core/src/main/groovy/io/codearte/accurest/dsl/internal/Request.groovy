package io.codearte.accurest.dsl.internal
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeNames = true)
class Request extends Common {

	DslProperty method
	Url url
	Headers headers
	Body body

	Request() {
	}

	Request(Request request) {
		this.method = request.method
		this.url = request.url
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
		this.url = new Url(url)
	}

	void url(DslProperty url) {
		this.url = new Url(url)
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
