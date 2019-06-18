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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import groovy.json.JsonOutput;
import groovy.lang.Closure;
import groovy.lang.GString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.ContractTemplate;
import org.springframework.cloud.contract.spec.internal.Body;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.Cookie;
import org.springframework.cloud.contract.spec.internal.Cookies;
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
import org.springframework.cloud.contract.spec.internal.QueryParameters;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.config.TestMode;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.cloud.contract.verifier.util.RegexpBuilders;
import org.springframework.util.StringUtils;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.springframework.cloud.contract.verifier.util.ContentType.DEFINED;
import static org.springframework.cloud.contract.verifier.util.ContentType.FORM;
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentType.TEXT;
import static org.springframework.cloud.contract.verifier.util.ContentType.XML;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.extractValue;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.getJavaMultipartFileParameterContent;

class ClassToBuildRestAssured {

}

interface JaxRsAcceptor {

	default boolean acceptType(GeneratedClassMetaData generatedClassMetaData,
			SingleContractMetadata singleContractMetadata) {
		return generatedClassMetaData.configProperties
				.getTestMode() == TestMode.JAXRSCLIENT && singleContractMetadata.isHttp();
	}

}

interface MockMvcAcceptor {

	default boolean acceptType(GeneratedClassMetaData generatedClassMetaData,
			SingleContractMetadata singleContractMetadata) {
		return generatedClassMetaData.configProperties.getTestMode() == TestMode.MOCKMVC
				&& singleContractMetadata.isHttp();
	}

}

interface ExplicitAcceptor {

	default boolean acceptType(GeneratedClassMetaData generatedClassMetaData) {
		return generatedClassMetaData.configProperties.getTestMode() == TestMode.EXPLICIT;
	}

}

interface WebTestClientAcceptor {

	default boolean acceptType(GeneratedClassMetaData generatedClassMetaData) {
		return generatedClassMetaData.configProperties
				.getTestMode() == TestMode.WEBTESTCLIENT;
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
		boolean accept = acceptType(this.generatedClassMetaData, metadata);
		if (!accept) {
			return false;
		}
		Response response = metadata.getContract().getResponse();
		return response.getAsync() || response.getDelay() != null;
	}

}

class MockMvcBodyGiven implements Given, RestAssuredBodyParser {

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
			body = requestBodyAsString(metadata);
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

	private String escapeRequestSpecialChars(SingleContractMetadata metadata,
			String string) {
		if (metadata.getInputTestContentType() == ContentType.JSON) {
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
			bb.addLine(string(cookie));
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

class RestAssuredGiven implements Given, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Given> requestGivens = new LinkedList<>();

	private final List<Given> bodyGivens = new LinkedList<>();

	RestAssuredGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.requestGivens.addAll(Arrays.asList(
				new MockMvcRequestGiven(blockBuilder, generatedClassMetaData),
				new ExplicitRequestGiven(blockBuilder, generatedClassMetaData),
				new WebTestClientRequestGiven(blockBuilder, generatedClassMetaData)));
		this.bodyGivens.addAll(Arrays.asList(new MockMvcHeadersGiven(blockBuilder),
				new MockMvcCookiesGiven(blockBuilder),
				new MockMvcBodyGiven(blockBuilder, generatedClassMetaData),
				new MockMvcMultipartGiven(blockBuilder, generatedClassMetaData)));
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "given:");
		addRequestGivenLine(singleContractMetadata);
		indentedBodyBlock(this.blockBuilder, this.bodyGivens, singleContractMetadata);
		return this;
	}

	private void addRequestGivenLine(SingleContractMetadata singleContractMetadata) {
		this.requestGivens.stream().filter(given -> given.accept(singleContractMetadata))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"No matching request building Given implementation for Rest Assured"))
				.apply(singleContractMetadata);
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isHttp()
				&& this.generatedClassMetaData.configProperties
						.getTestMode() != TestMode.JAXRSCLIENT;
	}

}

class MockMvcRequestGiven implements Given, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	MockMvcRequestGiven(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented("MockMvcRequestSpecification request = given()");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata);
	}

}

class JaxRsRequestGiven implements Given, JaxRsAcceptor {

	private final GeneratedClassMetaData generatedClassMetaData;

	JaxRsRequestGiven(GeneratedClassMetaData metaData) {
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata);
	}

}

class ExplicitRequestGiven implements Given, ExplicitAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	ExplicitRequestGiven(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented("RequestSpecification request = given()");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData);
	}

}

class WebTestClientRequestGiven implements Given, WebTestClientAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	WebTestClientRequestGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		this.blockBuilder
				.addIndented("WebTestClientRequestSpecification request = given()");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData);
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

class MockMvcUrlWhen implements When, MockMvcAcceptor, QueryParamsResolver {

	private static final String DOUBLE_QUOTE = "\"";

	private static final String QUERY_PARAM_METHOD = "queryParam";

	private final BlockBuilder blockBuilder;

	MockMvcUrlWhen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
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

interface QueryParamsResolver {

	/**
	 * Converts the query parameter value into String
	 */
	default String resolveParamValue(Object value) {
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

}

class RestAssuredWhen implements When, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> responseWhens = new LinkedList<>();

	private final List<When> whens = new LinkedList<>();

	RestAssuredWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.responseWhens.addAll(Arrays.asList(
				new MockMvcResponseWhen(blockBuilder, this.generatedClassMetaData),
				new ExplicitResponseWhen(blockBuilder, this.generatedClassMetaData),
				new WebTestClientResponseWhen(blockBuilder,
						this.generatedClassMetaData)));
		this.whens.addAll(Arrays.asList(new MockMvcUrlWhen(this.blockBuilder),
				new MockMvcAsyncWhen(this.blockBuilder, this.generatedClassMetaData)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "when:");
		addResponseWhenLine(singleContractMetadata);
		indentedBodyBlock(this.blockBuilder, this.whens, singleContractMetadata);
		return this;
	}

	private void addResponseWhenLine(SingleContractMetadata singleContractMetadata) {
		this.responseWhens.stream().filter(when -> when.accept(singleContractMetadata))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"No matching request building When implementation for Rest Assured"))
				.apply(singleContractMetadata);
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isHttp()
				&& this.generatedClassMetaData.configProperties
						.getTestMode() != TestMode.JAXRSCLIENT;
	}

}

class JaxRsWhen implements When, BodyMethodVisitor, JaxRsAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> whens = new LinkedList<>();

	JaxRsWhen(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.whens.addAll(Arrays.asList(
				new JaxRsUrlPathWhen(this.blockBuilder, this.generatedClassMetaData),
				new JaxRsRequestWhen(this.blockBuilder, this.generatedClassMetaData),
				new JaxRsRequestHeadersWhen(this.blockBuilder),
				new JaxRsRequestCookiesWhen(this.blockBuilder),
				new JaxRsRequestMethodWhen(this.blockBuilder,
						this.generatedClassMetaData),
				new JaxRsRequestInvokerWhen(this.blockBuilder)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "when:");
		this.blockBuilder.addIndented("Response response = webTarget");
		this.blockBuilder.indent();
		indentedBodyBlock(this.blockBuilder, this.whens, singleContractMetadata);
		this.blockBuilder.addEmptyLine().endBlock();
		if (expectsResponseBody(singleContractMetadata)) {
			this.blockBuilder.addLineWithEnding(
					"String responseAsString = response.readEntity(String.class)");
		}
		this.blockBuilder.endBlock();
		return this;
	}

	private boolean expectsResponseBody(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBody() != null;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptType(this.generatedClassMetaData, singleContractMetadata);
	}

}

class JaxRsThen implements Then, BodyMethodVisitor, JaxRsAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Then> thens = new LinkedList<>();

	JaxRsThen(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.thens.addAll(Arrays.asList(new JaxRsStatusCodeThen(this.blockBuilder),
				new JaxRsResponseHeadersThen(this.blockBuilder),
				new JaxRsResponseCookiesThen(this.blockBuilder),
				new GenericBodyThen(this.blockBuilder, generatedClassMetaData,
						JaxRsBodyParser.INSTANCE)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "then:");
		bodyBlock(this.blockBuilder, this.thens, singleContractMetadata);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptType(this.generatedClassMetaData, singleContractMetadata);
	}

}

class JaxRsStatusCodeThen implements Then {

	private final BlockBuilder blockBuilder;

	JaxRsStatusCodeThen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		this.blockBuilder
				.addIndented("assertThat(response.getStatus()).isEqualTo("
						+ response.getStatus().getServerValue() + ")")
				.addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}

class JaxRsResponseHeadersThen implements Then, HeaderOrCookieComparisonBuilder {

	private final BlockBuilder blockBuilder;

	JaxRsResponseHeadersThen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		validateResponseHeadersBlock(metadata);
		return this;
	}

	private void validateResponseHeadersBlock(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		response.getHeaders().executeForEachHeader(header -> processHeaderElement(
				header.getName(),
				header.getServerValue() instanceof NotToEscapePattern
						? header.getServerValue()
						: MapConverter.getTestSideValues(header.getServerValue())));
	}

	private void processHeaderElement(String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			this.blockBuilder.addLineWithEnding("assertThat(response.getHeaderString(\""
					+ property + "\"))." + createHeaderComparison(
							((NotToEscapePattern) value).getServerValue()));
		}
		else if (value instanceof Number) {
			this.blockBuilder.addLineWithEnding("assertThat(response.getHeaderString(\""
					+ property + "\")).isEqualTo(" + value + ")");
		}
		else if (value instanceof ExecutionProperty) {
			this.blockBuilder.addLineWithEnding(((ExecutionProperty) value)
					.insertValue("response.getHeaderString(\"" + property + "\")"));
		}
		else {
			this.blockBuilder.addLine("assertThat(response.getHeaderString(\"" + property
					+ "\"))." + createHeaderComparison(value));
		}
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getHeaders() != null;
	}

}

class JaxRsResponseCookiesThen implements Then, MockMvcAcceptor, CookieElementProcessor {

	private final BlockBuilder blockBuilder;

	JaxRsResponseCookiesThen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		processCookies(metadata);
		return this;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public String cookieKey(String key) {
		return "response.getCookies().get((\"" + key + "\")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		return response.getCookies() != null;
	}

}

interface CookieElementProcessor extends HeaderOrCookieComparisonBuilder {

	default void processCookies(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		Cookies cookies = response.getCookies();
		cookies.executeForEachCookie(cookie -> processCookieElement(cookie.getKey(),
				cookie.getServerValue() instanceof NotToEscapePattern
						? cookie.getServerValue()
						: MapConverter.getTestSideValues(cookie.getServerValue())));
		blockBuilder().addEndingIfNotPresent();
	}

	BlockBuilder blockBuilder();

	default void processCookieElement(String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			verifyCookieNotNull(property);
			blockBuilder().addIndented("assertThat(" + cookieKey(property) + ")."
					+ createMatchesMethod(((NotToEscapePattern) value).getServerValue()
							.pattern().replace("\\", "\\\\")));
		}
		else if (value instanceof String || value instanceof Pattern) {
			verifyCookieNotNull(property);
			blockBuilder().addIndented("assertThat(" + cookieKey(property) + ")."
					+ createHeaderComparison(value));
		}
		else if (value instanceof Number) {
			verifyCookieNotNull(property);
			blockBuilder().addIndented(
					"assertThat(" + cookieKey(property) + ").isEqualTo(" + value + ")");
		}
		else if (value instanceof ExecutionProperty) {
			verifyCookieNotNull(property);
			blockBuilder().addIndented(
					((ExecutionProperty) value).insertValue(cookieKey(property)));

		}
		else {
			// fallback
			processCookieElement(property, value.toString());
		}
	}

	default void verifyCookieNotNull(String key) {
		blockBuilder().addIndented("assertThat(" + cookieKey(key) + ").isNotNull()")
				.addEndingIfNotPresent();
	}

	String cookieKey(String key);

}

class JaxRsUrlPathWhen implements When, JaxRsAcceptor, QueryParamsResolver {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	JaxRsUrlPathWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendUrlPathAndQueryParameters(metadata.getContract().getRequest());
		return this;
	}

	private void appendUrlPathAndQueryParameters(Request request) {
		if (request.getUrl() != null) {
			this.blockBuilder.addIndented(".path(" + concreteUrl(request.getUrl()) + ")");
			appendQueryParams(request.getUrl().getQueryParameters());
		}
		else if (request.getUrlPath() != null) {
			this.blockBuilder
					.addIndented(".path(" + concreteUrl(request.getUrlPath()) + ")");
			appendQueryParams(request.getUrlPath().getQueryParameters());
		}
	}

	private String concreteUrl(DslProperty url) {
		Object testSideUrl = MapConverter.getTestSideValues(url);
		if (!(testSideUrl instanceof ExecutionProperty)) {
			return '"' + testSideUrl.toString() + '"';
		}
		return testSideUrl.toString();
	}

	private void appendQueryParams(QueryParameters queryParameters) {
		if (queryParameters == null || queryParameters.getParameters().isEmpty()) {
			return;
		}
		queryParameters.getParameters().stream().filter(this::allowedQueryParameter)
				.forEach(param -> this.blockBuilder.addIndented(".queryParam(\""
						+ param.getName() + "\", \"" + resolveParamValue(param) + "\")"));
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	private boolean allowedQueryParameter(QueryParameter param) {
		return allowedQueryParameter(param.getServerValue());
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	private boolean allowedQueryParameter(MatchingStrategy matchingStrategy) {
		return matchingStrategy.getType() != MatchingStrategy.Type.ABSENT;
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	private boolean allowedQueryParameter(Object o) {
		if (o instanceof QueryParameter) {
			return allowedQueryParameter((QueryParameter) o);
		}
		else if (o instanceof MatchingStrategy) {
			return allowedQueryParameter((MatchingStrategy) o);
		}
		return true;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata);
	}

}

class JaxRsRequestWhen implements When, JaxRsAcceptor, QueryParamsResolver {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	JaxRsRequestWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendRequestWithRequiredResponseContentType(metadata.getContract().getRequest());
		return this;
	}

	void appendRequestWithRequiredResponseContentType(Request request) {
		String acceptHeader = getHeader(request, "Accept");
		if (StringUtils.hasText(acceptHeader)) {
			this.blockBuilder.addIndented(".request(\"" + acceptHeader + "\")");
		}
		else {
			this.blockBuilder.addIndented(".request()");
		}
	}

	private String getHeader(Request request, String name) {
		if (request.getHeaders() == null || request.getHeaders().getEntries() == null) {
			return "";
		}
		Header foundHeader = request.getHeaders().getEntries().stream()
				.filter(header -> name.equals(header.getName())).findFirst().orElse(null);
		if (foundHeader == null) {
			return "";
		}
		return foundHeader.getServerValue().toString();
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata);
	}

}

class JaxRsRequestHeadersWhen implements When {

	private final BlockBuilder blockBuilder;

	JaxRsRequestHeadersWhen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendHeaders(metadata.getContract().getRequest());
		return this;
	}

	private void appendHeaders(Request request) {
		if (request.getHeaders() == null) {
			return;
		}
		request.getHeaders().executeForEachHeader(header -> {
			if (headerOfAbsentType(header)) {
				return;
			}
			if ("Content-Type".equals(header.getName())
					|| "Accept".equals(header.getName())) {
				return;
			}
			this.blockBuilder.addIndented(".header(\"" + header.getName() + "\", "
					+ quotedAndEscaped(header.getServerValue()) + ")");
		});
	}

	private boolean headerOfAbsentType(Header header) {
		return header.getServerValue() instanceof MatchingStrategy
				&& ((MatchingStrategy) header.getServerValue())
						.getType() == MatchingStrategy.Type.ABSENT;
	}

	private String quotedAndEscaped(Object string) {
		return '"' + StringEscapeUtils.escapeJava(String.valueOf(string)) + '"';
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getHeaders() != null;
	}

}

class JaxRsRequestCookiesWhen implements When {

	private final BlockBuilder blockBuilder;

	JaxRsRequestCookiesWhen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendCookies(metadata.getContract().getRequest());
		return this;
	}

	private void appendCookies(Request request) {
		if (request.getCookies() == null) {
			return;
		}
		request.getCookies().executeForEachCookie(cookie -> {
			if (cookieOfAbsentType(cookie)) {
				return;
			}
			this.blockBuilder.addIndented(".cookie(\"" + cookie.getKey() + "\", "
					+ quotedAndEscaped(cookie.getServerValue()) + ")");
		});
	}

	private boolean cookieOfAbsentType(Cookie cookie) {
		return cookie.getServerValue() instanceof MatchingStrategy
				&& ((MatchingStrategy) cookie.getServerValue())
						.getType() == MatchingStrategy.Type.ABSENT;
	}

	private String quotedAndEscaped(Object string) {
		return '"' + StringEscapeUtils.escapeJava(String.valueOf(string)) + '"';
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getCookies() != null;
	}

}

interface JaxRsBodyParser extends BodyParser {

	BodyParser INSTANCE = new JaxRsBodyParser() {
	};

	default String responseAsString() {
		return "responseAsString";
	}

	default String byteArrayString() {
		return "response.readEntity(byte[].class)";
	}

}

class JaxRsRequestMethodWhen implements When, JaxRsBodyParser {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	JaxRsRequestMethodWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(metaData);
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendMethodAndBody(metadata);
		return this;
	}

	void appendMethodAndBody(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		ContentType type = metadata.getInputTestContentType();
		String method = request.getMethod().getServerValue().toString().toLowerCase();
		if (request.getBody() != null) {
			String contentType = type.getMimeType();
			Object body = request.getBody().getServerValue();
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
				value = "\"" + requestBodyAsString(metadata) + "\"";
			}
			this.blockBuilder.addIndented(".build(\"" + method.toUpperCase()
					+ "\", entity(" + value + ", \"" + contentType + "\"))");
		}
		else {
			this.blockBuilder.addIndented(".build(\"" + method.toUpperCase() + "\")");
		}
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}

class JaxRsRequestInvokerWhen implements When, JaxRsBodyParser {

	private final BlockBuilder blockBuilder;

	JaxRsRequestInvokerWhen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented(".invoke()").addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}

class MockMvcResponseWhen implements When, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	MockMvcResponseWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented("ResponseOptions response = given().spec(request)");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata);
	}

}

class ExplicitResponseWhen implements When, ExplicitAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	ExplicitResponseWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented("Response response = given().spec(request)");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData);
	}

}

class WebTestClientResponseWhen implements When, WebTestClientAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	WebTestClientResponseWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder
				.addIndented("WebTestClientResponse response = given().spec(request)");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData);
	}

}

class RestAssuredThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Then> thens = new LinkedList<>();

	RestAssuredThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.thens.addAll(Arrays.asList(new RestAssuredStatusCodeThen(this.blockBuilder),
				new RestAssuredHeadersThen(this.blockBuilder),
				new RestAssuredCookiesThen(this.blockBuilder),
				new GenericBodyThen(this.blockBuilder, generatedClassMetaData,
						RestAssuredBodyParser.INSTANCE)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "then:");
		bodyBlock(this.blockBuilder, this.thens, singleContractMetadata);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isHttp()
				&& this.generatedClassMetaData.configProperties
						.getTestMode() != TestMode.JAXRSCLIENT;
	}

}

class RestAssuredStatusCodeThen implements Then {

	private final BlockBuilder blockBuilder;

	RestAssuredStatusCodeThen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		this.blockBuilder
				.addIndented("assertThat(response.statusCode()).isEqualTo("
						+ response.getStatus().getServerValue() + ")")
				.addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}

class RestAssuredHeadersThen
		implements Then, MockMvcAcceptor, HeaderOrCookieComparisonBuilder {

	private final BlockBuilder blockBuilder;

	RestAssuredHeadersThen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		Headers headers = response.getHeaders();
		headers.executeForEachHeader(header -> processHeaderElement(header.getName(),
				header.getServerValue() instanceof NotToEscapePattern
						? header.getServerValue()
						: MapConverter.getTestSideValues(header.getServerValue())));
		this.blockBuilder.addEndingIfNotPresent();
		return this;
	}

	private void processHeaderElement(String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			this.blockBuilder.addIndented("assertThat(response.header(\"" + property
					+ "\"))." + createMatchesMethod(((NotToEscapePattern) value)
							.getServerValue().pattern().replace("\\", "\\\\")));
		}
		else if (value instanceof String || value instanceof Pattern) {
			this.blockBuilder.addIndented("assertThat(response.header(\"" + property
					+ "\"))." + createHeaderComparison(value));
		}
		else if (value instanceof Number) {
			this.blockBuilder.addIndented("assertThat(response.header(\"" + property
					+ "\")).isEqualTo(" + value + ")");
		}
		else if (value instanceof ExecutionProperty) {
			this.blockBuilder.addIndented(((ExecutionProperty) value)
					.insertValue("response.header(\"" + property + "\")"));

		}
		else {
			// fallback
			processHeaderElement(property, value.toString());
		}
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		return response.getHeaders() != null;
	}

}

interface HeaderOrCookieComparisonBuilder {

	default String createHeaderComparison(Object headerValue) {
		if (headerValue instanceof Pattern) {
			return createHeaderComparison((Pattern) headerValue);
		}
		String escapedHeader = convertUnicodeEscapesIfRequired(headerValue.toString());
		return "isEqualTo(\"" + escapedHeader + "\")";
	}

	default String createMatchesMethod(String pattern) {
		return "matches(\"" + pattern + "\")";
	}

	default String convertUnicodeEscapesIfRequired(String json) {
		String unescapedJson = StringEscapeUtils.unescapeJson(json);
		return escapeJava(unescapedJson);
	}

	default String createHeaderComparison(Pattern headerValue) {
		return createMatchesMethod(escapeJava(headerValue.pattern()));
	}

}

interface RestAssuredVerifier {

	Log log = LogFactory.getLog(RestAssuredVerifier.class);

	// TODO: Remove in next major
	String REST_ASSURED_2_0_CLASS = "com.jayway.restassured.RestAssured";

	ClassPresenceChecker checker = new ClassPresenceChecker();

	@Deprecated
	default boolean isRestAssured2Present() {
		boolean restAssured2Present = checker.isClassPresent(REST_ASSURED_2_0_CLASS);
		if (restAssured2Present) {
			log.warn(
					"Rest Assured 2 found on the classpath. Please upgrade to the latest version of Rest Assured");
		}
		return restAssured2Present;

	}

}

class MockMvcRestAssuredImports implements Imports, RestAssuredVerifier {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	@Deprecated
	private static final String[] REST_ASSURED_2_IMPORTS = {
			"com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification",
			"com.jayway.restassured.response.ResponseOptions" };

	private static final String[] REST_ASSURED_3_IMPORTS = {
			"io.restassured.module.mockmvc.specification.MockMvcRequestSpecification",
			"io.restassured.response.ResponseOptions" };

	MockMvcRestAssuredImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(
				isRestAssured2Present() ? REST_ASSURED_2_IMPORTS : REST_ASSURED_3_IMPORTS)
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

class ExplicitRestAssuredImports implements Imports, RestAssuredVerifier {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] REST_ASSURED_2_IMPORTS = {
			"com.jayway.restassured.specification.RequestSpecification",
			"com.jayway.restassured.response.Response" };

	private static final String[] REST_ASSURED_3_IMPORTS = {
			"io.restassured.specification.RequestSpecification",
			"io.restassured.response.Response" };

	ExplicitRestAssuredImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(
				isRestAssured2Present() ? REST_ASSURED_2_IMPORTS : REST_ASSURED_3_IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestMode() == TestMode.EXPLICIT
				&& this.generatedClassMetaData.isAnyHttp();
	}

}

class WebTestClientRestAssuredImports implements Imports, RestAssuredVerifier {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] REST_ASSURED_2_IMPORTS = {
			"com.jayway.restassured.module.webtestclient.specification.WebTestClientRequestSpecification",
			"com.jayway.restassured.module.webtestclient.response.WebTestClientResponse" };

	private static final String[] REST_ASSURED_3_IMPORTS = {
			"io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification",
			"io.restassured.module.webtestclient.response.WebTestClientResponse" };

	WebTestClientRestAssuredImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(
				isRestAssured2Present() ? REST_ASSURED_2_IMPORTS : REST_ASSURED_3_IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestMode() == TestMode.WEBTESTCLIENT
				&& this.generatedClassMetaData.isAnyHttp();
	}

}

class MockMvcRestAssuredStaticImports implements Imports, RestAssuredVerifier {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] REST_ASSURED_2_IMPORTS = {
			"com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*" };

	private static final String[] REST_ASSURED_3_IMPORTS = {
			"io.restassured.module.mockmvc.RestAssuredMockMvc.*" };

	MockMvcRestAssuredStaticImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(
				isRestAssured2Present() ? REST_ASSURED_2_IMPORTS : REST_ASSURED_3_IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import static " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestMode() == TestMode.MOCKMVC;
	}

}

class ExplicitRestAssuredStaticImports implements Imports, RestAssuredVerifier {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] REST_ASSURED_2_IMPORTS = {
			"com.jayway.restassured.RestAssured.*" };

	private static final String[] REST_ASSURED_3_IMPORTS = {
			"io.restassured.RestAssured.*" };

	ExplicitRestAssuredStaticImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(
				isRestAssured2Present() ? REST_ASSURED_2_IMPORTS : REST_ASSURED_3_IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import static " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestMode() == TestMode.EXPLICIT
				&& this.generatedClassMetaData.isAnyHttp();
	}

}

class WebTestClientRestAssured3StaticImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = {
			"io.restassured.module.webtestclient.RestAssuredWebTestClient.*" };

	WebTestClientRestAssured3StaticImports(BlockBuilder blockBuilder,
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
				.getTestMode() == TestMode.WEBTESTCLIENT
				&& this.generatedClassMetaData.isAnyHttp();
	}

}

class RestAssuredCookiesThen implements Then, MockMvcAcceptor, CookieElementProcessor {

	private final BlockBuilder blockBuilder;

	RestAssuredCookiesThen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		processCookies(metadata);
		return this;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public String cookieKey(String key) {
		return "response.getCookie(\"" + key + "\")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		return response.getCookies() != null;
	}

}

class GenericBodyThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	private final TemplateProcessor templateProcessor;

	private final List<Then> thens = new LinkedList<>();

	GenericBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
		this.templateProcessor = new HandlebarsTemplateProcessor();
		this.thens.addAll(Arrays.asList(
				new GenericBinaryBodyThen(blockBuilder, metaData, this.bodyParser),
				new GenericTextBodyThen(blockBuilder, metaData, this.bodyParser),
				new GenericJsonBodyThen(blockBuilder, metaData, this.bodyParser),
				new GenericXmlBodyThen(blockBuilder, this.bodyParser)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		endBodyBlock(this.blockBuilder);
		this.blockBuilder.addEmptyLine();
		startBodyBlock(this.blockBuilder, "and:");
		Request request = metadata.getContract().getRequest();
		this.thens.stream().filter(then -> then.accept(metadata))
				.forEach(then -> then.apply(metadata));
		String newBody = this.templateProcessor.transform(request,
				this.blockBuilder.toString());
		this.blockBuilder.updateContents(newBody);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBody() != null;
	}

}

class GenericTextBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final BodyAssertionLineCreator bodyAssertionLineCreator;

	private final BodyParser bodyParser;

	GenericTextBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder,
				metaData, this.bodyParser.byteArrayString());
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Object convertedResponseBody = this.bodyParser.convertResponseBody(metadata);
		convertedResponseBody = StringEscapeUtils
				.escapeJava(convertedResponseBody.toString());
		simpleTextResponseBodyCheck(metadata, convertedResponseBody);
		return this;
	}

	private void simpleTextResponseBodyCheck(SingleContractMetadata metadata,
			Object convertedResponseBody) {
		this.blockBuilder.addLineWithEnding(
				getSimpleResponseBodyString(this.bodyParser.responseAsString()));
		this.bodyAssertionLineCreator.appendBodyAssertionLine(metadata, "",
				convertedResponseBody);
		this.blockBuilder.addEndingIfNotPresent();
	}

	private String getSimpleResponseBodyString(String responseString) {
		return "String responseBody = " + responseString
				+ this.blockBuilder.getLineEnding();
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		ContentType outputTestContentType = metadata.getOutputTestContentType();
		return outputTestContentType != JSON && outputTestContentType != XML
				&& metadata.getContract().getResponse().getBody() != null
				&& !(metadata.getContract().getResponse().getBody()
						.getServerValue() instanceof FromFileProperty);
	}

}

class GenericJsonBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final BodyParser bodyParser;

	private final BodyAssertionLineCreator bodyAssertionLineCreator;

	private final TemplateProcessor templateProcessor;

	private final ContractTemplate contractTemplate;

	GenericJsonBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder,
				metaData, this.bodyParser.byteArrayString());
		this.generatedClassMetaData = metaData;
		this.templateProcessor = new HandlebarsTemplateProcessor();
		this.contractTemplate = new HandlebarsTemplateProcessor();
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		BodyMatchers bodyMatchers = metadata.getContract().getResponse()
				.getBodyMatchers();
		Object convertedResponseBody = this.bodyParser.convertResponseBody(metadata);
		ContentType contentType = metadata.getOutputTestContentType();
		if (TEXT != contentType && FORM != contentType && DEFINED != contentType) {
			boolean dontParseStrings = contentType == JSON
					&& convertedResponseBody instanceof Map;
			Function parsingClosure = dontParseStrings ? Function.identity()
					: MapConverter.JSON_PARSING_FUNCTION;
			convertedResponseBody = MapConverter.getTestSideValues(convertedResponseBody,
					parsingClosure);
		}
		else {
			convertedResponseBody = StringEscapeUtils
					.escapeJava(convertedResponseBody.toString());
		}
		addJsonBodyVerification(metadata, convertedResponseBody, bodyMatchers);
		return this;
	}

	private void addJsonBodyVerification(SingleContractMetadata contractMetadata,
			Object responseBody, BodyMatchers bodyMatchers) {
		JsonBodyVerificationBuilder jsonBodyVerificationBuilder = new JsonBodyVerificationBuilder(
				this.generatedClassMetaData.configProperties, this.templateProcessor,
				this.contractTemplate, contractMetadata.getContract(),
				Optional.of(this.blockBuilder.getLineEnding()));
		Object convertedResponseBody = jsonBodyVerificationBuilder
				.addJsonResponseBodyCheck(this.blockBuilder, responseBody, bodyMatchers,
						this.bodyParser.responseAsString(), true);
		if (!(convertedResponseBody instanceof Map
				|| convertedResponseBody instanceof List)) {
			simpleTextResponseBodyCheck(contractMetadata, convertedResponseBody);
		}
		processBodyElement("", "", convertedResponseBody);
	}

	private void processBodyElement(String oldProp, String property, Object value) {
		String propDiff = subtract(property, oldProp);
		String prop = wrappedWithBracketsForDottedProp(propDiff);
		String mergedProp = StringUtils.hasText(property) ? oldProp + "." + prop : "";
		if (value instanceof ExecutionProperty) {
			processBodyElement(mergedProp, (ExecutionProperty) value);
		}
		else if (value instanceof Map.Entry) {
			processBodyElement(mergedProp, (Map.Entry) value);
		}
		else if (value instanceof Map) {
			processBodyElement(mergedProp, (Map) value);
		}
		else if (value instanceof List) {
			processBodyElement(mergedProp, (List) value);
		}
	}

	private void processBodyElement(String property, ExecutionProperty exec) {
		this.blockBuilder.addLineWithEnding(
				exec.insertValue("parsedJson.read(\"$" + property + "\")"));
	}

	private void processBodyElement(String property, Map.Entry entry) {
		processBodyElement(property, getMapKeyReferenceString(property, entry),
				entry.getValue());
	}

	private void processBodyElement(String property, Map map) {
		map.entrySet().forEach(o -> processBodyElement(property, (Map.Entry) o));
	}

	private void processBodyElement(String property, List list) {
		Iterator iterator = list.iterator();
		int index = -1;
		while (iterator.hasNext()) {
			Object listElement = iterator.next();
			index = index + 1;
			String prop = getPropertyInListString(property, index);
			processBodyElement(property, prop, listElement);
		}
	}

	private String getPropertyInListString(String property, Integer listIndex) {
		return property + "[" + listIndex + "]";
	}

	private String getMapKeyReferenceString(String property, Map.Entry entry) {
		return provideProperJsonPathNotation(property) + "." + entry.getKey();
	}

	private String provideProperJsonPathNotation(String property) {
		return property.replaceAll("(get\\(\\\\\")(.*)(\\\\\"\\))", "$2");
	}

	private String wrappedWithBracketsForDottedProp(String key) {
		String remindingKey = trailingKey(key);
		if (remindingKey.contains(".")) {
			return "['" + remindingKey + "']";
		}
		return remindingKey;
	}

	private String trailingKey(String key) {
		if (key.startsWith(".")) {
			return key.substring(1);
		}
		return key;
	}

	private String subtract(String one, String two) {
		return one.replaceAll(two, "");
	}

	private void simpleTextResponseBodyCheck(SingleContractMetadata metadata,
			Object convertedResponseBody) {
		this.blockBuilder.addLineWithEnding(
				getSimpleResponseBodyString(this.bodyParser.responseAsString()));
		this.bodyAssertionLineCreator.appendBodyAssertionLine(metadata, "",
				convertedResponseBody);
		this.blockBuilder.addEndingIfNotPresent();
	}

	private String getSimpleResponseBodyString(String responseString) {
		return "String responseBody = " + responseString
				+ this.blockBuilder.getLineEnding();
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		ContentType outputTestContentType = metadata.getOutputTestContentType();
		return JSON == outputTestContentType || DEFINED == outputTestContentType;
	}

}

class GenericXmlBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	GenericXmlBodyThen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		BodyMatchers bodyMatchers = metadata.getContract().getResponse()
				.getBodyMatchers();
		Object convertedResponseBody = this.bodyParser.convertResponseBody(metadata);
		XmlBodyVerificationBuilder xmlBodyVerificationBuilder = new XmlBodyVerificationBuilder(
				metadata.getContract(), Optional.of(this.blockBuilder.getLineEnding()));
		xmlBodyVerificationBuilder.addXmlResponseBodyCheck(this.blockBuilder,
				convertedResponseBody, bodyMatchers, this.bodyParser.responseAsString(),
				true);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		ContentType outputTestContentType = metadata.getOutputTestContentType();
		return XML == outputTestContentType;
	}

}

class GenericBinaryBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final BodyAssertionLineCreator bodyAssertionLineCreator;

	GenericBinaryBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder,
				metaData, bodyParser.byteArrayString());
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Object responseBody = metadata.getContract().getResponse().getBody()
				.getServerValue();
		byteResponseBodyCheck(metadata, (FromFileProperty) responseBody);
		return this;
	}

	private void byteResponseBodyCheck(SingleContractMetadata metadata,
			FromFileProperty convertedResponseBody) {
		this.bodyAssertionLineCreator.appendBodyAssertionLine(metadata, "",
				convertedResponseBody);
		this.blockBuilder.addEndingIfNotPresent();
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Object responseBody = metadata.getContract().getResponse().getBody()
				.getServerValue();
		if (!(responseBody instanceof FromFileProperty)) {
			return false;
		}
		return ((FromFileProperty) responseBody).isByte();
	}

}

interface RestAssuredBodyParser extends BodyParser {

	BodyParser INSTANCE = new RestAssuredBodyParser() {
	};

	@Override
	default String responseAsString() {
		return "response.getBody().asString()";
	}

	@Override
	default String byteArrayString() {
		return "response.getBody().asByteArray()";
	}

}

interface BodyParser {

	String byteArrayString();

	default Object convertResponseBody(SingleContractMetadata metadata) {
		Object responseBody = metadata.getContract().getResponse().getBody();
		ContentType contentType = metadata.getOutputTestContentType();
		if (responseBody instanceof FromFileProperty) {
			responseBody = ((FromFileProperty) responseBody).asString();
		}
		else if (responseBody instanceof GString) {
			responseBody = extractValue((GString) responseBody, contentType,
					o -> o instanceof DslProperty ? ((DslProperty) o).getServerValue()
							: o);
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

	default String convertUnicodeEscapesIfRequired(String json) {
		String unescapedJson = StringEscapeUtils.unescapeEcmaScript(json);
		return escapeJava(unescapedJson);
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

	/**
	 * Converts the passed body into ints server side representation. All
	 * {@link DslProperty} will return their server side values
	 */
	default Object extractServerValueFromBody(ContentType contentType, Object bodyValue) {
		if (bodyValue instanceof GString) {
			return extractValue((GString) bodyValue, contentType,
					ContentUtils.GET_TEST_SIDE);
		}
		boolean dontParseStrings = contentType == JSON && bodyValue instanceof Map;
		Closure parsingClosure = dontParseStrings ? Closure.IDENTITY
				: MapConverter.JSON_PARSING_CLOSURE;
		return MapConverter.transformValues(bodyValue, ContentUtils.GET_TEST_SIDE,
				parsingClosure);
	}

}

class BodyAssertionLineCreator {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	private final String byteArrayString;

	BodyAssertionLineCreator(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			String byteArrayString) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(metaData);
		this.byteArrayString = byteArrayString;
	}

	void appendBodyAssertionLine(SingleContractMetadata metadata, String property,
			Object value) {
		if (value instanceof String && ((String) value).startsWith("$")) {
			String newValue = stripFirstChar((String) value).replaceAll("\\$value",
					"responseBody" + property);
			this.blockBuilder.addLineWithEnding(newValue);
		}
		else {
			this.blockBuilder.addLineWithEnding(
					getResponseBodyPropertyComparisonString(metadata, property, value));
		}
	}

	/**
	 * Builds the code that for the given {@code property} will compare it to the given
	 * Object {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(
			SingleContractMetadata singleContractMetadata, String property,
			Object value) {
		if (value instanceof FromFileProperty) {
			return getResponseBodyPropertyComparisonString(singleContractMetadata,
					property, (FromFileProperty) value);
		}
		else if (value instanceof Pattern) {
			return getResponseBodyPropertyComparisonString(property, (Pattern) value);
		}
		else if (value instanceof ExecutionProperty) {
			return getResponseBodyPropertyComparisonString(property,
					(ExecutionProperty) value);
		}
		else if (value instanceof DslProperty) {
			return getResponseBodyPropertyComparisonString(singleContractMetadata,
					property, ((DslProperty) value).getServerValue());
		}
		return getResponseBodyPropertyComparisonString(property, value.toString());
	}

	/**
	 * Builds the code that for the given {@code property} will compare it to the given
	 * byte[] {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(
			SingleContractMetadata singleContractMetadata, String property,
			FromFileProperty value) {
		if (value.isByte()) {
			return "assertThat(" + this.byteArrayString + ").isEqualTo("
					+ this.bodyReader.readBytesFromFileString(singleContractMetadata,
							value, CommunicationType.RESPONSE)
					+ ")";
		}
		return getResponseBodyPropertyComparisonString(property, value.asString());
	}

	/**
	 * Builds the code that for the given {@code property} will compare it to the given
	 * String {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(String property,
			String value) {
		return "assertThat(responseBody" + property + ").isEqualTo(\"" + value + "\")";
	}

	/**
	 * Builds the code that for the given {@code property} will match it to the given
	 * regular expression {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(String property,
			Pattern value) {
		return "assertThat(responseBody" + property + ")." + createBodyComparison(value);
	}

	private String createBodyComparison(Pattern bodyValue) {
		String patternAsString = bodyValue.pattern();
		return createMatchesMethod(
				RegexpBuilders.buildGStringRegexpForTestSide(patternAsString));
	}

	private String createMatchesMethod(String pattern) {
		return "matches(\"" + pattern + "\")";
	}

	/**
	 * Builds the code that for the given {@code property} will match it to the given
	 * {@link ExecutionProperty} value
	 */
	private String getResponseBodyPropertyComparisonString(String property,
			ExecutionProperty value) {
		return value.insertValue("responseBody" + property);
	}

	private String stripFirstChar(String s) {
		return s.substring(1);
	}

}