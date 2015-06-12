package io.codearte.accurest.dsl.internal
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.xml.MarkupBuilder

@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Request extends Common {

	DslProperty method
	Url url
	UrlPath urlPath
	Headers headers
	Body body

	Request() {
	}

	Request(Request request) {
		this.method = request.method
		this.url = request.url
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

	MatchingStrategy equalToJson(Object value, JSONCompareMode jsonCompareMode) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON)
	}

	MatchingStrategy equalToJsonStrictly(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON, JSONCompareMode.STRICT)
	}

	MatchingStrategy equalToJsonLeniently(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON, JSONCompareMode.LENIENT)
	}

	MatchingStrategy equalToJsonNonExtensibly(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON, JSONCompareMode.NON_EXTENSIBLE)
	}

	MatchingStrategy equalToJsonWithStrictOrder(Object value) {
		return new MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON, JSONCompareMode.STRICT_ORDER)
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
