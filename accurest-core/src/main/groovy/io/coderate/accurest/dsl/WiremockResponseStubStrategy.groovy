package io.coderate.accurest.dsl
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.coderate.accurest.dsl.internal.ClientResponse
import io.coderate.accurest.dsl.internal.Response

@TypeChecked
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
		return ([status : response?.status?.clientValue,
		        headers: buildClientResponseHeadersSection(response.headers)
        ] << appendBody(response)).findAll { it.value }
	}

    private Map<String, Object> appendBody(ClientResponse response) {
        Object body = response?.body?.clientValue
        return body != null ? [body: parseBody(body)] : [:]
    }
}
