package io.coderate.accurest.dsl

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.coderate.accurest.dsl.internal.ClientRequest
import io.coderate.accurest.dsl.internal.Request
import io.coderate.accurest.dsl.internal.ServerRequest

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

	@PackageScope
	Map buildServerRequestContent() {
		return buildRequestContent(new ServerRequest(request))
	}

	private Map<String, Object> buildRequestContent(ClientRequest request) {
		return [method    : request?.method?.clientValue,
		        url       : request?.url?.clientValue,
		        urlPattern: request?.urlPattern?.clientValue,
		        urlPath   : request?.urlPath?.clientValue,
		        headers   : buildClientHeadersSection(request.headers)].findAll { it.value }
	}

	private Map<String, Object> buildRequestContent(ServerRequest request) {
		return [method    : request?.method?.serverValue,
		        url       : request?.url?.serverValue,
		        urlPattern: request?.urlPattern?.serverValue,
		        urlPath   : request?.urlPath?.serverValue,
		        headers   : buildServerHeadersSection(request.headers)].findAll { it.value }
	}
}
