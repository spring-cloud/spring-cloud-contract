package io.codearte.accurest.dsl

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
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
		if(!response) {
			return null
		}
		ResponseDefinitionBuilder builder = new ResponseDefinitionBuilder()
				.withStatus(response.status.clientValue as Integer)
		appendHeaders(builder)
		appendBody(builder)
		appendResponseDelayTime(builder)
		return builder.build()
	}

	private void appendHeaders(ResponseDefinitionBuilder builder) {
		if (response.headers) {
			builder.withHeaders(new HttpHeaders(response.headers.entries?.collect {
				new HttpHeader(it.name, it.clientValue.toString())
			}))
		}
	}

	private void appendBody(ResponseDefinitionBuilder builder) {
		if (response.body) {
			Object body = response.body.clientValue
			ContentType contentType = recognizeContentTypeFromHeader(response.headers)
			if (contentType == ContentType.UNKNOWN) {
				contentType = recognizeContentTypeFromContent(body)
			}
			builder.withBody(parseBody(body, contentType))
		}
	}

	private void appendResponseDelayTime(ResponseDefinitionBuilder builder) {
		if (response.delay) {
			builder.withFixedDelay(response.delay.clientValue as Integer)
		}
	}


}
