package io.codearte.accurest.dsl
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.internal.ClientResponse
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Response
import io.codearte.accurest.util.ContentType

import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader

@TypeChecked
@PackageScope
class WiremockResponseStubStrategy extends BaseWiremockStubStrategy {

	private final Request request
	private final Response response

	WiremockResponseStubStrategy(GroovyDsl groovyDsl) {
		this.response = groovyDsl.response
		this.request = groovyDsl.request
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
		ContentType contentType = recognizeContentTypeFromHeader(response.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(body)
		}
		return body != null ? [body: parseBody(body, contentType)] : [:]
	}


}
