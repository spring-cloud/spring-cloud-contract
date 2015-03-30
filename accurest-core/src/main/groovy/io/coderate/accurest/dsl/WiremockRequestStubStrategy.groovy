package io.coderate.accurest.dsl

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.coderate.accurest.dsl.internal.ClientRequest
import io.coderate.accurest.dsl.internal.Request

import java.util.regex.Pattern

@TypeChecked
@PackageScope
class WiremockRequestStubStrategy extends BaseWiremockStubStrategy {

	private final Request request

	WiremockRequestStubStrategy(GroovyDsl groovyDsl) {
		this.request = groovyDsl.request
	}

	@PackageScope
	Map buildClientRequestContent() {
		return buildRequestContent(new ClientRequest(request))
	}

	private Map<String, Object> buildRequestContent(ClientRequest request) {
		return ([method    : request?.method?.clientValue,
		        headers   : buildClientRequestHeadersSection(request.headers)
		] << appendUrl(request) << appendBody(request)).findAll { it.value }
	}

	private Map<String, Object> appendUrl(ClientRequest clientRequest) {
		Object url = clientRequest?.url?.clientValue
		return url instanceof Pattern ? [urlPattern: ((Pattern)url).pattern()] : [url: url]
	}

	private Map<String, Object> appendBody(ClientRequest clientRequest) {
		Object body = clientRequest?.body?.clientValue
		return body != null ? [bodyPatterns: [[equalTo: parseBody(body)]]] : [:]
	}
}
