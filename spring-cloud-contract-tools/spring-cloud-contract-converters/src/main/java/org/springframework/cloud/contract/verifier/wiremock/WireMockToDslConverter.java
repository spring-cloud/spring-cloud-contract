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

package org.springframework.cloud.contract.verifier.wiremock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import repackaged.nl.flotsam.xeger.Xeger;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;

/**
 * Converts WireMock stubs into the DSL format.
 *
 * @author Marcin Grzejszczak
 * @author Konstantin Shevchuk
 * @since 1.0.0
 */
public class WireMockToDslConverter {

	private static final JsonPointer PRIORITY_POINTER = JsonPointer.compile("/priority");

	private static final JsonPointer REQUEST_METHOD_POINTER = JsonPointer.compile("/request/method");

	private static final JsonPointer REQUEST_URL_POINTER = JsonPointer.compile("/request/url");

	private static final JsonPointer REQUEST_URL_PATH_POINTER = JsonPointer.compile("/request/urlPath");

	private static final JsonPointer REQUEST_URL_PATTERN_POINTER = JsonPointer.compile("/request/urlPattern");

	private static final JsonPointer REQUEST_URL_PATH_PATTERN_POINTER = JsonPointer.compile("/request/urlPathPattern");

	private static final JsonPointer REQUEST_HEADERS_POINTER = JsonPointer.compile("/request/headers");

	private static final JsonPointer REQUEST_BODY_POINTER = JsonPointer.compile("/request/bodyPatterns");

	private static final JsonPointer RESPONSE_STATUS_POINTER = JsonPointer.compile("/response/status");

	private static final JsonPointer RESPONSE_BODY_POINTER = JsonPointer.compile("/response/body");

	private static final JsonPointer RESPONSE_HEADERS_POINTER = JsonPointer.compile("/response/headers");

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	/**
	 * Returns the string content of the contract.
	 * @param wireMockStringStub - string content of the WireMock JSON stub
	 */
	public static String fromWireMockStub(String wireMockStringStub) {
		return new WireMockToDslConverter().convertFromWireMockStub(wireMockStringStub);
	}

	private String convertFromWireMockStub(String wireMockStringStub) {
		JsonNode wireMockStub = parseStubDefinition(wireMockStringStub);
		return buildPriority(wireMockStub) + "request {\n" + buildRequestMethod(wireMockStub)
				+ buildRequestUrl(wireMockStub) + buildRequestUrlPattern(wireMockStub)
				+ buildRequestUrlPathPattern(wireMockStub) + buildRequestUrlPath(wireMockStub)
				+ buildRequestHeaders(wireMockStub) + buildRequestBody(wireMockStub) + "}\n" + "response {\n"
				+ buildResponseStatus(wireMockStub) + buildResponseBody(wireMockStub)
				+ buildResponseHeaders(wireMockStub) + "}" + "\n";
	}

	private JsonNode parseStubDefinition(String wireMockStringStub) {
		try {
			return OBJECT_MAPPER.reader().readTree(wireMockStringStub);
		}
		catch (IOException e) {
			throw new RuntimeException("WireMock string stub could not be read", e);
		}
	}

	private String buildPriority(JsonNode wireMockStub) {
		String priority = "";
		JsonNode priorityNode = wireMockStub.at(PRIORITY_POINTER);
		if (priorityNode != null && priorityNode.asInt() > 0) {
			priority = "priority " + priorityNode.asInt() + "\n";
		}
		return priority;
	}

	private String buildRequestMethod(JsonNode wireMockStub) {
		String requestMethod = "";
		JsonNode requestMethodNode = wireMockStub.at(REQUEST_METHOD_POINTER);
		if (requestMethodNode != null) {
			requestMethod = "method '" + requestMethodNode.asText() + "'\n";
		}
		return requestMethod;
	}

	private String buildRequestUrl(JsonNode wireMockStub) {
		String requestUrl = "";
		JsonNode requestUrlNode = wireMockStub.at(REQUEST_URL_POINTER);
		if (!requestUrlNode.isMissingNode()) {
			requestUrl = "url '" + requestUrlNode.asText() + "'\n";
		}
		return requestUrl;
	}

	private String buildRequestUrlPath(JsonNode wireMockStub) {
		String requestUrlPath = "";
		JsonNode requestUrlPathNode = wireMockStub.at(REQUEST_URL_PATH_POINTER);
		if (!requestUrlPathNode.isMissingNode()) {
			requestUrlPath = "url '" + requestUrlPathNode.asText() + "'\n";
		}
		return requestUrlPath;
	}

	private String buildRequestUrlPattern(JsonNode wireMockStub) {
		String requestUrlPattern = "";
		JsonNode requestUrlPatternNode = wireMockStub.at(REQUEST_URL_PATTERN_POINTER);
		if (!requestUrlPatternNode.isMissingNode()) {
			String escapedRequestUrlPatternValue = escapeJava(requestUrlPatternNode.asText());
			requestUrlPattern = "url $(consumer(regex('" + escapedRequestUrlPatternValue + "')), producer('"
					+ new Xeger(escapedRequestUrlPatternValue).generate() + "'))\n";
		}
		return requestUrlPattern;
	}

	private String buildRequestUrlPathPattern(JsonNode wireMockStub) {
		String requestUrlPathPattern = "";
		JsonNode requestUrlPathPatternNode = wireMockStub.at(REQUEST_URL_PATH_PATTERN_POINTER);
		if (!requestUrlPathPatternNode.isMissingNode()) {
			String escapedRequestUrlPathPatternValue = escapeJava(requestUrlPathPatternNode.asText());
			requestUrlPathPattern = "urlPath $(consumer(regex('" + escapedRequestUrlPathPatternValue + "')), producer('"
					+ new Xeger(escapedRequestUrlPathPatternValue).generate() + "'))'\n";
		}
		return requestUrlPathPattern;
	}

	private String buildRequestHeaders(JsonNode wireMockStub) {
		final StringBuilder requestHeadersBuilder = new StringBuilder();
		JsonNode requestHeadersNode = wireMockStub.at(REQUEST_HEADERS_POINTER);

		if (requestHeadersNode.isObject()) {
			requestHeadersBuilder.append("headers {\n");
			ObjectNode requestHeadersObjectNode = requestHeadersNode.deepCopy();
			Iterator<Map.Entry<String, JsonNode>> fields = requestHeadersObjectNode.fields();
			fields.forEachRemaining(c -> {
				requestHeadersBuilder.append("header('").append(c.getKey()).append("',");
				Map.Entry<String, JsonNode> headerValue = c.getValue().deepCopy().fields().next();
				String header = buildHeader(headerValue.getKey(), headerValue.getValue().asText());
				requestHeadersBuilder.append(header).append(")").append("\n");
			});
			requestHeadersBuilder.append("}");
		}
		return requestHeadersBuilder.toString();
	}

	private String buildHeader(String method, String value) {
		switch (method) {
			case "equalTo":
				return "'" + value + "'";
			case "contains":
				String regex = "^.*" + value + ".*$";
				return "c(regex('" + escapeJava(regex) + "'))";
			default:
				return "c(regex('" + escapeJava(value) + "'))";
		}
	}

	private String buildRequestBody(JsonNode wireMockStub) {
		final StringBuilder requestBody = new StringBuilder();
		JsonNode requestBodyNode = wireMockStub.at(REQUEST_BODY_POINTER);
		if (requestBodyNode.isArray()) {
			ArrayNode requestBodyArrayNode = requestBodyNode.deepCopy();
			Iterator<JsonNode> elements = requestBodyArrayNode.elements();
			Iterable<JsonNode> iterableFields = () -> elements;
			List<Map.Entry<String, JsonNode>> requestBodyObjectNodes = new ArrayList<>();
			StreamSupport.stream(iterableFields.spliterator(), false).filter(f -> f instanceof ObjectNode)
					.map(f -> (ObjectNode) f).map(ObjectNode::fields)
					.forEachOrdered(i -> i.forEachRemaining(requestBodyObjectNodes::add));
			requestBodyObjectNodes.stream().filter(b -> b.getKey().equals("equalTo")).findFirst()
					.ifPresent(b -> requestBody.append("body ('").append(b.getValue().asText()).append("')"));
			requestBodyObjectNodes.stream().filter(b -> b.getKey().equals("equalToJson")).findFirst()
					.ifPresent(b -> requestBody.append("body ('").append(b.getValue().asText()).append("')"));
			requestBodyObjectNodes.stream().filter(b -> b.getKey().equals("matches")).findFirst()
					.ifPresent(b -> requestBody.append("body $(consumer(regex('")
							.append(escapeJava(b.getValue().asText())).append("')), producer('")
							.append(new Xeger(escapeJava(b.getValue().asText())).generate()).append("'))"));
		}
		return requestBody.toString();
	}

	private String buildResponseStatus(JsonNode wireMockStub) {
		String responseStatus = "";
		JsonNode responseStatusNode = wireMockStub.at(RESPONSE_STATUS_POINTER);
		if (!responseStatusNode.isMissingNode()) {
			int responseStatusValue = responseStatusNode.asInt();
			responseStatus += "status " + responseStatusValue + "\n";
		}
		return responseStatus;
	}

	private String buildResponseBody(JsonNode wireMockStub) {
		String responseBody = "";
		JsonNode responseBodyNode = wireMockStub.at(RESPONSE_BODY_POINTER);
		if (responseBodyNode.isInt()) {
			responseBody += "body( " + escapeJava(buildPrettyPrintResponseBody((IntNode) responseBodyNode)) + ")\n";
		}
		if (responseBodyNode.isTextual()) {
			responseBody += "body( \"" + escapeJava(buildPrettyPrintResponseBody((TextNode) responseBodyNode))
					+ "\")\n";
		}
		return responseBody;
	}

	private String buildPrettyPrintResponseBody(IntNode node) {
		return node.asText();
	}

	private String buildPrettyPrintResponseBody(TextNode node) {
		try {
			String textNode = node.asText();
			Object intermediateObjectForPrettyPrinting = OBJECT_MAPPER.reader().readValue(textNode, Object.class);
			DefaultIndenter customIndenter = new DefaultIndenter("    ", "\n");
			return OBJECT_MAPPER
					.writer(new PrivatePrettyPrinter().withArrayIndenter(customIndenter)
							.withObjectIndenter(customIndenter))
					.writeValueAsString(intermediateObjectForPrettyPrinting);
		}
		catch (IOException e) {
			throw new RuntimeException("WireMock response body could not be pretty printed");
		}
	}

	private String buildResponseHeaders(JsonNode wireMockStub) {
		final StringBuilder responseHeadersBuilder = new StringBuilder();
		JsonNode requestHeadersNode = wireMockStub.at(RESPONSE_HEADERS_POINTER);

		if (requestHeadersNode.isObject()) {
			responseHeadersBuilder.append("headers {\n");
			ObjectNode responseHeadersObjectNode = requestHeadersNode.deepCopy();
			Iterator<Map.Entry<String, JsonNode>> fields = responseHeadersObjectNode.fields();
			fields.forEachRemaining(c -> responseHeadersBuilder.append("header('").append(c.getKey()).append("',")
					.append("'").append(c.getValue().asText()).append("')\n"));
			responseHeadersBuilder.append("}");
		}
		return responseHeadersBuilder.toString();
	}

	private static class PrivatePrettyPrinter extends DefaultPrettyPrinter {

		@Override
		public DefaultPrettyPrinter createInstance() {
			return new PrivatePrettyPrinter();
		}

		@Override
		public DefaultPrettyPrinter withSeparators(Separators separators) {
			_separators = separators;
			_objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
			return this;
		}

	}

}
