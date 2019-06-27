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

package org.springframework.cloud.contract.verifier.builder;

import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.verifier.template.TemplateProcessor;

interface GroovyBodyParser extends BodyParser {

	@Override
	default String convertUnicodeEscapesIfRequired(String json) {
		return StringEscapeUtils.unescapeEcmaScript(json);
	}

	@Override
	default String postProcessJsonPath(String jsonPath) {
		if (templateProcessor().containsTemplateEntry(jsonPath)) {
			return jsonPath;
		}
		return jsonPath.replace("$", "\\$");
	}

	TemplateProcessor templateProcessor();

	@Override
	default String escape(String text) {
		return text.replaceAll("\\n", "\\\\n");
	}

	@Override
	default String escapeForSimpleTextAssertion(String text) {
		return escape(text);
	}

	@Override
	default String quotedShortText(Object text) {
		String string = text.toString();
		if (text instanceof Number) {
			return string;
		}
		else if (string.contains("'") || string.contains("\"")) {
			return quotedLongText(text);
		}
		return "'" + groovyEscapedString(text.toString()) + "'";
	}

	@Override
	default String quotedEscapedShortText(Object text) {
		String string = text.toString();
		if (text instanceof Number) {
			return string;
		}
		else if (string.contains("'") || string.contains("\"")) {
			return quotedEscapedLongText(text);
		}
		return "'" + text.toString() + "'";
	}

	@Override
	default String quotedEscapedLongText(Object text) {
		return "'''" + text.toString() + "'''";
	}

	@Override
	default String quotedLongText(Object text) {
		return "'''" + groovyEscapedString(text) + "'''";
	}

	default String groovyEscapedString(Object text) {
		return escape(text.toString()).replaceAll("\\\\\"", "\"");
	}

}
