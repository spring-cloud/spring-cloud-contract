/*
 * Copyright 2016-2020 the original author or authors.
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

package org.springframework.cloud.contract.wiremock.restdocs;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.jayway.jsonpath.JsonPath;

import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @param <T> result type
 * @param <S> stub type
 * @author Dave Syer
 */
public abstract class WireMockVerifyHelper<T, S extends WireMockVerifyHelper<T, S>> {

	private Map<String, JsonPath> jsonPaths = new LinkedHashMap<>();

	private MediaType contentType;

	private MappingBuilder builder;

	/**
	 * @param name the stub name (ignored)
	 * @return this
	 * @deprecated in favour of explicitly calling <code>andDo(document(name))</code>
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public S stub(String name) {
		return (S) this;
	}

	public void configure(T result) {
		Map<String, Object> configuration = getConfiguration(result);
		byte[] requestBodyContent = getRequestBodyContent(result);
		if (requestBodyContent != null) {
			String actual = new String(requestBodyContent, Charset.forName("UTF-8"));
			for (JsonPath jsonPath : this.jsonPaths.values()) {
				new JsonPathValue(jsonPath, actual).assertHasValue(Object.class, "an object");
			}
		}
		configuration.put("contract.jsonPaths", this.jsonPaths.keySet());
		if (this.contentType != null) {
			configuration.put("contract.contentType", this.contentType);
			MediaType resultType = getContentType(result);
			assertThat(resultType).isNotNull().as("no content type");
			assertThat(this.contentType.includes(resultType)).isTrue().as("content type did not match");
		}
		if (this.builder != null) {
			this.builder.willReturn(getResponseDefinition(result));
			StubMapping stubMapping = this.builder.build();
			Request request = getWireMockRequest(result);
			MatchResult match = stubMapping.getRequest().match(request);
			assertThat(match.isExactMatch()).as("wiremock did not match request").isTrue();
			configuration.put("contract.stubMapping", stubMapping);
		}
	}

	protected abstract Request getWireMockRequest(T result);

	protected abstract MediaType getContentType(T result);

	protected abstract byte[] getRequestBodyContent(T result);

	protected abstract ResponseDefinitionBuilder getResponseDefinition(T result);

	protected abstract Map<String, Object> getConfiguration(T result);

	@SuppressWarnings("unchecked")
	public S wiremock(MappingBuilder builder) {
		this.builder = builder;
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S jsonPath(String expression, Object... args) {
		compile(expression, args);
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S contentType(MediaType contentType) {
		this.contentType = contentType;
		return (S) this;
	}

	private void compile(String expression, Object... args) {
		org.springframework.util.Assert.hasText((expression == null ? null : expression),
				"expression must not be null or empty");
		expression = String.format(expression, args);
		this.jsonPaths.put(expression, JsonPath.compile(expression));
	}

}
