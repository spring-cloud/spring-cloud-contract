/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl.wiremock;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import groovy.lang.GString;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * Converts a {@link Request} into {@link ResponseDefinition}.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 1.0.0
 */
class WireMockResponseStubStrategy extends BaseWireMockStubStrategy {

	private final Response response;

	private final ContentType contentType;

	WireMockResponseStubStrategy(Contract groovyDsl,
			SingleContractMetadata singleContractMetadata) {
		super(groovyDsl);
		this.response = groovyDsl.getResponse();
		this.contentType = contentType(singleContractMetadata);
	}

	protected ContentType contentType(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.getEvaluatedOutputStubContentType();
	}

	ResponseDefinition buildClientResponseContent() {
		if (response == null) {
			return null;
		}
		ResponseDefinitionBuilder builder = new ResponseDefinitionBuilder().withStatus(
				(Integer) MapConverter.getStubSideValues(response.getStatus()));
		appendHeaders(builder);
		appendBody(builder);
		appendResponseDelayTime(builder);
		builder.withTransformers(responseTransformerNames());
		return builder.build();
	}

	private String[] responseTransformerNames() {
		List<WireMockExtensions> wireMockExtensions = SpringFactoriesLoader
				.loadFactories(WireMockExtensions.class, null);
		if (!wireMockExtensions.isEmpty()) {
			return wireMockExtensions.stream().map(WireMockExtensions::extensions)
					.flatMap(Collection::stream).map(Extension::getName)
					.toArray(String[]::new);
		}
		return new String[] { new DefaultResponseTransformer().getName() };
	}

	private void appendHeaders(ResponseDefinitionBuilder builder) {
		if (response.getHeaders() != null) {
			HttpHeaders headers = response.getHeaders().getEntries().stream()
					.map(it -> new HttpHeader(it.getName(),
							MapConverter.getStubSideValues(it.getClientValue())
									.toString()))
					.collect(collectingAndThen(toList(), HttpHeaders::new));
			builder.withHeaders(headers);
		}
	}

	private void appendBody(ResponseDefinitionBuilder builder) {
		if (response.getBody() != null) {
			Object body = MapConverter.getStubSideValues(response.getBody());
			if (body instanceof byte[]) {
				builder.withBody((byte[]) body);
			}
			else if (body instanceof FromFileProperty
					&& ((FromFileProperty) body).isByte()) {
				builder.withBody(((FromFileProperty) body).asBytes());
			}
			else if (body instanceof Map) {
				builder.withBody(parseBody((Map<?, ?>) body, contentType));
			}
			else if (body instanceof List) {
				builder.withBody(parseBody((List<?>) body, contentType));
			}
			else if (body instanceof GString) {
				builder.withBody(parseBody((GString) body, contentType));
			}
			else {
				builder.withBody(parseBody(body, contentType));
			}
		}
	}

	private void appendResponseDelayTime(ResponseDefinitionBuilder builder) {
		// TODO: Add a missing test for this
		if (response.getDelay() != null) {
			builder.withFixedDelay((Integer) response.getDelay().getClientValue());
		}
	}

}
