/*
 * Copyright 2013-present the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import repackaged.nl.flotsam.xeger.Xeger;
import tools.jackson.core.JsonPointer;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static tools.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
import static tools.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES;
import static tools.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES;
import static tools.jackson.core.json.JsonReadFeature.ALLOW_YAML_COMMENTS;

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

	private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
			.configure(ALLOW_JAVA_COMMENTS, true)
			.configure(ALLOW_YAML_COMMENTS, true)
			.configure(ALLOW_UNQUOTED_PROPERTY_NAMES, true)
			.configure(ALLOW_SINGLE_QUOTES, true)
			.build();

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
		catch (Exception e) {
			throw new RuntimeException("WireMock string stub could not be read", e);
		}
	}

	private String buildPriority(JsonNode wireMockStub) {
		String priority = "";
		JsonNode priorityNode = wireMockStub.at(PRIORITY_POINTER);
		if (!priorityNode.isMissingNode() && priorityNode.asInt() > 0) {
			priority = "priority " + priorityNode.asInt() + "\n";
		}
		return priority;
	}

	private String buildRequestMethod(JsonNode wireMockStub) {
		String requestMethod = "";
		JsonNode requestMethodNode = wireMockStub.at(REQUEST_METHOD_POINTER);
		if (requestMethodNode != null) {
			requestMethod = "method '" + requestMethodNode.asString() + "'\n";
		}
		return requestMethod;
	}

	private String buildRequestUrl(JsonNode wireMockStub) {
		String requestUrl = "";
		JsonNode requestUrlNode = wireMockStub.at(REQUEST_URL_POINTER);
		if (!requestUrlNode.isMissingNode()) {
			requestUrl = "url '" + requestUrlNode.asString() + "'\n";
		}
		return requestUrl;
	}

	private String buildRequestUrlPath(JsonNode wireMockStub) {
		String requestUrlPath = "";
		JsonNode requestUrlPathNode = wireMockStub.at(REQUEST_URL_PATH_POINTER);
		if (!requestUrlPathNode.isMissingNode()) {
			requestUrlPath = "url '" + requestUrlPathNode.asString() + "'\n";
		}
		return requestUrlPath;
	}

	private String buildRequestUrlPattern(JsonNode wireMockStub) {
		String requestUrlPattern = "";
		JsonNode requestUrlPatternNode = wireMockStub.at(REQUEST_URL_PATTERN_POINTER);
		if (!requestUrlPatternNode.isMissingNode()) {
			String escapedRequestUrlPatternValue = escapeJava(requestUrlPatternNode.asString());
			requestUrlPattern = "url $(consumer(regex('" + escapedRequestUrlPatternValue + "')), producer('"
					+ new Xeger(escapedRequestUrlPatternValue).generate() + "'))\n";
		}
		return requestUrlPattern;
	}

	private String buildRequestUrlPathPattern(JsonNode wireMockStub) {
		String requestUrlPathPattern = "";
		JsonNode requestUrlPathPatternNode = wireMockStub.at(REQUEST_URL_PATH_PATTERN_POINTER);
		if (!requestUrlPathPatternNode.isMissingNode()) {
			String escapedRequestUrlPathPatternValue = escapeJava(requestUrlPathPatternNode.asString());
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
			JsonNode requestHeadersObjectNode = requestHeadersNode.deepCopy();
			Set<Map.Entry<String, JsonNode>> fields = requestHeadersObjectNode.properties();
			fields.forEach(c -> {
				requestHeadersBuilder.append("header('").append(c.getKey()).append("',");
				ObjectNode headersNode = (ObjectNode) c.getValue().deepCopy();
				Iterator<Map.Entry<String, JsonNode>> headersNodeIterator = headersNode.properties()
						.iterator();
				if (headersNodeIterator.hasNext()) {
					Map.Entry<String, JsonNode> headerValue = headersNodeIterator.next();
					String header = buildHeader(headerValue.getKey(), headerValue.getValue()
							.asString());
					requestHeadersBuilder.append(header).append(")").append("\n");
				}
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
			ArrayNode requestBodyArrayNode = (ArrayNode) requestBodyNode.deepCopy();
			Collection<JsonNode> elements = requestBodyArrayNode.elements();
			List<Map.Entry<String, JsonNode>> requestBodyObjectNodes = new ArrayList<>();

			elements.stream()
					.filter(f -> f instanceof ObjectNode)
					.map(f -> (ObjectNode) f)
					.map(ObjectNode::properties)
					.forEachOrdered(requestBodyObjectNodes::addAll);
			requestBodyObjectNodes.stream()
					.filter(b -> b.getKey().equals("equalTo"))
					.findFirst()
					.ifPresent(b -> requestBody.append("body ('")
							.append(b.getValue().asString()).append("')"));
			requestBodyObjectNodes.stream()
					.filter(b -> b.getKey().equals("equalToJson"))
					.findFirst()
					.ifPresent(b -> requestBody.append("body ('")
							.append(b.getValue().asString()).append("')"));
			requestBodyObjectNodes.stream()
					.filter(b -> b.getKey().equals("matches"))
					.findFirst()
					.ifPresent(b -> requestBody.append("body $(consumer(regex('")
							.append(escapeJava(b.getValue().asString()))
							.append("')), producer('")
							.append(new Xeger(escapeJava(b.getValue()
									.asString())).generate())
							.append("'))"));
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
		if (responseBodyNode.isString()) {
			responseBody += "body( \"" + escapeJava(buildPrettyPrintResponseBody((StringNode) responseBodyNode))
					+ "\")\n";
		}
		return responseBody;
	}

	private String buildPrettyPrintResponseBody(IntNode node) {
		return node.asString();
	}

	private String buildPrettyPrintResponseBody(StringNode node) {
		try {
			String stringNode = node.asString();
			Object intermediateObjectForPrettyPrinting = OBJECT_MAPPER.readerFor(Object.class)
					.readValue(stringNode);

			DefaultIndenter customIndenter = new DefaultIndenter("    ", "\n");
			return OBJECT_MAPPER.writer()
					.with(new DefaultPrettyPrinter().withArrayIndenter(customIndenter)
							.withObjectIndenter(customIndenter))
					.writeValueAsString(intermediateObjectForPrettyPrinting);
		}
		catch (Exception e) {
			throw new RuntimeException("WireMock response body could not be pretty printed", e);
		}
	}

	private String buildResponseHeaders(JsonNode wireMockStub) {
		final StringBuilder responseHeadersBuilder = new StringBuilder();
		JsonNode requestHeadersNode = wireMockStub.at(RESPONSE_HEADERS_POINTER);

		if (requestHeadersNode.isObject()) {
			responseHeadersBuilder.append("headers {\n");
			JsonNode responseHeadersObjectNode = requestHeadersNode.deepCopy();
			Set<Map.Entry<String, JsonNode>> fields = responseHeadersObjectNode.properties();
			fields.forEach(c -> responseHeadersBuilder.append("header('")
					.append(c.getKey())
					.append("',")
					.append("'")
					.append(c.getValue().asString())
					.append("')\n"));
			responseHeadersBuilder.append("}");
		}
		return responseHeadersBuilder.toString();
	}

}
