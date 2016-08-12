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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import com.github.tomakehurst.wiremock.client.RemoteMappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.servlet.WireMockHttpServletRequestAdapter;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;

public class ContractRequestHandler implements ResultHandler {

	static final String ATTRIBUTE_NAME_CONFIGURATION = "org.springframework.restdocs.configuration";

	private Map<String, JsonPath> jsonPaths = new LinkedHashMap<>();
	private MediaType contentType;
	private String name;

	private RemoteMappingBuilder<?, ?> builder;

	public ContractRequestHandler() {
	}

	public ResultHandler stub(String name) {
		this.name = name;
		// TODO: try and get access to the internals of this so we don't need to store
		// state in the snippet
		return this;
	}

	@Override
	public void handle(MvcResult result) throws Exception {
		MockHttpServletRequest request = result.getRequest();
		Map<String, Object> configuration = getConfiguration(result);
		String actual = StreamUtils.copyToString(request.getInputStream(),
				Charset.forName("UTF-8"));
		for (JsonPath jsonPath : jsonPaths.values()) {
			new JsonPathValue(jsonPath, actual).assertHasValue(Object.class, "an object");
		}
		configuration.put("contract.jsonPaths", jsonPaths.keySet());
		if (contentType != null) {
			configuration.put("contract.contentType", contentType);
			String resultType = request.getContentType();
			assertThat(resultType).isNotNull().as("no content type");
			assertThat(contentType.includes(MediaType.valueOf(resultType))).isTrue()
					.as("content type did not match");
		}
		if (builder != null) {
			builder.willReturn(getResponseDefinition(result));
			StubMapping stubMapping = builder.build();
			MatchResult match = stubMapping.getRequest()
					.match(new WireMockHttpServletRequestAdapter(request));
			assertThat(match.isExactMatch()).as("wiremock did not match request").isTrue();
			configuration.put("contract.stubMapping", stubMapping);
		}
		MockMvcRestDocumentation.document(this.name).handle(result);
	}

	private ResponseDefinitionBuilder getResponseDefinition(MvcResult result)
			throws UnsupportedEncodingException {
		MockHttpServletResponse response = result.getResponse();
		ResponseDefinitionBuilder definition = ResponseDefinitionBuilder
				.responseDefinition().withBody(response.getContentAsString())
				.withStatus(response.getStatus());
		addResponseHeaders(definition, response);
		return definition;
	}

	private void addResponseHeaders(ResponseDefinitionBuilder definition,
			MockHttpServletResponse input) {
		for (String name : input.getHeaderNames()) {
			definition.withHeader(name, input.getHeader(name));
		}
	}

	private Map<String, Object> getConfiguration(MvcResult result) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) result.getRequest()
				.getAttribute(ATTRIBUTE_NAME_CONFIGURATION);
		if (map == null) {
			map = new HashMap<>();
			result.getRequest().setAttribute(ATTRIBUTE_NAME_CONFIGURATION, map);
		}
		return map;
	}

	public ContractRequestHandler wiremock(RemoteMappingBuilder<?, ?> builder) {
		this.builder = builder;
		return this;
	}

	public ContractRequestHandler jsonPath(String expression, Object... args) {
		compile(expression, args);
		return this;
	}

	public ContractRequestHandler contentType(MediaType contentType) {
		this.contentType = contentType;
		return this;
	}

	private void compile(String expression, Object... args) {
		org.springframework.util.Assert.hasText(
				(expression == null ? null : expression),
				"expression must not be null or empty");
		expression = String.format(expression, args);
		jsonPaths.put(expression, JsonPath.compile(expression));
	}

}

class JsonPathValue {

	private final JsonPath jsonPath;
	private final String expression;
	private final CharSequence actual;

	JsonPathValue(JsonPath jsonPath, CharSequence actual) {
		this.jsonPath = jsonPath;
		this.actual = actual;
		this.expression = jsonPath.getPath();
	}

	public void assertHasValue(Class<?> type, String expectedDescription) {
		Object value = getValue(true);
		if (value == null || isIndefiniteAndEmpty()) {
			throw new AssertionError(getNoValueMessage());
		}
		if (type != null && !type.isInstance(value)) {
			throw new AssertionError(getExpectedValueMessage(expectedDescription));
		}
	}

	private boolean isIndefiniteAndEmpty() {
		return !isDefinite() && isEmpty();
	}

	private boolean isDefinite() {
		return this.jsonPath.isDefinite();
	}

	private boolean isEmpty() {
		return ObjectUtils.isEmpty(getValue(false));
	}

	public Object getValue(boolean required) {
		try {
			CharSequence json = this.actual;
			return this.jsonPath.read(json == null ? null : json.toString());
		}
		catch (Exception ex) {
			if (!required) {
				return null;
			}
			throw new AssertionError(getNoValueMessage() + ". " + ex.getMessage());
		}
	}

	private String getNoValueMessage() {
		return "No value at JSON path \"" + this.expression + "\"";
	}

	private String getExpectedValueMessage(String expectedDescription) {
		return String.format("Expected %s at JSON path \"%s\" but found: %s",
				expectedDescription, this.expression,
				ObjectUtils.nullSafeToString(StringUtils.quoteIfString(getValue(false))));
	}

}
