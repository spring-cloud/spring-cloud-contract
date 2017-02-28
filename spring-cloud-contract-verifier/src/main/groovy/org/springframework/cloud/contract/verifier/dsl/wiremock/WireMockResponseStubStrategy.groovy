/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response
import org.springframework.cloud.contract.verifier.util.ContentType

import static org.springframework.cloud.contract.verifier.util.ContentUtils.recognizeContentTypeFromContent
import static org.springframework.cloud.contract.verifier.util.ContentUtils.recognizeContentTypeFromHeader
/**
 * Converts a {@link Request} into {@link ResponseDefinition}
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
class WireMockResponseStubStrategy extends BaseWireMockStubStrategy {

	private final Response response

	WireMockResponseStubStrategy(Contract groovyDsl) {
		this.response = groovyDsl.response
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
		builder.withTransformer("response-template", "", "")
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
		// TODO: Add a missing test for this
		if (response.delay) {
			builder.withFixedDelay(response.delay.clientValue as Integer)
		}
	}


}
