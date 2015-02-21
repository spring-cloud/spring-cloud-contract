package io.coderate.accurest.dsl

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.coderate.accurest.dsl.internal.ClientResponse
import io.coderate.accurest.dsl.internal.Response
import io.coderate.accurest.dsl.internal.ServerResponse

@CompileStatic
@PackageScope
class WiremockResponseStubStrategy extends BaseWiremockStubStrategy {

	private final Response response

	WiremockResponseStubStrategy(GroovyDsl groovyDsl) {
		this.response = groovyDsl.response
	}

	@PackageScope
	Map buildClientResponseContent() {
		return buildResponseContent(new ClientResponse(response))
	}

	private Map<String, Object> buildResponseContent(ClientResponse response) {
		return [status : response?.status?.clientValue,
		        body   : response?.body?.clientValue,
		        headers: buildClientHeadersSection(response.headers)].findAll { it.value }
	}
}
