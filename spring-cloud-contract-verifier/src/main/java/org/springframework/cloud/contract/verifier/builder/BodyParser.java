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

package org.springframework.cloud.contract.verifier.builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import groovy.json.JsonOutput;
import groovy.lang.GString;
import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.MapConverter;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.springframework.cloud.contract.verifier.util.ContentType.DEFINED;
import static org.springframework.cloud.contract.verifier.util.ContentType.FORM;
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentType.TEXT;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.extractValue;

interface BodyParser extends BodyThen {

	String byteArrayString();

	default String convertUnicodeEscapesIfRequired(String json) {
		String unescapedJson = StringEscapeUtils.unescapeEcmaScript(json);
		return escapeJava(unescapedJson);
	}

	default String convertToJsonString(Object bodyValue) {
		String json = JsonOutput.toJson(bodyValue);
		json = convertUnicodeEscapesIfRequired(json);
		return trimRepeatedQuotes(json);
	}

	default String trimRepeatedQuotes(String toTrim) {
		if (toTrim.startsWith("\"")) {
			return toTrim.replaceAll("\"", "");
			// #261
		}
		else if (toTrim.startsWith("\\\"") && toTrim.endsWith("\\\"")) {
			return toTrim.substring(2, toTrim.length() - 2);
		}
		return toTrim;
	}

	default Object convertResponseBody(SingleContractMetadata metadata) {
		ContentType contentType = metadata.getOutputTestContentType();
		DslProperty body = responseBody(metadata);
		Object responseBody = extractServerValueFromBody(contentType, body.getServerValue());
		if (responseBody instanceof FromFileProperty) {
			responseBody = ((FromFileProperty) responseBody).asString();
		}
		else if (responseBody instanceof GString) {
			responseBody = extractValue((GString) responseBody, contentType,
					o -> o instanceof DslProperty ? ((DslProperty) o).getServerValue() : o);
		}
		else if (responseBody instanceof DslProperty) {
			responseBody = MapConverter.getTestSideValues(responseBody);
		}
		return responseBody;
	}

	String responseAsString();

	@SuppressWarnings("unchecked")
	default String requestBodyAsString(SingleContractMetadata metadata) {
		ContentType contentType = metadata.getInputTestContentType();
		DslProperty body = requestBody(metadata);
		Object bodyValue = extractServerValueFromBody(contentType, body.getServerValue());
		if (contentType == ContentType.FORM) {
			if (bodyValue instanceof Map) {
				// [a:3, b:4] == "a=3&b=4"
				return ((Map) bodyValue).entrySet().stream().map(o -> {
					Map.Entry entry = (Map.Entry) o;
					return convertUnicodeEscapesIfRequired(
							entry.getKey().toString() + "=" + MapConverter.getTestSideValuesForText(entry.getValue()));
				}).collect(Collectors.joining("&")).toString();
			}
			else if (bodyValue instanceof List) {
				// ["a=3", "b=4"] == "a=3&b=4"
				return ((List) bodyValue).stream()
						.map(o -> convertUnicodeEscapesIfRequired(MapConverter.getTestSideValuesForText(o).toString()))
						.collect(Collectors.joining("&")).toString();
			}
		}
		else {
			return convertToJsonString(bodyValue);
		}
		return "";
	}

	/**
	 * Converts the passed body into ints server side representation. All
	 * {@link DslProperty} will return their server side values
	 */
	default Object extractServerValueFromBody(ContentType contentType, Object bodyValue) {
		if (bodyValue instanceof GString) {
			return extractValue((GString) bodyValue, contentType, ContentUtils.GET_TEST_SIDE_FUNCTION);
		}
		else if (bodyValue instanceof FromFileProperty) {
			return MapConverter.transformValues(bodyValue, ContentUtils.GET_TEST_SIDE_FUNCTION);
		}
		else if (TEXT != contentType && FORM != contentType && DEFINED != contentType) {
			boolean dontParseStrings = contentType == JSON && bodyValue instanceof Map;
			Function<String, Object> parsingClosure = dontParseStrings ? MapConverter.IDENTITY
					: MapConverter.JSON_PARSING_FUNCTION;
			return MapConverter.getTestSideValues(bodyValue, parsingClosure);
		}
		return bodyValue;
	}

	default String escape(String text) {
		return StringEscapeUtils.escapeJava(text);
	}

	default String escapeForSimpleTextAssertion(String text) {
		return text;
	}

	default String postProcessJsonPath(String jsonPath) {
		return jsonPath;
	}

	default String quotedLongText(Object text) {
		return quotedEscapedLongText(escape(text.toString()));
	}

	default String quotedEscapedLongText(Object text) {
		return "\"" + text.toString() + "\"";
	}

	default String quotedShortText(Object text) {
		return quotedLongText(text);
	}

	default String quotedEscapedShortText(Object text) {
		return quotedEscapedLongText(text);
	}

}
