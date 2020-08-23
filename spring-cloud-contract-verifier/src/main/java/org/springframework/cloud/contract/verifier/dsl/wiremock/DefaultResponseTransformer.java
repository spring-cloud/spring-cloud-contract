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

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import wiremock.com.github.jknack.handlebars.Helper;

import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsEscapeHelper;
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsJsonPathHelper;

/**.
 * Default implementation of {@link ResponseTemplateTransformer} that contains default set
 * of handlebars helpers
 *
 * @author Marcin Grzejszczak
 * @since 1.2.0
 */
public class DefaultResponseTransformer extends ResponseTemplateTransformer {
	public DefaultResponseTransformer() {
		super(false, defaultHelpers());
	}

	public DefaultResponseTransformer(boolean global) {
		super(global);
	}

	public DefaultResponseTransformer(boolean global, String helperName, Helper helper) {
		super(global, helperName, helper);
	}

	public DefaultResponseTransformer(boolean global, Map<String, Helper> helpers) {
		super(global, helpers);
	}

	private static Map<String, Helper> defaultHelpers() {
		Map<String, Helper> helpers = new HashMap<>();
		helpers.put(HandlebarsJsonPathHelper.NAME, new HandlebarsJsonPathHelper());
		helpers.put(HandlebarsEscapeHelper.NAME, new HandlebarsEscapeHelper());
		return helpers;
	}

}
