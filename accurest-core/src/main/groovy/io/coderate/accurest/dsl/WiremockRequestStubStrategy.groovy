package io.coderate.accurest.dsl
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.coderate.accurest.dsl.internal.ClientRequest
import io.coderate.accurest.dsl.internal.Request

import java.util.regex.Pattern

@CompileStatic
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
		        headers   : buildClientHeadersSection(request.headers),
		        body      : request?.body?.clientValue
		] << appendUrl(request)).findAll { it.value }
	}

	private Map<String, Object> appendUrl(ClientRequest clientRequest) {
		Object url = clientRequest?.url?.clientValue
		return url instanceof Pattern ? [urlPattern: ((Pattern)url).pattern()] : [url: url]
	}

}
