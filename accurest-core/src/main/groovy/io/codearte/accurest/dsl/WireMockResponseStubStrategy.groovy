package io.codearte.accurest.dsl
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Response
import io.codearte.accurest.util.ContentType

import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader

@TypeChecked
@PackageScope
class WireMockResponseStubStrategy extends BaseWireMockStubStrategy {

	private final Request request
	private final Response response

	WireMockResponseStubStrategy(GroovyDsl groovyDsl) {
		this.response = groovyDsl.response
		this.request = groovyDsl.request
	}

	@PackageScope
	ResponseDefinition buildClientResponseContent() {
		int status = response.status.clientValue as Integer
		HttpHeaders httpHeaders = getHttpHeaders()
		String body = getBody()
		return new ResponseDefinition(status, body as String, null, null, null, httpHeaders, null, null, null, null, null)
	}

	private HttpHeaders getHttpHeaders() {
		if(!(response.headers)) {
			return null
		}
		return new HttpHeaders(response.headers.entries?.collect { new HttpHeader(it.name, it.clientValue.toString()) })
	}

	private String getBody() {
		if (!response.body) {
			return null
		}
		Object body = response.body.clientValue
		ContentType contentType = recognizeContentTypeFromHeader(response.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(body)
		}
		return parseBody(body, contentType)
	}


}
