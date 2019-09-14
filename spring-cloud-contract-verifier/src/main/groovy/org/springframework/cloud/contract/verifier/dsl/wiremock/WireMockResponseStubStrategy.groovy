/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.extension.Extension
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import groovy.transform.PackageScope
import groovy.transform.TypeChecked

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.MapConverter
import org.springframework.core.io.support.SpringFactoriesLoader
/**
 * Converts a {@link Request} into {@link ResponseDefinition}
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
class WireMockResponseStubStrategy extends BaseWireMockStubStrategy {

	private final Response response
	private final ContentType contentType

	WireMockResponseStubStrategy(Contract groovyDsl, SingleContractMetadata singleContractMetadata) {
		super(groovyDsl)
		this.response = groovyDsl.response
		this.contentType = contentType(singleContractMetadata)
	}

	protected ContentType contentType(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.evaluatedOutputStubContentType
	}

	@PackageScope
	ResponseDefinition buildClientResponseContent() {
		if (!response) {
			return null
		}
		ResponseDefinitionBuilder builder = new ResponseDefinitionBuilder()
				.withStatus(MapConverter.getStubSideValues(response.status) as Integer)
		appendHeaders(builder)
		appendBody(builder)
		appendResponseDelayTime(builder)
		builder.withTransformers(responseTransformerNames())
		return builder.build()
	}

	private String[] responseTransformerNames() {
		List<WireMockExtensions> wireMockExtensions = SpringFactoriesLoader.
				loadFactories(WireMockExtensions, null)
		if (wireMockExtensions) {
			return ((List<Extension>) wireMockExtensions
					.collect { WireMockExtensions extension -> extension.extensions() }
					.flatten())
					.collect { Extension e -> e.getName() } as String[]
		}
		return [new DefaultResponseTransformer().getName()] as String[]
	}

	private void appendHeaders(ResponseDefinitionBuilder builder) {
		if (response.headers) {
			builder.withHeaders(new HttpHeaders(response.headers.entries?.collect {
				new HttpHeader(it.name, MapConverter.getStubSideValues(it.clientValue).
						toString())
			}))
		}
	}

	private void appendBody(ResponseDefinitionBuilder builder) {
		if (response.body) {
			Object body = MapConverter.getStubSideValues(response.body)
			if (body instanceof byte[]) {
				builder.withBody(body)
			}
			else if (body instanceof FromFileProperty && body.isByte()) {
				builder.withBody(body.asBytes())
			}
			else {
				builder.withBody(parseBody(body, contentType))
			}
		}
	}

	private void appendResponseDelayTime(ResponseDefinitionBuilder builder) {
		// TODO: Add a missing test for this
		if (response.delay) {
			builder.withFixedDelay(response.delay.clientValue as Integer)
		}
	}

}
