/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock.restdocs;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.StandardWriterResolver;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.util.StringUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.trace;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class WireMockSnippet implements Snippet {

	private String snippetName = "stubs";

	private Set<String> headerBlackList = new HashSet<>(
			Arrays.asList("host", "content-length"));

	private Set<String> jsonPaths = new LinkedHashSet<>();

	private MediaType contentType;

	private StubMapping stubMapping;

	private boolean hasJsonBodyRequestToMatch = false;
	private boolean hasXmlBodyRequestToMatch = false;

	private static final TemplateFormat TEMPLATE_FORMAT = new TemplateFormat() {

		@Override
		public String getId() {
			return "json";
		}

		@Override
		public String getFileExtension() {
			return "json";
		}
	};

	@Override
	public void document(Operation operation) throws IOException {
		extractMatchers(operation);
		if (this.stubMapping == null) {
			this.stubMapping = request(operation).willReturn(response(operation)).build();
		}
		String json = Json.write(this.stubMapping);
		RestDocumentationContext context = (RestDocumentationContext) operation
				.getAttributes().get(RestDocumentationContext.class.getName());
		RestDocumentationContextPlaceholderResolverFactory placeholders = new RestDocumentationContextPlaceholderResolverFactory();
		WriterResolver writerResolver = new StandardWriterResolver(placeholders, "UTF-8",
				TEMPLATE_FORMAT);
		try (Writer writer = writerResolver.resolve(this.snippetName, operation.getName(),
				context)) {
			writer.append(json);
		}
	}

	private void extractMatchers(Operation operation) {
		this.stubMapping = (StubMapping) operation.getAttributes()
				.get("contract.stubMapping");
		if (this.stubMapping != null) {
			return;
		}
		@SuppressWarnings("unchecked")
		Set<String> jsonPaths = (Set<String>) operation.getAttributes()
				.get("contract.jsonPaths");
		this.jsonPaths = jsonPaths;
		this.contentType = (MediaType) operation.getAttributes()
				.get("contract.contentType");
		if (this.contentType == null) {
			this.hasJsonBodyRequestToMatch = hasJsonContentType(operation);
			this.hasXmlBodyRequestToMatch = hasXmlContentType(operation);
		}
	}

	private boolean hasJsonContentType(Operation operation) {
		return hasContentType(operation, MediaType.APPLICATION_JSON);
	}

	private boolean hasXmlContentType(Operation operation) {
		return hasContentType(operation, MediaType.APPLICATION_XML);
	}

	private boolean hasContentType(Operation operation, MediaType mediaType) {
		return operation.getRequest().getHeaders().getContentType() != null
				&& (operation.getRequest().getHeaders().getContentType()
				.isCompatibleWith(mediaType));
	}

	private ResponseDefinitionBuilder response(Operation operation) {
		return aResponse().withHeaders(responseHeaders(operation))
				.withBody(operation.getResponse().getContentAsString())
				.withStatus(operation.getResponse().getStatus().value());
	}

	private MappingBuilder request(Operation operation) {
		return queryParams(
				requestHeaders(requestBuilder(operation), operation)
				, operation);
	}

	private MappingBuilder queryParams(MappingBuilder request, Operation operation) {
		String rawQuery = operation.getRequest().getUri().getRawQuery();
		if (StringUtils.isEmpty(rawQuery)) {
			return request;
		}
		for (String queryPair : rawQuery.split("&")) {
			String[] splitQueryPair = queryPair.split("=");
			request = request.withQueryParam(splitQueryPair[0], WireMock.equalTo(splitQueryPair[1]));
		}
		return request;
	}

	private MappingBuilder requestHeaders(MappingBuilder request, Operation operation) {
		org.springframework.http.HttpHeaders headers = operation.getRequest()
				.getHeaders();
		// TODO: whitelist headers
		for (String name : headers.keySet()) {
			if (!this.headerBlackList.contains(name.toLowerCase())) {
				if ("content-type".equalsIgnoreCase(name) && this.contentType != null) {
					continue;
				}
				request = request.withHeader(name, equalTo(headers.getFirst(name)));
			}
		}
		if (this.contentType != null) {
			request = request.withHeader("Content-Type",
					matching(Pattern.quote(this.contentType.toString()) + ".*"));
		}
		return request;
	}

	private MappingBuilder requestBuilder(Operation operation) {
		switch (operation.getRequest().getMethod()) {
		case DELETE:
			return delete(requestPattern(operation));
		case POST:
			return bodyPattern(post(requestPattern(operation)),
					operation.getRequest().getContentAsString());
		case PUT:
			return bodyPattern(put(requestPattern(operation)),
					operation.getRequest().getContentAsString());
		case PATCH:
			return bodyPattern(patch(requestPattern(operation)),
					operation.getRequest().getContentAsString());
		case GET:
			return get(requestPattern(operation));
		case HEAD:
			return head(requestPattern(operation));
		case OPTIONS:
			return options(requestPattern(operation));
		case TRACE:
			return trace(requestPattern(operation));
		default:
			throw new UnsupportedOperationException(
					"Unsupported method type: " + operation.getRequest().getMethod());
		}
	}

	private MappingBuilder bodyPattern(MappingBuilder builder, String content) {
		if (this.jsonPaths != null && !this.jsonPaths.isEmpty()) {
			for (String jsonPath : this.jsonPaths) {
				builder.withRequestBody(matchingJsonPath(jsonPath));
			}
		}
		else if (!StringUtils.isEmpty(content)) {
			if (this.hasJsonBodyRequestToMatch) {
				builder.withRequestBody(equalToJson(content));
			}
			else if (this.hasXmlBodyRequestToMatch) {
				builder.withRequestBody(equalToXml(content));
			}
			else {
				builder.withRequestBody(equalTo(content));
			}
		}
		return builder;
	}

	private UrlPattern requestPattern(Operation operation) {
		return urlEqualTo(operation.getRequest().getUri().getPath());
	}

	private HttpHeaders responseHeaders(Operation operation) {
		org.springframework.http.HttpHeaders headers = operation.getResponse()
				.getHeaders();
		HttpHeaders result = new HttpHeaders();
		for (String name : headers.keySet()) {
			if (!this.headerBlackList.contains(name.toLowerCase())) {
				result = result.plus(new HttpHeader(name, headers.get(name)));
			}
		}
		return result;
	}

}