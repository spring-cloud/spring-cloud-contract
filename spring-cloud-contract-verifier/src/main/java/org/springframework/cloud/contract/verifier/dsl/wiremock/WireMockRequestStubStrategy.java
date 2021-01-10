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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import groovy.lang.GString;
import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.Body;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.spec.internal.OptionalProperty;
import org.springframework.cloud.contract.spec.internal.PathBodyMatcher;
import org.springframework.cloud.contract.spec.internal.QueryParameters;
import org.springframework.cloud.contract.spec.internal.RegexPatterns;
import org.springframework.cloud.contract.spec.internal.RegexProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;
import org.springframework.cloud.contract.verifier.dsl.ContractVerifierMetadata;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.cloud.contract.verifier.util.MethodBufferingJsonVerifiable;
import org.springframework.cloud.contract.verifier.util.xml.XmlToXPathsConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.contract.spec.internal.MatchingStrategy.Type.BINARY_EQUAL_TO;
import static org.springframework.cloud.contract.spec.internal.MatchingType.EQUALITY;
import static org.springframework.cloud.contract.verifier.util.ContentType.FORM;
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.getEqualsTypeFromContentType;
import static org.springframework.cloud.contract.verifier.util.RegexpBuilders.buildGStringRegexpForStubSide;
import static org.springframework.cloud.contract.verifier.util.RegexpBuilders.buildJSONRegexpMatch;
import static org.springframework.cloud.contract.verifier.util.xml.XmlToXPathsConverter.retrieveValue;

/**
 * Converts a {@link Request} into {@link RequestPattern}.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @author Olga Maciaszek-Sharma
 * @since 1.0.0
 */
class WireMockRequestStubStrategy extends BaseWireMockStubStrategy {

	private final Request request;

	private final ContentType contentType;

	WireMockRequestStubStrategy(Contract groovyDsl, SingleContractMetadata singleContractMetadata) {
		super(groovyDsl);
		this.request = groovyDsl.getRequest();
		this.contentType = contentType(singleContractMetadata);
	}

	protected ContentType contentType(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.getEvaluatedInputStubContentType();
	}

	RequestPattern buildClientRequestContent() {
		if (request == null) {
			return null;
		}
		RequestPatternBuilder requestPatternBuilder = appendMethodAndUrl();
		appendCookies(requestPatternBuilder);
		appendHeaders(requestPatternBuilder);
		appendQueryParameters(requestPatternBuilder);
		appendBody(requestPatternBuilder);
		appendMultipart(requestPatternBuilder);
		return requestPatternBuilder.build();
	}

	private void appendBody(RequestPatternBuilder requestPatternBuilder) {
		if (contract.getMetadata().containsKey(ContractVerifierMetadata.METADATA_KEY)) {
			ContractVerifierMetadata metadata = ContractVerifierMetadata.fromMetadata(contract.getMetadata());
			appendSpringCloudContractMatcher(metadata, requestPatternBuilder);
			if (!StringUtils.hasLength(metadata.getTool())) {
				doAppendBody(requestPatternBuilder);
			}
		}
		else {
			doAppendBody(requestPatternBuilder);
		}
	}

	private void appendSpringCloudContractMatcher(ContractVerifierMetadata metadata,
			RequestPatternBuilder requestPatternBuilder) {
		Parameters parameters = Parameters.one("tool", metadata.getTool() != null ? metadata.getTool() : "unknown");
		YamlContractConverter converter = new YamlContractConverter();
		List<YamlContract> contracts = converter.convertTo(Collections.singleton(contract));
		Map<String, byte[]> store = converter.store(contracts);
		parameters.put("contract", new String(store.entrySet().iterator().next().getValue()));
		requestPatternBuilder.andMatching(SpringCloudContractRequestMatcher.NAME, parameters);
	}

	private RequestPatternBuilder appendMethodAndUrl() {
		if (request.getMethod() == null) {
			return null;
		}
		RequestMethod requestMethod = RequestMethod.fromString(
				Optional.ofNullable(request.getMethod().getClientValue()).map(c -> c.toString()).orElse(null));
		UrlPattern urlPattern = urlPattern();
		return RequestPatternBuilder.newRequestPattern(requestMethod, urlPattern);
	}

	private void doAppendBody(RequestPatternBuilder requestPattern) {
		if (request.getBody() == null) {
			return;
		}
		boolean bodyHasMatchingStrategy = request.getBody().getClientValue() instanceof MatchingStrategy;
		MatchingStrategy matchingStrategy = getMatchingStrategyFromBody(request.getBody());
		if (contentType == ContentType.JSON) {
			Object clientSideBody = MapConverter.transformToClientValues(request.getBody());
			Object originalBody = Optional.ofNullable(matchingStrategy).map(DslProperty::getClientValue).orElse(null);
			if (bodyHasMatchingStrategy) {
				requestPattern.withRequestBody(convertToValuePattern(matchingStrategy));
			}
			else if (clientSideBody instanceof Pattern || clientSideBody instanceof RegexProperty) {
				requestPattern.withRequestBody(
						convertToValuePattern(appendBodyRegexpMatchPattern(request.getBody(), contentType)));
			}
			else {
				Object body = JsonToJsonPathsConverter.removeMatchingJsonPaths(originalBody, request.getBodyMatchers());
				JsonPaths values = JsonToJsonPathsConverter
						.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(body);
				if ((values.isEmpty() && request.getBodyMatchers() != null && !request.getBodyMatchers().hasMatchers())
						|| onlySizeAssertionsArePresent(values)) {
					try {
						requestPattern.withRequestBody(WireMock.equalToJson(
								new ObjectMapper()
										.writeValueAsString(getMatchingStrategy(request.getBody().getClientValue())
												.getClientValue()),
								false, false));
					}
					catch (JsonProcessingException e) {
						throw new IllegalArgumentException("The MatchingStrategy could not be serialized", e);
					}
				}
				else {
					values.stream().filter(v -> !v.assertsSize()).forEach(it -> requestPattern
							.withRequestBody(WireMock.matchingJsonPath(it.jsonPath().replace("\\\\", "\\"))));
				}
			}
			Optional.ofNullable(request.getBodyMatchers()).map(BodyMatchers::matchers)
					.ifPresent(bodyMatchers -> bodyMatchers.forEach(bodyMatcher -> {
						String newPath = JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(bodyMatcher,
								originalBody);
						requestPattern.withRequestBody(WireMock.matchingJsonPath(newPath.replace("\\\\", "\\")));
					}));
		}
		else if (contentType == ContentType.XML) {
			Object originalBody = Optional.ofNullable(matchingStrategy).map(DslProperty::getClientValue).orElse(null);
			if (bodyHasMatchingStrategy) {
				requestPattern.withRequestBody(convertToValuePattern(matchingStrategy));
			}
			else {
				Object body = XmlToXPathsConverter.removeMatchingXPaths(originalBody, request.getBodyMatchers());
				List<BodyMatcher> byEqualityMatchersFromXml = XmlToXPathsConverter.mapToMatchers(body);
				byEqualityMatchersFromXml.forEach(
						bodyMatcher -> addWireMockStubMatchingSection(bodyMatcher, requestPattern, originalBody));
			}
			Optional.ofNullable(request.getBodyMatchers()).map(BodyMatchers::matchers)
					.ifPresent(bodyMatchers -> bodyMatchers.forEach(
							bodyMatcher -> addWireMockStubMatchingSection(bodyMatcher, requestPattern, originalBody)));
		}
		else if (containsPattern(request.getBody())) {
			requestPattern.withRequestBody(convertToValuePattern(appendBodyRegexpMatchPattern(request.getBody())));
		}
		else {
			requestBodyGuessedFromMatchingStrategy(requestPattern);
		}
	}

	private Object generateConcreteValue(Object originalBody) {
		if (originalBody instanceof Pattern || originalBody instanceof RegexProperty) {
			return new RegexProperty(originalBody).generate();
		}
		return originalBody;
	}

	private RequestPatternBuilder requestBodyGuessedFromMatchingStrategy(RequestPatternBuilder requestPattern) {
		return requestPattern
				.withRequestBody(convertToValuePattern(getMatchingStrategy(request.getBody().getClientValue())));
	}

	private static void addWireMockStubMatchingSection(BodyMatcher matcher, RequestPatternBuilder requestPattern,
			Object body) {
		Set<MatchingType> matchingTypesUnsupportedForRequest = new HashSet<>(
				Arrays.asList(MatchingType.NULL, MatchingType.COMMAND, MatchingType.TYPE));
		if (!(matcher instanceof PathBodyMatcher)) {
			throw new IllegalArgumentException("Only jsonPath and XPath matchers can be processed.");
		}
		String retrievedValue = Optional.ofNullable(matcher.value()).map(Object::toString).orElseGet(() -> {
			if (matchingTypesUnsupportedForRequest.contains(matcher.matchingType())) {
				throw new IllegalArgumentException("Null, Command and Type matchers are not supported in requests.");
			}
			if (EQUALITY == matcher.matchingType()) {
				return retrieveValue(matcher, body);
			}
			else {
				return "";
			}
		});
		PathBodyMatcher pathMatcher = (PathBodyMatcher) matcher;
		requestPattern.withRequestBody(
				WireMock.matchingXPath(pathMatcher.path(), XPathBodyMatcherToWireMockValuePatternConverter
						.mapToPattern(pathMatcher.matchingType(), String.valueOf(retrievedValue))));
	}

	private boolean onlySizeAssertionsArePresent(JsonPaths values) {
		return !CollectionUtils.isEmpty(values)
				&& (request.getBodyMatchers() == null || !request.getBodyMatchers().hasMatchers())
				&& this.every(values.iterator(), MethodBufferingJsonVerifiable::assertsSize);
	}

	private <T> boolean every(Iterator<T> self, Function<T, Boolean> function) {
		while (self.hasNext()) {
			if (!function.apply(self.next())) {
				return false;
			}
		}
		return true;
	}

	private void appendMultipart(RequestPatternBuilder requestPattern) {
		if (request.getMultipart() == null) {
			return;
		}
		if (request.getMultipart().getClientValue() instanceof Map) {
			List<StringValuePattern> multipartPattern = ((Map<?, ?>) request.getMultipart()
					.getClientValue())
					.entrySet().stream().map(
							it -> it.getValue() instanceof NamedProperty
									? WireMock.matching(RegexPatterns.multipartFile(it.getKey(),
									((NamedProperty) it.getValue()).getName().getClientValue(),
									((NamedProperty) it.getValue()).getValue().getClientValue(),
									Optional.ofNullable(
											((NamedProperty) it.getValue()).getContentType())
											.map(DslProperty::getClientValue).orElse(null)))
									: WireMock.matching(RegexPatterns.multipartParam(it.getKey(),
									MapConverter.getStubSideValuesForNonBody(it.getValue()))))
					.collect(Collectors.toList());
			multipartPattern.forEach(requestPattern::withRequestBody);

		}

	}

	private void appendHeaders(RequestPatternBuilder requestPattern) {
		if (request.getHeaders() != null) {
			request.getHeaders().getEntries().forEach(header -> requestPattern.withHeader(header.getName(),
					(StringValuePattern) convertToValuePattern(header.getClientValue())));
		}
	}

	private void appendCookies(RequestPatternBuilder requestPattern) {
		if (request.getCookies() == null) {
			return;
		}
		request.getCookies().getEntries().forEach(cookie -> requestPattern.withCookie(cookie.getKey(),
				(StringValuePattern) convertToValuePattern(cookie.getClientValue())));
	}

	private UrlPattern urlPattern() {
		Object urlPath = urlPathOrUrlIfQueryPresent();
		if (urlPath != null) {
			if (urlPath instanceof Pattern || urlPath instanceof RegexProperty) {
				return WireMock.urlPathMatching((String) getStubSideValue(new RegexProperty(urlPath).pattern()));
			}
			else {
				return WireMock.urlPathEqualTo((String) getStubSideValue(urlPath.toString()));
			}
		}
		if (request.getUrl() == null) {
			throw new IllegalStateException("URL is required!");
		}
		Object url = getUrlIfGstring(request.getUrl().getClientValue());
		if (url instanceof Pattern || url instanceof RegexProperty) {
			return WireMock.urlMatching(new RegexProperty(url).pattern());
		}
		return WireMock.urlEqualTo(url.toString());
	}

	private Object urlPathOrUrlIfQueryPresent() {
		Object urlPath = Optional.ofNullable(request).map(Request::getUrlPath).map(DslProperty::getClientValue)
				.orElse(null);
		Object queryParamsFromUrl = Optional.ofNullable(request).map(Request::getUrl).map(Url::getQueryParameters)
				.map(QueryParameters::getParameters).orElse(null);
		if (urlPath != null) {
			return urlPath;
		}
		if (queryParamsFromUrl != null) {
			return Optional.ofNullable(request).map(Request::getUrl).map(Url::getClientValue).orElse(null);
		}
		return null;
	}

	private Object getUrlIfGstring(Object clientSide) {
		if (clientSide instanceof GString) {
			if (Arrays.stream(((GString) clientSide).getValues()).anyMatch(it -> {
				Object value = getStubSideValue(it);
				return value instanceof Pattern || value instanceof RegexProperty;
			})) {
				String string = getStubSideValue(clientSide).toString();
				return new RegexProperty(Pattern.compile(string));
			}
			else {
				return getStubSideValue(clientSide).toString();
			}
		}
		return clientSide;
	}

	private void appendQueryParameters(RequestPatternBuilder requestPattern) {
		QueryParameters queryParameters = Optional.ofNullable(request).map(Request::getUrlPath)
				.map(Url::getQueryParameters).orElseGet(() -> Optional.ofNullable(request).map(Request::getUrl)
						.map(Url::getQueryParameters).orElse(null));

		Optional.ofNullable(queryParameters).map(QueryParameters::getParameters).ifPresent(
				parameters -> parameters.forEach(parameter -> requestPattern.withQueryParam(parameter.getName(),
						(StringValuePattern) convertToValuePattern(parameter.getClientValue()))));
	}

	protected ContentPattern<?> convertToValuePattern(Object object) {
		if (object instanceof Pattern || object instanceof RegexProperty) {
			return WireMock.matching(new RegexProperty(object).pattern());
		}
		else if (object instanceof OptionalProperty) {
			return WireMock.matching(((OptionalProperty) object).optionalPattern());
		}
		else if (object instanceof MatchingStrategy) {
			MatchingStrategy value = (MatchingStrategy) object;
			switch (value.getType()) {
			case NOT_MATCHING:
				return WireMock.notMatching(value.getClientValue().toString());
			case ABSENT:
				return WireMock.absent();
			case EQUAL_TO:
				return WireMock.equalTo(clientBody(value.getClientValue(), contentType).toString());
			case CONTAINS:
				return WireMock.containing(clientBody(value.getClientValue(), contentType).toString());
			case MATCHING:
				return WireMock.matching(clientBody(value.getClientValue(), contentType).toString());
			case EQUAL_TO_JSON:
				return WireMock.equalToJson(clientBody(value.getClientValue(), contentType).toString());
			case EQUAL_TO_XML:
				return WireMock.equalToXml(clientBody(value.getClientValue(), contentType).toString());
			case BINARY_EQUAL_TO:
				return WireMock.binaryEqualTo((byte[]) clientBody(value.getClientValue(), contentType));
			default:
				throw new UnsupportedOperationException("Unknown matching strategy " + value.getType());
			}
		}
		else {
			return WireMock.equalTo(clientBody(object, contentType).toString());
		}
	}

	protected Object clientBody(Object bodyValue, ContentType contentType) {
		if (FORM == contentType) {
			if (bodyValue instanceof Map) {
				// [a:3, b:4] == "a=3&b=4"
				return ((Map<?, ?>) bodyValue).entrySet().stream()
						.map(e -> StringEscapeUtils.unescapeEcmaScript(e.getKey().toString() + "=" + e.getValue()))
						.collect(Collectors.joining("&"));
			}
			else if (bodyValue instanceof List) {
				// ["a=3", "b=4"] == "a=3&b=4"
				return ((List<?>) bodyValue).stream().map(it -> StringEscapeUtils.unescapeEcmaScript(it.toString()))
						.collect(Collectors.joining("&"));
			}
		}
		else if (bodyValue instanceof FromFileProperty) {
			return ((FromFileProperty) bodyValue).isByte() ? ((FromFileProperty) bodyValue).asBytes()
					: ((FromFileProperty) bodyValue).asString();
		}
		else if (JSON == contentType) {
			return parseBody(bodyValue, contentType);
		}
		return bodyValue;
	}

	private MatchingStrategy getMatchingStrategyFromBody(Body body) {
		if (body == null) {
			return null;
		}
		return getMatchingStrategy(body.getClientValue());
	}

	private MatchingStrategy getMatchingStrategy(Object bodyValue) {
		if (bodyValue instanceof GString) {
			return this.getMatchingStrategy((GString) bodyValue);
		}
		else if (bodyValue instanceof MatchingStrategy) {
			return this.getMatchingStrategy((MatchingStrategy) bodyValue);
		}
		else if (bodyValue instanceof FromFileProperty) {
			return this.getMatchingStrategy((FromFileProperty) bodyValue);
		}
		else {
			return tryToFindMachingStrategy(bodyValue);
		}
	}

	private MatchingStrategy getMatchingStrategy(FromFileProperty bodyValue) {
		return new MatchingStrategy(bodyValue, BINARY_EQUAL_TO);
	}

	private MatchingStrategy getMatchingStrategy(MatchingStrategy matchingStrategy) {
		return getMatchingStrategyIncludingContentType(matchingStrategy);
	}

	private MatchingStrategy getMatchingStrategy(GString gString) {
		if (gString == null) {
			return new MatchingStrategy("", MatchingStrategy.Type.EQUAL_TO);
		}
		Object extractedValue = ContentUtils.extractValue(gString,
				it -> it instanceof DslProperty ? ((DslProperty<?>) it).getClientValue() : getStringFromGString(it));

		Object value = getStringFromGString(extractedValue);
		return getMatchingStrategy(value);
	}

	private Object getStringFromGString(Object object) {
		return object instanceof GString ? object.toString() : object;
	}

	private MatchingStrategy tryToFindMachingStrategy(Object bodyValue) {
		return new MatchingStrategy(MapConverter.transformToClientValues(bodyValue),
				getEqualsTypeFromContentType(contentType));
	}

	private MatchingStrategy getMatchingStrategyIncludingContentType(MatchingStrategy matchingStrategy) {
		MatchingStrategy.Type type = matchingStrategy.getType();
		Object value = matchingStrategy.getClientValue();
		ContentType contentType = ContentUtils.recognizeContentTypeFromMatchingStrategy(type);
		if (contentType == ContentType.UNKNOWN && type == MatchingStrategy.Type.EQUAL_TO) {
			contentType = ContentUtils.recognizeContentTypeFromContent(value);
			type = getEqualsTypeFromContentType(contentType);
		}
		MatchingStrategy newMatchingStrategy;
		if (value instanceof Map) {
			newMatchingStrategy = new MatchingStrategy(parseBody((Map<?, ?>) value, contentType), type);
		}
		else if (value instanceof List) {
			newMatchingStrategy = new MatchingStrategy(parseBody((List<?>) value, contentType), type);
		}
		else if (value instanceof GString) {
			newMatchingStrategy = new MatchingStrategy(parseBody((GString) value, contentType), type);
		}
		else {
			newMatchingStrategy = new MatchingStrategy(parseBody(value, contentType), type);
		}
		return newMatchingStrategy;
	}

	private MatchingStrategy appendBodyRegexpMatchPattern(Object value, ContentType contentType) {
		Object clientValue = MapConverter.transformToClientValues(value);
		switch (contentType) {
		case JSON:
			return new MatchingStrategy(buildJSONRegexpMatch(clientValue), MatchingStrategy.Type.MATCHING);
		case UNKNOWN:
			return new MatchingStrategy(buildGStringRegexpForStubSide(clientValue), MatchingStrategy.Type.MATCHING);
		default:
			throw new IllegalStateException(contentType.name() + " pattern matching is not implemented yet");
		}
	}

	private MatchingStrategy appendBodyRegexpMatchPattern(Object value) {
		return appendBodyRegexpMatchPattern(value, ContentType.UNKNOWN);
	}

	private boolean containsPattern(Object o) {
		if (o instanceof GString) {
			return containsPattern(((GString) o).getValues());
		}
		else if (o instanceof Map) {
			return containsPattern(((Map<?, ?>) o).entrySet());
		}
		else if (o instanceof Collection) {
			List<Boolean> result = (List<Boolean>) ((Collection) o).stream().map(this::containsPattern)
					.collect(Collectors.toList());
			return result.stream().reduce(false, (a, b) -> a || b);
		}
		else if (o instanceof Object[]) {
			return containsPattern(Arrays.asList((Object[]) o));
		}
		else if (o instanceof Map.Entry<?, ?>) {
			return containsPattern(((Map.Entry<?, ?>) o).getValue());
		}
		else if (o instanceof RegexProperty) {
			return true;
		}
		else if (o instanceof DslProperty<?>) {
			return containsPattern(((DslProperty<?>) o).getClientValue());
		}
		else {
			return o instanceof Pattern;
		}
	}

}
