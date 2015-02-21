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

	@PackageScope
	Map buildServerResponseContent() {
		return buildResponseContent(new ServerResponse(response))
	}

	private Map<String, Object> buildResponseContent(ClientResponse response) {
		return [status : response?.status?.clientValue,
		        body   : response?.body?.forClientSide(),
		        headers: buildClientHeadersSection(response.headers)].findAll { it.value }
	}

	private Map<String, Object> buildResponseContent(ServerResponse response) {
		return [status : response?.status?.serverValue,
		        body   : response?.body?.forServerSide(),
		        headers: buildServerHeadersSection(response.headers)].findAll { it.value }
	}

}
