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

package org.springframework.cloud.contract.verifier.builder.handlebars;

import java.io.IOException;
import java.util.Map;

import com.github.tomakehurst.wiremock.extension.responsetemplating.RequestTemplateModel;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.WireMockHelpers;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import wiremock.com.github.jknack.handlebars.Helper;
import wiremock.com.github.jknack.handlebars.Options;

import org.springframework.cloud.contract.verifier.builder.TestSideRequestTemplateModel;

/**
 * A Handlebars helper for the {@code jsonpath} helper function.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class HandlebarsJsonPathHelper implements Helper<Object> {

	public static final String NAME = "jsonpath";

	public static final String REQUEST_MODEL_NAME = "request";

	@Override
	public Object apply(Object context, Options options) throws IOException {
		if (context instanceof Map) {
			// legacy
			Map<String, Object> oldContext = (Map<String, Object>) context;
			String jsonPath = options.param(0);
			Object model = oldContext.get(REQUEST_MODEL_NAME);
			if (model instanceof TestSideRequestTemplateModel) {
				return returnObjectForTest((TestSideRequestTemplateModel) model,
						jsonPath);
			}
			else if (model instanceof RequestTemplateModel) {
				return returnObjectForStub(model, jsonPath);
			}
			throw new IllegalArgumentException("Unsupported model");
		}
		else if (context instanceof String) {
			Object value = WireMockHelpers.jsonPath.apply(context, options);
			if (testSideModel(options)) {
				return processTestResponseValue(value);
			}
			return value;
		}
		throw new IllegalArgumentException("Unsupported context");
	}

	private boolean testSideModel(Options options) {
		Object model = options.context.model();
		if (!(model instanceof Map)) {
			return false;
		}

		Map map = (Map) model;
		return map.values().stream()
				.anyMatch(o -> o instanceof TestSideRequestTemplateModel);
	}

	private Object returnObjectForStub(Object model, String jsonPath) {
		DocumentContext documentContext = JsonPath
				.parse(((RequestTemplateModel) model).getBody());
		return documentContext.read(jsonPath);
	}

	private Object returnObjectForTest(TestSideRequestTemplateModel model,
			String jsonPath) {
		String body = removeSurroundingQuotes(model.getEscapedBody()).replace("\\\"",
				"\"");
		DocumentContext documentContext = JsonPath.parse(body);
		Object value = documentContext.read(jsonPath);
		return processTestResponseValue(value);
	}

	private Object processTestResponseValue(Object value) {
		if (value instanceof Long) {
			return (long) value + "L";
		}
		return value;
	}

	private String removeSurroundingQuotes(String body) {
		if (body.startsWith("\"") && body.endsWith("\"")) {
			return body.substring(1, body.length() - 1);
		}
		return body;
	}

}
