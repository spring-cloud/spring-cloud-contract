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
import org.apache.commons.text.StringEscapeUtils;
import wiremock.com.github.jknack.handlebars.Helper;
import wiremock.com.github.jknack.handlebars.Options;

import org.springframework.cloud.contract.verifier.builder.TestSideRequestTemplateModel;

/**
 * A Handlebars helper for the {@code escapejsonbody} helper function.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class HandlebarsEscapeHelper implements Helper<Map<String, Object>> {

	public static final String NAME = "escapejsonbody";

	public static final String REQUEST_MODEL_NAME = "request";

	@Override
	public Object apply(Map<String, Object> context, Options options) throws IOException {
		Object model = context.get(REQUEST_MODEL_NAME);
		if (model instanceof TestSideRequestTemplateModel) {
			return StringEscapeUtils.escapeJson(returnObjectForTest(model).toString());
		}
		else if (model instanceof RequestTemplateModel) {
			return StringEscapeUtils.escapeJson(returnObjectForStub(model).toString());
		}

		throw new IllegalArgumentException("Unsupported model");
	}

	private Object returnObjectForStub(Object model) {
		return ((RequestTemplateModel) model).getBody();
	}

	private Object returnObjectForTest(Object model) {
		return ((TestSideRequestTemplateModel) model).getEscapedBody();
	}

}
