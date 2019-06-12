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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import groovy.json.JsonOutput;
import groovy.lang.Closure;
import groovy.lang.GString;
import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.internal.Body;
import org.springframework.cloud.contract.spec.internal.Cookie;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern;
import org.springframework.cloud.contract.spec.internal.OptionalProperty;
import org.springframework.cloud.contract.spec.internal.QueryParameter;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.config.TestMode;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.MapConverter;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.extractValue;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.getJavaMultipartFileParameterContent;

class ClassToBuildMockMvc {

}

/**
 *
 */
interface MockMvcAcceptor {

	default boolean acceptMockMvc(GeneratedClassMetaData generatedClassMetaData,
			SingleContractMetadata singleContractMetadata) {
		return generatedClassMetaData.configProperties.getTestMode() == TestMode.MOCKMVC
				&& singleContractMetadata.isHttp();
	}

}

class MockMvcAsyncWhen implements When, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	MockMvcAsyncWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		if (response.getAsync()) {
			this.blockBuilder.addIndented(".when().async()");
		}
		if (response.getDelay() != null) {
			this.blockBuilder.addIndented(
					".timeout(" + response.getDelay().getServerValue() + ")");
		}
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		boolean accept = acceptMockMvc(this.generatedClassMetaData, metadata);
		if (!accept) {
			return false;
		}
		Response response = metadata.getContract().getResponse();
		return response.getAsync() || response.getDelay() != null;
	}

}

class MockMvcBodyGiven implements Given {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	MockMvcBodyGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(this.blockBuilder, metadata);
		return this;
	}

	private void processInput(BlockBuilder bb, SingleContractMetadata metadata) {
		Object body;
		Request request = metadata.getContract().getRequest();
		Object serverValue = request.getBody().getServerValue();
		if (serverValue instanceof ExecutionProperty
				|| serverValue instanceof FromFileProperty) {
			body = request.getBody().getServerValue();
		}
		else {
			body = getBodyAsString(metadata);
		}
		bb.addIndented(getBodyString(metadata, body));
	}

	private String getBodyString(SingleContractMetadata metadata, Object body) {
		String value;
		if (body instanceof ExecutionProperty) {
			value = body.toString();
		}
		else if (body instanceof FromFileProperty) {
			FromFileProperty fileProperty = (FromFileProperty) body;
			value = fileProperty.isByte()
					? this.bodyReader.readBytesFromFileString(metadata, fileProperty,
							CommunicationType.REQUEST)
					: this.bodyReader.readStringFromFileString(metadata, fileProperty,
							CommunicationType.REQUEST);
		}
		else {
			String escaped = escapeRequestSpecialChars(metadata, body.toString());
			value = "\"" + escaped + "\"";
		}
		return ".body(" + value + ")";
	}

	@SuppressWarnings("unchecked")
	private String getBodyAsString(SingleContractMetadata metadata) {
		ContentType contentType = metadata.getInputContentType();
		Body body = metadata.getContract().getRequest().getBody();
		Object bodyValue = extractServerValueFromBody(contentType, body.getServerValue());
		if (contentType == ContentType.FORM) {
			if (bodyValue instanceof Map) {
				// [a:3, b:4] == "a=3&b=4"
				return ((Map) bodyValue).entrySet().stream().map(o -> {
					Map.Entry entry = (Map.Entry) o;
					return convertUnicodeEscapesIfRequired(
							entry.getKey().toString() + "=" + entry.getValue());
				}).collect(Collectors.joining("&")).toString();
			}
			else if (bodyValue instanceof List) {
				// ["a=3", "b=4"] == "a=3&b=4"
				return ((List) bodyValue).stream()
						.map(o -> convertUnicodeEscapesIfRequired(o.toString()))
						.collect(Collectors.joining("&")).toString();
			}
		}
		else {
			String json = JsonOutput.toJson(bodyValue);
			json = convertUnicodeEscapesIfRequired(json);
			return trimRepeatedQuotes(json);
		}
		return "";
	}

	private String convertUnicodeEscapesIfRequired(String json) {
		String unescapedJson = StringEscapeUtils.unescapeEcmaScript(json);
		return escapeJava(unescapedJson);
	}

	private String trimRepeatedQuotes(String toTrim) {
		if (toTrim.startsWith("\"")) {
			return toTrim.replaceAll("\"", "");
			// #261
		}
		else if (toTrim.startsWith("\\\"") && toTrim.endsWith("\\\"")) {
			return toTrim.substring(2, toTrim.length() - 2);
		}
		return toTrim;
	}

	/**
	 * Converts the passed body into ints server side representation. All
	 * {@link DslProperty} will return their server side values
	 */
	private Object extractServerValueFromBody(ContentType contentType, Object bodyValue) {
		if (bodyValue instanceof GString) {
			return extractValue((GString) bodyValue, contentType,
					MapConverterUtils.GET_SERVER_VALUE);
		}
		boolean dontParseStrings = contentType == JSON && bodyValue instanceof Map;
		Closure parsingClosure = dontParseStrings ? Closure.IDENTITY
				: MapConverter.JSON_PARSING_CLOSURE;
		return MapConverter.transformValues(bodyValue, MapConverterUtils.GET_SERVER_VALUE,
				parsingClosure);
	}

	private String escapeRequestSpecialChars(SingleContractMetadata metadata,
			String string) {
		if (metadata.getInputContentType() == ContentType.JSON) {
			return string.replaceAll("\\\\n", "\\\\\\\\n");
		}
		return string;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getBody() != null;
	}

}

class MockMvcCookiesGiven implements Given {

	private final BlockBuilder blockBuilder;

	MockMvcCookiesGiven(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(this.blockBuilder, metadata.getContract().getRequest());
		return this;
	}

	private void processInput(BlockBuilder bb, Request request) {
		request.getCookies().executeForEachCookie(cookie -> {
			if (ofAbsentType(cookie)) {
				return;
			}
			bb.addIndented(string(cookie));
		});
	}

	private String string(Cookie cookie) {
		return ".cookie(" + ContentHelper.getTestSideForNonBodyValue(cookie.getKey())
				+ ", " + ContentHelper.getTestSideForNonBodyValue(cookie.getServerValue())
				+ ")";
	}

	private boolean ofAbsentType(Cookie cookie) {
		return cookie.getServerValue() instanceof MatchingStrategy
				&& MatchingStrategy.Type.ABSENT
						.equals(((MatchingStrategy) cookie.getServerValue()).getType());
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getCookies() != null;
	}

}

class MockMvcGiven implements Given, BodyMethodVisitor, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Given> givens = new LinkedList<>();

	MockMvcGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.givens.addAll(Arrays.asList(new MockMvcHeadersGiven(blockBuilder),
				new MockMvcCookiesGiven(blockBuilder),
				new MockMvcBodyGiven(blockBuilder, generatedClassMetaData),
				new MockMvcMultipartGiven(blockBuilder, generatedClassMetaData)));
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "// given:")
				.addIndented("MockMvcRequestSpecification request = given()");
		indentedBodyBlock(this.blockBuilder, this.givens, singleContractMetadata);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptMockMvc(this.generatedClassMetaData, singleContractMetadata);
	}

}

class MockMvcHeadersGiven implements Given {

	private final BlockBuilder blockBuilder;

	MockMvcHeadersGiven(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(this.blockBuilder, metadata.getContract().getRequest().getHeaders());
		return this;
	}

	private void processInput(BlockBuilder bb, Headers headers) {
		headers.executeForEachHeader(header -> {
			if (ofAbsentType(header)) {
				return;
			}
			bb.addIndented(string(header));
		});
	}

	private String string(Header header) {
		return ".header(" + ContentHelper.getTestSideForNonBodyValue(header.getName())
				+ ", " + ContentHelper.getTestSideForNonBodyValue(header.getServerValue())
				+ ")";
	}

	private boolean ofAbsentType(Header header) {
		return header.getServerValue() instanceof MatchingStrategy
				&& MatchingStrategy.Type.ABSENT
						.equals(((MatchingStrategy) header.getServerValue()).getType());
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getHeaders() != null;
	}

}

class MockMvcMultipartGiven implements Given {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	MockMvcMultipartGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		getMultipartParameters(metadata).entrySet().forEach(entry -> this.blockBuilder
				.addIndented(getMultipartParameterLine(metadata, entry)));
		return this;
	}

	private String getMultipartParameterLine(SingleContractMetadata metadata,
			Map.Entry<String, Object> parameter) {
		if (parameter.getValue() instanceof NamedProperty) {
			return ".multiPart(" + getMultipartFileParameterContent(metadata,
					parameter.getKey(), (NamedProperty) parameter.getValue()) + ")";
		}
		return getParameterString(parameter);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMultipartParameters(SingleContractMetadata metadata) {
		return (Map<String, Object>) metadata.getContract().getRequest().getMultipart()
				.getServerValue();
	}

	private String getMultipartFileParameterContent(SingleContractMetadata metadata,
			String propertyName, NamedProperty propertyValue) {
		return getJavaMultipartFileParameterContent(propertyName, propertyValue,
				fileProp -> this.bodyReader.readBytesFromFileString(metadata, fileProp,
						CommunicationType.REQUEST));
	}

	private String getParameterString(Map.Entry<String, Object> parameter) {
		return ".param(\"" + escapeJava(parameter.getKey()) + "\", \""
				+ escapeJava((String) parameter.getValue()) + "\")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getMultipart() != null;
	}

}

class MockMvcUrlWhen implements When, MockMvcAcceptor {

	private static final String DOUBLE_QUOTE = "\"";

	private static final String QUERY_PARAM_METHOD = "queryParam";

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	MockMvcUrlWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		Url url = getUrl(request);
		addQueryParameters(url);
		addUrl(url, request);
		return this;
	}

	private Url getUrl(Request request) {
		if (request.getUrl() != null) {
			return request.getUrl();
		}
		if (request.getUrlPath() != null) {
			return request.getUrlPath();
		}
		throw new IllegalStateException("URL is not set!");
	}

	private void addQueryParameters(Url buildUrl) {
		if (buildUrl.getQueryParameters() != null) {
			buildUrl.getQueryParameters().getParameters().stream()
					.filter(this::allowedQueryParameter).forEach(this::addQueryParameter);
		}
	}

	private boolean allowedQueryParameter(Object o) {
		if (o instanceof QueryParameter) {
			return allowedQueryParameter(((QueryParameter) o).getServerValue());
		}
		else if (o instanceof MatchingStrategy) {
			return MatchingStrategy.Type.ABSENT.equals(((MatchingStrategy) o).getType());
		}
		return true;
	}

	private void addQueryParameter(QueryParameter queryParam) {
		this.blockBuilder.addLine("." + QUERY_PARAM_METHOD + "(" + DOUBLE_QUOTE
				+ queryParam.getName() + DOUBLE_QUOTE + "," + DOUBLE_QUOTE
				+ resolveParamValue(queryParam) + DOUBLE_QUOTE + ")");
	}

	/**
	 * Converts the query parameter value into String
	 */
	private String resolveParamValue(Object value) {
		if (value instanceof QueryParameter) {
			return resolveParamValue(((QueryParameter) value).getServerValue());
		}
		else if (value instanceof OptionalProperty) {
			return resolveParamValue(((OptionalProperty) value).optionalPattern());
		}
		else if (value instanceof MatchingStrategy) {
			return ((MatchingStrategy) value).getServerValue().toString();
		}
		return value.toString();
	}

	private void addUrl(Url buildUrl, Request request) {
		Object testSideUrl = MapConverter.getTestSideValues(buildUrl);
		String method = request.getMethod().getServerValue().toString().toLowerCase();
		String url = testSideUrl.toString();
		if (!(testSideUrl instanceof ExecutionProperty)) {
			url = DOUBLE_QUOTE + testSideUrl.toString() + DOUBLE_QUOTE;
		}
		this.blockBuilder.addIndented("." + method + "(" + url + ")");
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}

class MockMvcWhen implements When, BodyMethodVisitor, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> whens = new LinkedList<>();

	MockMvcWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.whens.addAll(Arrays.asList(
				new MockMvcUrlWhen(this.blockBuilder, this.generatedClassMetaData),
				new MockMvcAsyncWhen(this.blockBuilder, this.generatedClassMetaData)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "// when:")
				.addIndented("ResponseOptions response = given().spec(request)");
		indentedBodyBlock(this.blockBuilder, this.whens, singleContractMetadata);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptMockMvc(this.generatedClassMetaData, singleContractMetadata);
	}

}

class MockMvcThen implements Then, BodyMethodVisitor, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Then> thens = new LinkedList<>();

	MockMvcThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.thens.addAll(Arrays.asList(
				new MockMvcStatusCodeThen(this.blockBuilder),
				new MockMvcHeadersThen(this.blockBuilder, generatedClassMetaData)
				));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "// then:");
		bodyBlock(this.blockBuilder, this.thens, singleContractMetadata);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptMockMvc(this.generatedClassMetaData, singleContractMetadata);
	}

}

class MockMvcStatusCodeThen implements Then, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	MockMvcStatusCodeThen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		this.blockBuilder.addLineWithEnding("assertThat(response.statusCode()).isEqualTo(" + response.getStatus().getServerValue() + ")");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}

class MockMvcHeadersThen implements Then, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	MockMvcHeadersThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		Headers headers = response.getHeaders();
		headers.executeForEachHeader(header -> {
			processHeaderElement(header.getName(), header.getServerValue() instanceof NotToEscapePattern ?
					header.getServerValue() :
					MapConverter.getTestSideValues(header.getServerValue()));
		});
		return this;
	}

	private void processHeaderElement(String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			this.blockBuilder.addLineWithEnding("assertThat(response.header(\"" + property + "\"))."
					+
					createMatchesMethod(((NotToEscapePattern) value).getServerValue().pattern().replace("\\", "\\\\")));
		} else if (value instanceof String || value instanceof Pattern) {
			this.blockBuilder.
					addLineWithEnding("assertThat(response.header(\"" + property + "\"))." + createHeaderComparison(value));
		} else if (value instanceof Number) {
			this.blockBuilder.
					addLineWithEnding("assertThat(response.header(\"" + property + "\")).isEqualTo(" + value + ")");
		} else if (value instanceof ExecutionProperty) {
			this.blockBuilder.addLineWithEnding(((ExecutionProperty) value).insertValue("response.header(\"" + property + "\")"));

		}
		else {
			// fallback
			processHeaderElement(property, value.toString());
		}
	}

	private String createMatchesMethod(String pattern) {
		return "matches(\"" + pattern + "\")";
	}

	private String createHeaderComparison(Object headerValue) {
		if (headerValue instanceof Pattern) {
			return createHeaderComparison((Pattern) headerValue);
		}
		String escapedHeader = convertUnicodeEscapesIfRequired(headerValue.toString());
		return "isEqualTo(\"" + escapedHeader + "\")";
	}

	private String convertUnicodeEscapesIfRequired(String json) {
		String unescapedJson = StringEscapeUtils.unescapeJson(json);
		return escapeJava(unescapedJson);
	}

	private String createHeaderComparison(Pattern headerValue) {
		return createMatchesMethod(escapeJava(headerValue.pattern()));
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		boolean accept = acceptMockMvc(this.generatedClassMetaData, metadata);
		if (!accept) {
			return false;
		}
		Response response = metadata.getContract().getResponse();
		return response.getHeaders() != null;
	}

}

class MockMvcRestAssured3Imports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = {
			"io.restassured.module.mockmvc.specification.MockMvcRequestSpecification",
			"io.restassured.response.ResponseOptions" };

	MockMvcRestAssured3Imports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestMode() == TestMode.MOCKMVC
				&& this.generatedClassMetaData.isAnyHttp();
	}

}

class MockMvcRestAssured3StaticImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = {
			"io.restassured.module.mockmvc.RestAssuredMockMvc.*" };

	MockMvcRestAssured3StaticImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import static " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestMode() == TestMode.MOCKMVC;
	}

}
