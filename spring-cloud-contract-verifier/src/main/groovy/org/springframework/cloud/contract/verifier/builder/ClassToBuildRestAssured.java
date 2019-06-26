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
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.Cookie;
import org.springframework.cloud.contract.spec.internal.Cookies;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern;
import org.springframework.cloud.contract.spec.internal.OptionalProperty;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.spec.internal.QueryParameter;
import org.springframework.cloud.contract.spec.internal.QueryParameters;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.config.TestMode;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.MapConverter;
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

class MockMvcBodyGiven implements Given {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	MockMvcBodyGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
		this.bodyParser = bodyParser;
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
			body = this.bodyParser.requestBodyAsString(metadata);
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
			value = this.bodyParser.quotedEscapedLongText(escaped);
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
		Iterator<Cookie> iterator = request.getCookies().getEntries().iterator();
		while (iterator.hasNext()) {
			Cookie cookie = iterator.next();
			if (ofAbsentType(cookie)) {
				return;
			}
			if (iterator.hasNext()) {
				this.blockBuilder.addLine(string(cookie));
			}
			else {
				this.blockBuilder.addIndented(string(cookie));
			}
		}
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

class JavaRestAssuredGiven extends RestAssuredGiven {

	private final GeneratedClassMetaData metaData;

	JavaRestAssuredGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, RestAssuredBodyParser.INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() != TestFramework.SPOCK;
	}

}

class SpockRestAssuredGiven extends RestAssuredGiven {

	private final GeneratedClassMetaData metaData;

	SpockRestAssuredGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, SpockRestAssuredBodyParser.INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
	}

}

class RestAssuredGiven implements Given, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Given> requestGivens = new LinkedList<>();

	private final List<Given> bodyGivens = new LinkedList<>();

	RestAssuredGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.requestGivens.addAll(Arrays.asList(
				new MockMvcRequestGiven(blockBuilder, generatedClassMetaData),
				new SpockMockMvcRequestGiven(blockBuilder, generatedClassMetaData),
				new ExplicitRequestGiven(blockBuilder, generatedClassMetaData),
				new WebTestClientRequestGiven(blockBuilder, generatedClassMetaData)));
		this.bodyGivens.addAll(Arrays.asList(new MockMvcHeadersGiven(blockBuilder),
				new MockMvcCookiesGiven(blockBuilder),
				new MockMvcBodyGiven(blockBuilder, generatedClassMetaData, bodyParser),
				new MockMvcMultipartGiven(blockBuilder, generatedClassMetaData,
						bodyParser),
				new SpockMockMvcMultipartGiven(blockBuilder, generatedClassMetaData,
						bodyParser)));
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

class SpockMockMvcRequestGiven implements Given, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	SpockMockMvcRequestGiven(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented("def request = given()");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata)
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.SPOCK;
	}

}

class JaxRsGiven implements Given, JaxRsAcceptor {

	private final GeneratedClassMetaData generatedClassMetaData;

	JaxRsGiven(GeneratedClassMetaData metaData) {
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
		Iterator<Header> iterator = headers.getEntries().iterator();
		while (iterator.hasNext()) {
			Header header = iterator.next();
			if (ofAbsentType(header)) {
				return;
			}
			if (iterator.hasNext()) {
				bb.addLine(string(header));
			}
			else {
				bb.addIndented(string(header));
			}
		}
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

	private final GeneratedClassMetaData generatedClassMetaData;

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	MockMvcMultipartGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
		this.bodyParser = bodyParser;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		getMultipartParameters(metadata).entrySet().forEach(entry -> this.blockBuilder
				.addLine(getMultipartParameterLine(metadata, entry)));
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
		return ".param(" + this.bodyParser.quotedShortText(parameter.getKey()) + ", "
				+ this.bodyParser.quotedShortText(parameter.getValue()) + ")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getMultipart() != null
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() != TestFramework.SPOCK;
	}

}

class SpockMockMvcMultipartGiven implements Given {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	SpockMockMvcMultipartGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
		this.bodyParser = bodyParser;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		getMultipartParameters(metadata).entrySet().forEach(entry -> this.blockBuilder
				.addLine(getMultipartParameterLine(metadata, entry)));
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
		return ContentUtils.getGroovyMultipartFileParameterContent(propertyName,
				propertyValue,
				fileProp -> this.bodyReader.readBytesFromFileString(metadata, fileProp,
						CommunicationType.REQUEST));
	}

	private String getParameterString(Map.Entry<String, Object> parameter) {
		return ".param(" + this.bodyParser.quotedShortText(parameter.getKey()) + ", "
				+ this.bodyParser.quotedShortText(parameter.getValue()) + ")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getMultipart() != null
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.SPOCK;
	}

}

class MockMvcQueryParamsWhen implements When, MockMvcAcceptor, QueryParamsResolver {

	private static final String QUERY_PARAM_METHOD = "queryParam";

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	MockMvcQueryParamsWhen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		Url url = getUrl(request);
		addQueryParameters(url);
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
		buildUrl.getQueryParameters().getParameters().stream()
				.filter(this::allowedQueryParameter).forEach(this::addQueryParameter);
	}

	private boolean allowedQueryParameter(Object o) {
		if (o instanceof QueryParameter) {
			return allowedQueryParameter(((QueryParameter) o).getServerValue());
		}
		else if (o instanceof MatchingStrategy) {
			return !MatchingStrategy.Type.ABSENT.equals(((MatchingStrategy) o).getType());
		}
		return true;
	}

	private void addQueryParameter(QueryParameter queryParam) {
		this.blockBuilder.addLine("." + QUERY_PARAM_METHOD + "("
				+ this.bodyParser.quotedLongText(queryParam.getName()) + ","
				+ this.bodyParser.quotedLongText(resolveParamValue(queryParam)) + ")");
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		Url url = getUrl(request);
		return url.getQueryParameters() != null;
	}

}

class MockMvcUrlWhen implements When, MockMvcAcceptor, QueryParamsResolver {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	MockMvcUrlWhen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		Url url = getUrl(request);
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

	private void addUrl(Url buildUrl, Request request) {
		Object testSideUrl = MapConverter.getTestSideValues(buildUrl);
		String method = request.getMethod().getServerValue().toString().toLowerCase();
		String url = testSideUrl.toString();
		if (!(testSideUrl instanceof ExecutionProperty)) {
			url = this.bodyParser.quotedShortText(testSideUrl.toString());
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

class JavaRestAssuredWhen extends RestAssuredWhen {

	private final GeneratedClassMetaData metaData;

	JavaRestAssuredWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, RestAssuredBodyParser.INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() != TestFramework.SPOCK;
	}

}

class SpockRestAssuredWhen extends RestAssuredWhen {

	private final GeneratedClassMetaData metaData;

	SpockRestAssuredWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, RestAssuredBodyParser.INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
	}

}

class RestAssuredWhen implements When, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> responseWhens = new LinkedList<>();

	private final List<When> whens = new LinkedList<>();

	RestAssuredWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.responseWhens.addAll(Arrays.asList(
				new MockMvcResponseWhen(blockBuilder, this.generatedClassMetaData),
				new SpockMockMvcResponseWhen(blockBuilder, this.generatedClassMetaData),
				new ExplicitResponseWhen(blockBuilder, this.generatedClassMetaData),
				new WebTestClientResponseWhen(blockBuilder,
						this.generatedClassMetaData)));
		this.whens.addAll(
				Arrays.asList(new MockMvcQueryParamsWhen(this.blockBuilder, bodyParser),
						new MockMvcAsyncWhen(this.blockBuilder,
								this.generatedClassMetaData),
						new MockMvcUrlWhen(this.blockBuilder, bodyParser)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "when:");
		addResponseWhenLine(singleContractMetadata);
		indentedBodyBlock(this.blockBuilder, this.whens, singleContractMetadata);
		this.blockBuilder.addEmptyLine();
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

class JavaJaxRsWhen extends JaxRsWhen {

	private final GeneratedClassMetaData metaData;

	JavaJaxRsWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, JaxRsBodyParser.INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() != TestFramework.SPOCK;
	}

}

class SpockJaxRsWhen extends JaxRsWhen {

	private final GeneratedClassMetaData metaData;

	SpockJaxRsWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, SpockJaxRsBodyParser.INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
	}

}

class JaxRsWhen implements When, BodyMethodVisitor, JaxRsAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> whens = new LinkedList<>();

	JaxRsWhen(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData,
			BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.whens.addAll(Arrays.asList(
				new JaxRsUrlPathWhen(this.blockBuilder, this.generatedClassMetaData),
				new JaxRsRequestWhen(this.blockBuilder, this.generatedClassMetaData),
				new JaxRsRequestHeadersWhen(this.blockBuilder, bodyParser),
				new JaxRsRequestCookiesWhen(this.blockBuilder, bodyParser),
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

class JavaJaxRsThen extends JaxRsThen {

	private final GeneratedClassMetaData metaData;

	JavaJaxRsThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, JaxRsBodyParser.INSTANCE,
				ComparisonBuilder.JAVA_HTTP_INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() != TestFramework.SPOCK;
	}

}

class SpockJaxRsThen extends JaxRsThen {

	private final GeneratedClassMetaData metaData;

	SpockJaxRsThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, SpockJaxRsBodyParser.INSTANCE,
				GroovyComparisonBuilder.JAXRS_HTTP_INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
	}

}

class JaxRsThen implements Then, BodyMethodVisitor, JaxRsAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Then> thens = new LinkedList<>();

	JaxRsThen(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData,
			BodyParser bodyParser, ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.thens.addAll(Arrays.asList(
				new JaxRsStatusCodeThen(this.blockBuilder, comparisonBuilder),
				new JaxRsResponseHeadersThen(this.blockBuilder, generatedClassMetaData,
						comparisonBuilder),
				new JaxRsResponseCookiesThen(this.blockBuilder, generatedClassMetaData,
						comparisonBuilder),
				new GenericHttpBodyThen(this.blockBuilder, generatedClassMetaData,
						bodyParser, comparisonBuilder)));
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

	private final ComparisonBuilder comparisonBuilder;

	JaxRsStatusCodeThen(BlockBuilder blockBuilder, ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		this.blockBuilder
				.addIndented(this.comparisonBuilder.assertThat("response.getStatus()",
						response.getStatus().getServerValue()))
				.addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}

class JaxRsResponseHeadersThen implements Then {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	JaxRsResponseHeadersThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		validateResponseHeadersBlock(metadata);
		return this;
	}

	private void validateResponseHeadersBlock(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		Headers headers = response.getHeaders();
		Iterator<Header> iterator = headers.getEntries().iterator();
		while (iterator.hasNext()) {
			Header header = iterator.next();
			String text = processHeaderElement(header.getName(),
					header.getServerValue() instanceof NotToEscapePattern
							? header.getServerValue()
							: MapConverter.getTestSideValues(header.getServerValue()));
			if (iterator.hasNext()) {
				this.blockBuilder.addLineWithEnding(text);
			}
			else {
				this.blockBuilder.addIndented(text);
			}
		}
	}

	private String processHeaderElement(String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			return this.comparisonBuilder
					.assertThat("response.getHeaderString(\"" + property + "\")")
					+ this.comparisonBuilder.createComparison(
							((NotToEscapePattern) value).getServerValue());
		}
		else if (value instanceof Number) {
			return this.comparisonBuilder
					.assertThat("response.getHeaderString(\"" + property + "\")", value);
		}
		else if (value instanceof ExecutionProperty) {
			return ((ExecutionProperty) value)
					.insertValue("response.getHeaderString(\"" + property + "\")");
		}
		else {
			return this.comparisonBuilder
					.assertThat("response.getHeaderString(\"" + property + "\")")
					+ this.comparisonBuilder.createComparison(value);
		}
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getHeaders() != null;
	}

}

class JaxRsResponseCookiesThen implements Then, MockMvcAcceptor, CookieElementProcessor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	JaxRsResponseCookiesThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		processCookies(metadata);
		return this;
	}

	@Override
	public ComparisonBuilder comparisonBuilder() {
		return this.comparisonBuilder;
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

interface CookieElementProcessor {

	ComparisonBuilder comparisonBuilder();

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
			blockBuilder().addIndented(comparisonBuilder().assertThat(cookieKey(property))
					+ comparisonBuilder().matches(((NotToEscapePattern) value)
							.getServerValue().pattern().replace("\\", "\\\\")));
		}
		else if (value instanceof String || value instanceof Pattern) {
			verifyCookieNotNull(property);
			blockBuilder().addIndented(
					comparisonBuilder().assertThat(cookieKey(property), value));
		}
		else if (value instanceof Number) {
			verifyCookieNotNull(property);
			blockBuilder().addIndented(
					comparisonBuilder().assertThat(cookieKey(property), value));
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
		blockBuilder().addLineWithEnding(
				comparisonBuilder().assertThatIsNotNull(cookieKey(key)));
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

	private final BodyParser bodyParser;

	JaxRsRequestHeadersWhen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendHeaders(metadata.getContract().getRequest());
		return this;
	}

	private void appendHeaders(Request request) {
		request.getHeaders().executeForEachHeader(header -> {
			if (headerOfAbsentType(header)) {
				return;
			}
			if ("Content-Type".equals(header.getName())
					|| "Accept".equals(header.getName())) {
				return;
			}
			this.blockBuilder.addIndented(".header(\"" + header.getName() + "\", "
					+ this.bodyParser.quotedLongText(header.getServerValue()) + ")");
		});
	}

	private boolean headerOfAbsentType(Header header) {
		return header.getServerValue() instanceof MatchingStrategy
				&& ((MatchingStrategy) header.getServerValue())
						.getType() == MatchingStrategy.Type.ABSENT;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getHeaders() != null && !metadata
				.getContract().getRequest().getHeaders().getEntries().isEmpty();
	}

}

class JaxRsRequestCookiesWhen implements When {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	JaxRsRequestCookiesWhen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
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
		Iterator<Cookie> iterator = request.getCookies().getEntries().iterator();
		while (iterator.hasNext()) {
			Cookie cookie = iterator.next();
			if (cookieOfAbsentType(cookie)) {
				return;
			}
			String value = ".cookie(\"" + cookie.getKey() + "\", "
					+ this.bodyParser.quotedLongText(cookie.getServerValue()) + ")";
			if (iterator.hasNext()) {
				this.blockBuilder.addLine(value);
			}
			else {
				this.blockBuilder.addIndented(value);
			}
		}
	}

	private boolean cookieOfAbsentType(Cookie cookie) {
		return cookie.getServerValue() instanceof MatchingStrategy
				&& ((MatchingStrategy) cookie.getServerValue())
						.getType() == MatchingStrategy.Type.ABSENT;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getCookies() != null && !metadata
				.getContract().getRequest().getCookies().getEntries().isEmpty();
	}

}

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

interface SpockJaxRsBodyParser extends JaxRsBodyParser, GroovyBodyParser {

	SpockRestAssuredBodyParser INSTANCE = HandlebarsTemplateProcessor::new;

	@Override
	default String byteArrayString() {
		return "response.readEntity(byte[])";
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

class SpockMockMvcResponseWhen implements When, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	SpockMockMvcResponseWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented("def response = given().spec(request)");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata)
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.SPOCK;
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

class JavaRestAssuredThen extends RestAssuredThen {

	private final GeneratedClassMetaData metaData;

	JavaRestAssuredThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, RestAssuredBodyParser.INSTANCE,
				ComparisonBuilder.JAVA_HTTP_INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() != TestFramework.SPOCK;
	}

}

class SpockRestAssuredThen extends RestAssuredThen {

	private final GeneratedClassMetaData metaData;

	SpockRestAssuredThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, SpockRestAssuredBodyParser.INSTANCE,
				GroovyComparisonBuilder.SPOCK_HTTP_INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
	}

}

class RestAssuredThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Then> thens = new LinkedList<>();

	RestAssuredThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.thens.addAll(Arrays.asList(
				new RestAssuredStatusCodeThen(this.blockBuilder, comparisonBuilder),
				new RestAssuredHeadersThen(this.blockBuilder, comparisonBuilder),
				new RestAssuredCookiesThen(this.blockBuilder, comparisonBuilder),
				new GenericHttpBodyThen(this.blockBuilder, generatedClassMetaData,
						bodyParser, comparisonBuilder)));
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

	private final ComparisonBuilder comparisonBuilder;

	RestAssuredStatusCodeThen(BlockBuilder blockBuilder,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		this.blockBuilder
				.addIndented(this.comparisonBuilder.assertThat("response.statusCode()",
						response.getStatus().getServerValue()))
				.addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}

class RestAssuredHeadersThen implements Then, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final ComparisonBuilder comparisonBuilder;

	RestAssuredHeadersThen(BlockBuilder blockBuilder,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		Headers headers = response.getHeaders();
		Iterator<Header> iterator = headers.getEntries().iterator();
		while (iterator.hasNext()) {
			Header header = iterator.next();
			String text = processHeaderElement(header.getName(),
					header.getServerValue() instanceof NotToEscapePattern
							? header.getServerValue()
							: MapConverter.getTestSideValues(header.getServerValue()));
			if (iterator.hasNext()) {
				this.blockBuilder.addLineWithEnding(text);
			}
			else {
				this.blockBuilder.addIndented(text);
			}
		}
		this.blockBuilder.addEndingIfNotPresent();
		return this;
	}

	private String processHeaderElement(String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			return this.comparisonBuilder
					.assertThat("response.header(\"" + property + "\")")
					+ matchesManuallyEscapedPattern((NotToEscapePattern) value);
		}
		else if (value instanceof ExecutionProperty) {
			return ((ExecutionProperty) value)
					.insertValue("response.header(\"" + property + "\")");

		}
		return this.comparisonBuilder.assertThat("response.header(\"" + property + "\")",
				value);
	}

	private String matchesManuallyEscapedPattern(NotToEscapePattern value) {
		return this.comparisonBuilder
				.matchesEscaped(value.getServerValue().pattern().replace("\\", "\\\\"));
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		return response.getHeaders() != null;
	}

}

interface GroovyComparisonBuilder extends ComparisonBuilder {

	ComparisonBuilder SPOCK_HTTP_INSTANCE = (GroovyComparisonBuilder) () -> SpockRestAssuredBodyParser.INSTANCE;

	ComparisonBuilder JAXRS_HTTP_INSTANCE = (GroovyComparisonBuilder) () -> JaxRsBodyParser.INSTANCE;

	ComparisonBuilder SPOCK_MESSAGING_INSTANCE = (GroovyComparisonBuilder) () -> SpockMessagingBodyParser.INSTANCE;

	@Override
	default String assertThat(String object) {
		return object;
	}

	@Override
	default String isEqualToUnquoted(String unquoted) {
		return " == " + unquoted;
	}

	@Override
	default String isEqualTo(Number number) {
		return " == " + number.toString();
	}

	@Override
	default String matches(String pattern) {
		return " ==~ java.util.regex.Pattern.compile("
				+ bodyParser().quotedShortText(pattern) + ")";
	}

	@Override
	default String matchesEscaped(String pattern) {
		return " ==~ java.util.regex.Pattern.compile("
				+ bodyParser().quotedEscapedShortText(pattern) + ")";
	}

	@Override
	default String isNotNull() {
		return " != null";
	}

}

interface ComparisonBuilder {

	ComparisonBuilder JAVA_HTTP_INSTANCE = () -> RestAssuredBodyParser.INSTANCE;

	ComparisonBuilder JAVA_MESSAGING_INSTANCE = () -> JavaMessagingBodyParser.INSTANCE;

	default String createComparison(Object headerValue) {
		if (headerValue instanceof Pattern) {
			return createComparison((Pattern) headerValue);
		}
		else if (headerValue instanceof Number) {
			return isEqualTo((Number) headerValue);
		}
		String escapedHeader = convertUnicodeEscapesIfRequired(headerValue.toString());
		return isEqualTo(escapedHeader);
	}

	default String createUnescapedComparison(Object headerValue) {
		if (headerValue instanceof Pattern) {
			return createComparison((Pattern) headerValue);
		}
		else if (headerValue instanceof Number) {
			return isEqualTo((Number) headerValue);
		}
		return isEqualTo(headerValue.toString());
	}

	default String assertThat(String object) {
		return "assertThat(" + object + ")";
	}

	default String assertThatIsNotNull(String object) {
		return assertThat(object) + isNotNull();
	}

	default String assertThat(String object, Object valueToCompareAgainst) {
		return assertThat(object) + createComparison(valueToCompareAgainst);
	}

	default String assertThatUnescaped(String object, Object valueToCompareAgainst) {
		return assertThat(object) + createUnescapedComparison(valueToCompareAgainst);
	}

	default String isEqualTo(String escapedHeaderValue) {
		return isEqualToUnquoted(bodyParser().quotedShortText(escapedHeaderValue));
	}

	default String isEqualToUnquoted(String unquoted) {
		return ".isEqualTo(" + unquoted + ")";
	}

	default String isEqualTo(Number number) {
		return ".isEqualTo(" + number.toString() + ")";
	}

	default String isNotNull() {
		return ".isNotNull()";
	}

	default String matches(String pattern) {
		return ".matches(" + bodyParser().quotedShortText(pattern) + ")";
	}

	default String matchesEscaped(String pattern) {
		return ".matches(" + bodyParser().quotedEscapedShortText(pattern) + ")";
	}

	default String convertUnicodeEscapesIfRequired(String json) {
		String unescapedJson = StringEscapeUtils.unescapeJson(json);
		return escapeJava(unescapedJson);
	}

	default String createComparison(Pattern value) {
		return matches(value.pattern());
	}

	BodyParser bodyParser();

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
				.getTestMode() == TestMode.MOCKMVC
				&& this.generatedClassMetaData.isAnyHttp();
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

	private final ComparisonBuilder comparisonBuilder;

	RestAssuredCookiesThen(BlockBuilder blockBuilder,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		processCookies(metadata);
		return this;
	}

	@Override
	public ComparisonBuilder comparisonBuilder() {
		return this.comparisonBuilder;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public String cookieKey(String key) {
		return "response.cookie(\"" + key + "\")";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		return response.getCookies() != null;
	}

}

class GenericHttpBodyThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	private final TemplateProcessor templateProcessor;

	private final ComparisonBuilder comparisonBuilder;

	private final List<Then> thens = new LinkedList<>();

	GenericHttpBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			BodyParser bodyParser, ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
		this.comparisonBuilder = comparisonBuilder;
		this.templateProcessor = new HandlebarsTemplateProcessor();
		this.thens.addAll(Arrays.asList(
				new GenericBinaryBodyThen(blockBuilder, metaData, this.bodyParser,
						comparisonBuilder),
				new GenericTextBodyThen(blockBuilder, metaData, this.bodyParser,
						this.comparisonBuilder),
				new GenericJsonBodyThen(blockBuilder, metaData, this.bodyParser,
						this.comparisonBuilder),
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

interface BodyThen {

	default DslProperty requestBody(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getBody();
	}

	default DslProperty responseBody(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBody();
	}

	default BodyMatchers responseBodyMatchers(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBodyMatchers();
	}

}

class GenericTextBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final BodyAssertionLineCreator bodyAssertionLineCreator;

	private final BodyParser bodyParser;

	private final ComparisonBuilder comparisonBuilder;

	GenericTextBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			BodyParser bodyParser, ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder,
				metaData, this.bodyParser.byteArrayString(), this.comparisonBuilder);
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Object convertedResponseBody = this.bodyParser.convertResponseBody(metadata);
		if (convertedResponseBody instanceof String) {
			convertedResponseBody = this.bodyParser
					.escapeForSimpleTextAssertion(convertedResponseBody.toString());
		}
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
				&& this.bodyParser.responseBody(metadata) != null
				&& !(this.bodyParser.responseBody(metadata)
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

	private final ComparisonBuilder comparisonBuilder;

	GenericJsonBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			BodyParser bodyParser, ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder,
				metaData, this.bodyParser.byteArrayString(), this.comparisonBuilder);
		this.generatedClassMetaData = metaData;
		this.templateProcessor = new HandlebarsTemplateProcessor();
		this.contractTemplate = new HandlebarsTemplateProcessor();
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		BodyMatchers bodyMatchers = this.bodyParser.responseBodyMatchers(metadata);
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
				Optional.of(this.blockBuilder.getLineEnding()),
				bodyParser::postProcessJsonPath);
		// TODO: Refactor spock from should comment out bdd blocks
		Object convertedResponseBody = jsonBodyVerificationBuilder
				.addJsonResponseBodyCheck(this.blockBuilder, responseBody, bodyMatchers,
						this.bodyParser.responseAsString(),
						this.generatedClassMetaData.configProperties
								.getTestFramework() != TestFramework.SPOCK);
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
		this.blockBuilder.addLineWithEnding(exec.insertValue(this.bodyParser
				.postProcessJsonPath("parsedJson.read(\"$" + property + "\")")));
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

	private String subtract(String self, String text) {
		int index = self.indexOf(text);
		if (index == -1) {
			return self;
		}
		int end = index + text.length();
		if (self.length() > end) {
			return self.substring(0, index) + self.substring(end);
		}
		return self.substring(0, index);
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
		BodyMatchers bodyMatchers = this.bodyParser.responseBodyMatchers(metadata);
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

	private final BodyParser bodyParser;

	private final ComparisonBuilder comparisonBuilder;

	GenericBinaryBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			BodyParser bodyParser, ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder,
				metaData, bodyParser.byteArrayString(), this.comparisonBuilder);
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Object responseBody = this.bodyParser.responseBody(metadata).getServerValue();
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
		Object responseBody = this.bodyParser.responseBody(metadata).getServerValue();
		if (!(responseBody instanceof FromFileProperty)) {
			return false;
		}
		return ((FromFileProperty) responseBody).isByte();
	}

}

interface SpockRestAssuredBodyParser extends RestAssuredBodyParser, GroovyBodyParser {

	BodyParser INSTANCE = (SpockRestAssuredBodyParser) HandlebarsTemplateProcessor::new;

	@Override
	default String responseAsString() {
		return "response.body.asString()";
	}

	@Override
	default String byteArrayString() {
		return "response.body.asByteArray()";
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
		Object responseBody = extractServerValueFromBody(contentType,
				body.getServerValue());
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
		DslProperty body = requestBody(metadata);
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
			return extractValue((GString) bodyValue, contentType,
					ContentUtils.GET_TEST_SIDE);
		}
		if (TEXT != contentType && FORM != contentType && DEFINED != contentType) {
			boolean dontParseStrings = contentType == JSON && bodyValue instanceof Map;
			Closure parsingClosure = dontParseStrings ? Closure.IDENTITY
					: MapConverter.JSON_PARSING_CLOSURE;
			return MapConverter.transformValues(bodyValue, ContentUtils.GET_TEST_SIDE,
					parsingClosure);
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

class BodyAssertionLineCreator {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	private final String byteArrayString;

	private final ComparisonBuilder comparisonBuilder;

	BodyAssertionLineCreator(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			String byteArrayString, ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(metaData);
		this.byteArrayString = byteArrayString;
		this.comparisonBuilder = comparisonBuilder;
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
			return this.comparisonBuilder.assertThat(this.byteArrayString)
					+ this.comparisonBuilder.isEqualToUnquoted(this.bodyReader
							.readBytesFromFileString(singleContractMetadata, value,
									CommunicationType.RESPONSE));
		}
		return getResponseBodyPropertyComparisonString(property, value.asString());
	}

	/**
	 * Builds the code that for the given {@code property} will compare it to the given
	 * String {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(String property,
			String value) {
		return this.comparisonBuilder.assertThatUnescaped("responseBody" + property,
				value);
	}

	/**
	 * Builds the code that for the given {@code property} will match it to the given
	 * regular expression {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(String property,
			Pattern value) {
		return this.comparisonBuilder.assertThat("responseBody" + property, value);
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

interface SpockMessagingBodyParser extends MessagingBodyParser, GroovyBodyParser {

	BodyParser INSTANCE = (SpockMessagingBodyParser) HandlebarsTemplateProcessor::new;

}

interface JavaMessagingBodyParser extends MessagingBodyParser {

	BodyParser INSTANCE = new JavaMessagingBodyParser() {
	};

}

interface MessagingBodyParser extends BodyParser {

	default String responseAsString() {
		return "contractVerifierObjectMapper.writeValueAsString(response.getPayload())";
	}

	default String byteArrayString() {
		return "response.getPayloadAsByteArray()";
	}

	@Override
	default DslProperty responseBody(SingleContractMetadata metadata) {
		return metadata.getContract().getOutputMessage().getBody();
	}

	@Override
	default BodyMatchers responseBodyMatchers(SingleContractMetadata metadata) {
		return metadata.getContract().getOutputMessage().getBodyMatchers();
	}

}

class SpockMessagingGiven extends MessagingGiven {

	private final GeneratedClassMetaData generatedClassMetaData;

	SpockMessagingGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, SpockMessagingBodyParser.INSTANCE);
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return super.accept(metadata) && this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
	}

}

class JavaMessagingGiven extends MessagingGiven {

	private final GeneratedClassMetaData generatedClassMetaData;

	JavaMessagingGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData, JavaMessagingBodyParser.INSTANCE);
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return super.accept(metadata) && this.generatedClassMetaData.configProperties
				.getTestFramework() != TestFramework.SPOCK;
	}

}

class MessagingGiven implements Given, MethodVisitor<Given>, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Given> givens = new LinkedList<>();

	MessagingGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.givens.addAll(Arrays.asList(
				new MessagingBodyGiven(this.blockBuilder,
						new BodyReader(this.generatedClassMetaData), bodyParser),
				new MessagingHeadersGiven(this.blockBuilder)));
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		startBodyBlock(this.blockBuilder, "given:");
		this.blockBuilder.addIndented(
				"ContractVerifierMessage inputMessage = contractVerifierMessaging.create(")
				.addEmptyLine().indent();
		this.givens.stream().filter(given -> given.accept(metadata)).forEach(given -> {
			given.apply(metadata);
			this.blockBuilder.addEmptyLine();
		});
		this.blockBuilder.unindent().unindent().startBlock().addIndented(")")
				.addEndingIfNotPresent().addEmptyLine().endBlock();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.isMessaging()
				&& metadata.getContract().getInput().getTriggeredBy() == null;
	}

}

class MessagingBodyGiven implements Given, MethodVisitor<Given> {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	MessagingBodyGiven(BlockBuilder blockBuilder, BodyReader bodyReader,
			BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = bodyReader;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		appendBodyGiven(metadata);
		return this;
	}

	private void appendBodyGiven(SingleContractMetadata metadata) {
		ContentType contentType = metadata.getInputTestContentType();
		Input inputMessage = metadata.getContract().getInput();
		Object bodyValue = this.bodyParser.extractServerValueFromBody(contentType,
				inputMessage.getMessageBody().getServerValue());
		if (bodyValue instanceof FromFileProperty) {
			FromFileProperty fileProperty = (FromFileProperty) bodyValue;
			String byteText = fileProperty.isByte()
					? this.bodyReader.readBytesFromFileString(metadata, fileProperty,
							CommunicationType.REQUEST)
					: this.bodyParser.quotedLongText(
							this.bodyReader.readStringFromFileString(metadata,
									fileProperty, CommunicationType.REQUEST));
			this.blockBuilder.addIndented(byteText);
		}
		else {
			String text = this.bodyParser.convertToJsonString(bodyValue);
			this.blockBuilder.addIndented(this.bodyParser.quotedEscapedLongText(text));
		}
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getMessageBody() != null;
	}

}

class MessagingHeadersGiven implements Given, MethodVisitor<Given> {

	private final BlockBuilder blockBuilder;

	MessagingHeadersGiven(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		Input inputMessage = metadata.getContract().getInput();
		this.blockBuilder.startBlock().addIndented(", headers()").startBlock();
		inputMessage.getMessageHeaders().executeForEachHeader(header -> {
			this.blockBuilder.addEmptyLine().addIndented(getHeaderString(header));
		});
		this.blockBuilder.endBlock();
		return this;
	}

	private String getHeaderString(Header header) {
		return ".header(" + getTestSideValue(header.getName()) + ", "
				+ getTestSideValue(header.getServerValue()) + ")";
	}

	private String getTestSideValue(Object object) {
		return '"' + MapConverter.getTestSideValues(object).toString() + '"';
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getMessageHeaders() != null;
	}

}

class MessagingWhen implements When, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> whens = new LinkedList<>();

	MessagingWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.whens.addAll(Arrays.asList(new MessagingTriggeredByWhen(this.blockBuilder),
				new MessagingBodyWhen(this.blockBuilder),
				new MessagingAssertThatWhen(this.blockBuilder)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "when:");
		bodyBlock(this.blockBuilder, this.whens, singleContractMetadata);
		this.blockBuilder.addEmptyLine();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging();
	}

}

class MessagingTriggeredByWhen implements When {

	private final BlockBuilder blockBuilder;

	MessagingTriggeredByWhen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented(
				metadata.getContract().getInput().getTriggeredBy().getExecutionCommand())
				.addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getTriggeredBy() != null;
	}

}

class MessagingBodyWhen implements When {

	private final BlockBuilder blockBuilder;

	MessagingBodyWhen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented("contractVerifierMessaging.send(inputMessage, \""
				+ metadata.getContract().getInput().getMessageFrom().getServerValue()
				+ "\")").addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getMessageFrom() != null;
	}

}

class MessagingAssertThatWhen implements When {

	private final BlockBuilder blockBuilder;

	MessagingAssertThatWhen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented(
				metadata.getContract().getInput().getAssertThat().getExecutionCommand());
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getAssertThat() != null;
	}

}

class JavaMessagingWithBodyThen extends MessagingWithBodyThen {

	private final GeneratedClassMetaData metaData;

	JavaMessagingWithBodyThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData,
				ComparisonBuilder.JAVA_MESSAGING_INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() != TestFramework.SPOCK;
	}

}

class SpockMessagingWithBodyThen extends MessagingWithBodyThen {

	private final GeneratedClassMetaData metaData;

	SpockMessagingWithBodyThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		super(blockBuilder, generatedClassMetaData,
				GroovyComparisonBuilder.SPOCK_MESSAGING_INSTANCE);
		this.metaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return super.accept(singleContractMetadata) && this.metaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
	}

}

class MessagingWithBodyThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	private final List<Then> thens = new LinkedList<>();

	MessagingWithBodyThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.comparisonBuilder = comparisonBuilder;
		this.thens.addAll(Arrays.asList(
				new MessagingSpockNoMessageThen(this.blockBuilder,
						generatedClassMetaData),
				new MessagingReceiveMessageThen(this.blockBuilder, generatedClassMetaData,
						this.comparisonBuilder),
				new MessagingHeadersThen(this.blockBuilder, generatedClassMetaData,
						this.comparisonBuilder),
				new MessagingBodyThen(this.blockBuilder, generatedClassMetaData,
						comparisonBuilder),
				new MessagingAssertThatThen(this.blockBuilder)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "then:");
		bodyBlock(this.blockBuilder, this.thens, singleContractMetadata);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging()
				&& singleContractMetadata.getContract().getOutputMessage() != null;
	}

}

class MessagingSpockNoMessageThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	MessagingSpockNoMessageThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		this.blockBuilder.addLineWithEnding("noExceptionThrown()");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging()
				&& singleContractMetadata.getContract().getOutputMessage() == null
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.SPOCK;
	}

}

class MessagingReceiveMessageThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	MessagingReceiveMessageThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		OutputMessage outputMessage = singleContractMetadata.getContract()
				.getOutputMessage();
		this.blockBuilder.addLineWithEnding(
				"ContractVerifierMessage response = contractVerifierMessaging.receive("
						+ sentToValue(outputMessage.getSentTo().getServerValue()) + ")");
		this.blockBuilder.addLineWithEnding(
				this.comparisonBuilder.assertThatIsNotNull("response"));
		return this;
	}

	private String sentToValue(Object sentTo) {
		if (sentTo instanceof ExecutionProperty) {
			return ((ExecutionProperty) sentTo).getExecutionCommand();
		}
		return '"' + sentTo.toString() + '"';
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging() && singleContractMetadata
				.getContract().getOutputMessage().getSentTo() != null;
	}

}

class MessagingHeadersThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	MessagingHeadersThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		endBodyBlock(this.blockBuilder);
		startBodyBlock(this.blockBuilder, "and:");
		OutputMessage outputMessage = singleContractMetadata.getContract()
				.getOutputMessage();
		outputMessage.getHeaders().executeForEachHeader(header -> {
			processHeaderElement(header.getName(),
					header.getServerValue() instanceof NotToEscapePattern
							? header.getServerValue()
							: MapConverter.getTestSideValues(header.getServerValue()));
		});
		return this;
	}

	private void appendLineWithHeaderNotNull(String property) {
		this.blockBuilder.addLineWithEnding(this.comparisonBuilder
				.assertThatIsNotNull("response.getHeader(\"" + property + "\")"));
	}

	private void processHeaderElement(String property, Object value) {
		if (value instanceof Number) {
			processHeaderElement(property, (Number) value);
		}
		else if (value instanceof Pattern) {
			processHeaderElement(property, (Pattern) value);
		}
		else if (value instanceof ExecutionProperty) {
			processHeaderElement(property, (ExecutionProperty) value);
		}
		else {
			processHeaderElement(property, value.toString());
		}
	}

	private void processHeaderElement(String property, String value) {
		appendLineWithHeaderNotNull(property);
		this.blockBuilder.addLineWithEnding(this.comparisonBuilder.assertThat(
				"response.getHeader(\"" + property + "\").toString()", value));
	}

	private void processHeaderElement(String property, Number value) {
		appendLineWithHeaderNotNull(property);
		blockBuilder.addLineWithEnding(this.comparisonBuilder
				.assertThat("response.getHeader(\"" + property + "\")", value));
	}

	private void processHeaderElement(String property, Pattern pattern) {
		appendLineWithHeaderNotNull(property);
		blockBuilder.addLineWithEnding(this.comparisonBuilder.assertThat(
				"response.getHeader(\"" + property + "\").toString()", pattern));
	}

	private void processHeaderElement(String property, ExecutionProperty exec) {
		appendLineWithHeaderNotNull(property);
		blockBuilder.addLineWithEnding(
				exec.insertValue("response.getHeader(\"" + property + "\").toString()"));
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging() && singleContractMetadata
				.getContract().getOutputMessage().getHeaders() != null;
	}

}

class MessagingBodyThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	private final List<Then> thens = new LinkedList<>();

	private final BodyParser bodyParser;

	MessagingBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyParser = comparisonBuilder.bodyParser();
		this.thens.addAll(Arrays.asList(
				new GenericBinaryBodyThen(blockBuilder, metaData, this.bodyParser,
						this.comparisonBuilder),
				new GenericTextBodyThen(blockBuilder, metaData, this.bodyParser,
						this.comparisonBuilder),
				new GenericJsonBodyThen(blockBuilder, metaData, this.bodyParser,
						this.comparisonBuilder),
				new GenericXmlBodyThen(blockBuilder, this.bodyParser)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		endBodyBlock(this.blockBuilder);
		startBodyBlock(this.blockBuilder, "and:");
		this.thens.stream().filter(then -> then.accept(singleContractMetadata))
				.forEach(then -> then.apply(singleContractMetadata));
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging()
				&& this.bodyParser.responseBody(singleContractMetadata) != null;
	}

}

class MessagingAssertThatThen implements Then {

	private final BlockBuilder blockBuilder;

	MessagingAssertThatThen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addLineWithEnding(metadata.getContract().getOutputMessage()
				.getAssertThat().getExecutionCommand());
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getOutputMessage().getAssertThat() != null;
	}

}

class SpockMessagingEmptyThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	SpockMessagingEmptyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		startBodyBlock(this.blockBuilder, "then:");
		this.blockBuilder.addLineWithEnding("noExceptionThrown()");
		endBodyBlock(this.blockBuilder);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.isMessaging()
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.SPOCK
				&& metadata.getContract().getOutputMessage() == null;
	}

}