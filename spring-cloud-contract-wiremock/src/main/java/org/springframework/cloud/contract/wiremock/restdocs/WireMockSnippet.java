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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.Snippet;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class WireMockSnippet implements Snippet {

	private String snippetName = "stubs";

	private Set<String> headerBlackList = new HashSet<>(
			Arrays.asList("host", "content-length"));

	private Set<String> jsonPaths = new LinkedHashSet<>();

	private MediaType contentType;

	private StubMapping stubMapping;

	private boolean hasJsonBodyRequestToMatch = false;

	@Override
	public void document(Operation operation) throws IOException {
		extractMatchers(operation);
		if (this.stubMapping == null) {
			this.stubMapping = request(operation).willReturn(response(operation)).build();
		}
		String json = Json.write(this.stubMapping);
		RestDocumentationContext context = (RestDocumentationContext) operation
				.getAttributes().get(RestDocumentationContext.class.getName());
		File output = new File(context.getOutputDirectory(),
				this.snippetName + "/" + operation.getName() + ".json");
		output.getParentFile().mkdirs();
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(output))) {
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
		}
	}

	private boolean hasJsonContentType(Operation operation) {
		return operation.getRequest().getHeaders().getContentType() != null
				&& (operation.getRequest().getHeaders().getContentType()
								.isCompatibleWith(MediaType.APPLICATION_JSON));
	}

	private ResponseDefinitionBuilder response(Operation operation) {
		return aResponse().withHeaders(responseHeaders(operation))
				.withBody(operation.getResponse().getContentAsString())
				.withStatus(operation.getResponse().getStatus().value());
	}

	private MappingBuilder request(Operation operation) {
		return requestHeaders(requestBuilder(operation), operation);
	}

	private MappingBuilder requestHeaders(MappingBuilder request,
			Operation operation) {
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
		default:
			return get(requestPattern(operation));
		}
	}

	private MappingBuilder bodyPattern(MappingBuilder builder,
			String content) {
		if (this.jsonPaths != null) {
			for (String jsonPath : this.jsonPaths) {
				builder.withRequestBody(matchingJsonPath(jsonPath));
			}
		}
		else if (this.hasJsonBodyRequestToMatch) {
			builder.withRequestBody(equalToJson(content));
		}
		else {
			builder.withRequestBody(equalTo(content));
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