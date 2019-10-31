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

package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.matching.*
import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Commons
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.*
import org.springframework.cloud.contract.verifier.util.*
import org.springframework.cloud.contract.verifier.util.xml.XmlToXPathsConverter

import java.util.regex.Pattern

import static org.springframework.cloud.contract.spec.internal.MatchingStrategy.Type.BINARY_EQUAL_TO
import static org.springframework.cloud.contract.spec.internal.MatchingType.*
import static org.springframework.cloud.contract.verifier.util.ContentType.FORM
import static org.springframework.cloud.contract.verifier.util.ContentUtils.getEqualsTypeFromContentType
import static org.springframework.cloud.contract.verifier.util.RegexpBuilders.buildGStringRegexpForStubSide
import static org.springframework.cloud.contract.verifier.util.RegexpBuilders.buildJSONRegexpMatch
import static org.springframework.cloud.contract.verifier.util.xml.XmlToXPathsConverter.retrieveValue

/**
 * Converts a {@link Request} into {@link RequestPattern}
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @author Olga Maciaszek-Sharma
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
@Commons
class WireMockRequestStubStrategy extends BaseWireMockStubStrategy {

	private final Request request
	private final ContentType contentType

	WireMockRequestStubStrategy(Contract groovyDsl) {
		super(groovyDsl)
		this.request = groovyDsl.request
		this.contentType =
				tryToGetContentType(request?.body?.clientValue, request?.headers)
	}

	@PackageScope
	RequestPattern buildClientRequestContent() {
		if (!request) {
			return null
		}
		RequestPatternBuilder requestPatternBuilder = appendMethodAndUrl()
		appendCookies(requestPatternBuilder)
		appendHeaders(requestPatternBuilder)
		appendQueryParameters(requestPatternBuilder)
		appendBody(requestPatternBuilder)
		appendMultipart(requestPatternBuilder)
		return requestPatternBuilder.build()
	}

	private RequestPatternBuilder appendMethodAndUrl() {
		if (!request.method) {
			return null
		}
		RequestMethod requestMethod = RequestMethod.
				fromString(request.method.clientValue?.toString())
		UrlPattern urlPattern = urlPattern()
		return RequestPatternBuilder.newRequestPattern(requestMethod, urlPattern)
	}

	private void appendBody(RequestPatternBuilder requestPattern) {
		if (!request.body) {
			return
		}
		boolean bodyHasMatchingStrategy = request.body.clientValue instanceof MatchingStrategy
		MatchingStrategy matchingStrategy = getMatchingStrategyFromBody(request.body)
		if (contentType == ContentType.JSON) {
			def originalBody = matchingStrategy?.clientValue
			if (bodyHasMatchingStrategy) {
				requestPattern.withRequestBody(
						convertToValuePattern(matchingStrategy))
			} else if (containsPattern(request?.body)) {
				requestPattern.withRequestBody(
						convertToValuePattern(appendBodyRegexpMatchPattern(request.body)))
			} else {
				def body = JsonToJsonPathsConverter.
						removeMatchingJsonPaths(originalBody, request.bodyMatchers)
				JsonPaths values = JsonToJsonPathsConverter.
						transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(body)
				if ((values.empty && !request.bodyMatchers?.hasMatchers())
						||
						onlySizeAssertionsArePresent(values)) {
					requestPattern.withRequestBody(WireMock.equalToJson(JsonOutput.toJson(
							getMatchingStrategy(request.body.clientValue).clientValue),
							false, false))
				} else {
					values.findAll { !it.assertsSize() }.each {
						requestPattern.withRequestBody(WireMock.
								matchingJsonPath(it.jsonPath().replace("\\\\", "\\")))
					}
				}
			}
			request.bodyMatchers?.matchers()?.each {
				String newPath = JsonToJsonPathsConverter.
						convertJsonPathAndRegexToAJsonPath(it, originalBody)
				requestPattern.withRequestBody(WireMock.
						matchingJsonPath(newPath.replace("\\\\", "\\")))
			}
		}
		else if (contentType == ContentType.XML) {
			Object originalBody = matchingStrategy?.clientValue
			if (bodyHasMatchingStrategy) {
				requestPattern.withRequestBody(
						convertToValuePattern(matchingStrategy))
			} else {
				Object body = XmlToXPathsConverter
						.removeMatchingXPaths(originalBody, request.bodyMatchers)
				List<BodyMatcher> byEqualityMatchersFromXml = new XmlToXPathsConverter()
						.mapToMatchers(body)
				byEqualityMatchersFromXml.each {
					addWireMockStubMatchingSection(it, requestPattern, originalBody)
				}
			}
			request.bodyMatchers?.matchers()?.each {
				addWireMockStubMatchingSection(it, requestPattern, originalBody)
			}
		}
		else if (containsPattern(request?.body)) {
			requestPattern.withRequestBody(
					convertToValuePattern(appendBodyRegexpMatchPattern(request.body)))
		}
		else {
			requestBodyGuessedFromMatchingStrategy(requestPattern)
		}
	}

	private Object generateConcreteValue(Object originalBody) {
		if (originalBody instanceof Pattern || originalBody instanceof RegexProperty) {
			return new RegexProperty(originalBody).generate()
		}
		return originalBody
	}

	private RequestPatternBuilder requestBodyGuessedFromMatchingStrategy(RequestPatternBuilder requestPattern) {
		return requestPattern.withRequestBody(convertToValuePattern(
				getMatchingStrategy(request.body.clientValue)))
	}

	private static void addWireMockStubMatchingSection(BodyMatcher matcher,
			RequestPatternBuilder requestPattern,
			Object body) {
		Set<MatchingType> matchingTypesUnsupportedForRequest = [NULL, COMMAND, TYPE] as Set
		if (!matcher instanceof PathBodyMatcher) {
			throw new IllegalArgumentException("Only jsonPath and XPath matchers can be processed.")
		}
		String retrievedValue = Optional.ofNullable(matcher.value()).orElseGet({
			if (matchingTypesUnsupportedForRequest.contains(matcher.matchingType())) {
				throw new IllegalArgumentException("Null, Command and Type matchers are not supported in requests.")
			}
			if (EQUALITY == matcher.matchingType()) {
				return retrieveValue(matcher, body)
			}
			else {
				return ''
			}
		})
		PathBodyMatcher pathMatcher = matcher as PathBodyMatcher
		requestPattern.withRequestBody(WireMock.matchingXPath(pathMatcher.path(),
				XPathBodyMatcherToWireMockValuePatternConverter
						.mapToPattern(pathMatcher.matchingType(),
						String.valueOf(retrievedValue))))
	}

	private boolean onlySizeAssertionsArePresent(JsonPaths values) {
		return !values.empty && !request.bodyMatchers?.hasMatchers() && values.
				every { it.assertsSize() }
	}

	private void appendMultipart(RequestPatternBuilder requestPattern) {
		if (!request.multipart) {
			return
		}
		if (request.multipart.clientValue instanceof Map) {
			List<StringValuePattern> multipartPatterns = (request.multipart.clientValue as Map).
					collect {
						(it.value instanceof NamedProperty
								? WireMock.matching(RegexPatterns.
								multipartFile(it.key, (it.value as NamedProperty).name.clientValue,
										(it.value as NamedProperty).value.clientValue,
										(it.value as NamedProperty).contentType?.clientValue))
								: WireMock.
								matching(RegexPatterns.multipartParam(it.key, it.value)))
					}
			multipartPatterns.each {
				requestPattern.withRequestBody(it)
			}
		}
	}

	private void appendHeaders(RequestPatternBuilder requestPattern) {
		if (!request.headers) {
			return
		}
		request.headers.entries.each {
			requestPattern.withHeader(it.name, (StringValuePattern)
					convertToValuePattern(it.clientValue))
		}
	}

	private void appendCookies(RequestPatternBuilder requestPattern) {
		if (!request.cookies) {
			return
		}
		request.cookies.entries.each {
			requestPattern.withCookie(it.key, (StringValuePattern)
					convertToValuePattern(it.clientValue))
		}
	}

	private UrlPattern urlPattern() {
		Object urlPath = urlPathOrUrlIfQueryPresent()
		if (urlPath) {
			if (urlPath instanceof Pattern || urlPath instanceof RegexProperty) {
				return WireMock.urlPathMatching(
						getStubSideValue(new RegexProperty(urlPath).pattern()) as String)
			}
			else {
				return WireMock.
						urlPathEqualTo(getStubSideValue(urlPath.toString()) as String)
			}
		}
		if (!request.url) {
			throw new IllegalStateException("URL is required!")
		}
		Object url = getUrlIfGstring(request?.url?.clientValue)
		if (url instanceof Pattern || url instanceof RegexProperty) {
			return WireMock.urlMatching(new RegexProperty(url).pattern())
		}
		return WireMock.urlEqualTo(url.toString())
	}

	private Object urlPathOrUrlIfQueryPresent() {
		Object urlPath = request?.urlPath?.clientValue
		Object queryParamsFromUrl = request?.url?.queryParameters?.parameters
		if (urlPath) {
			return urlPath
		}
		if (queryParamsFromUrl) {
			return request?.url?.clientValue
		}
		return null
	}

	private Object getUrlIfGstring(Object clientSide) {
		if (clientSide instanceof GString) {
			if (clientSide.values.any {
				def value = getStubSideValue(it)
				return value instanceof Pattern || value instanceof RegexProperty
			}) {
				String string = getStubSideValue(clientSide).toString()
				return new RegexProperty(Pattern.compile(string))
			}
			else {
				return getStubSideValue(clientSide).toString()
			}
		}
		return clientSide
	}

	private void appendQueryParameters(RequestPatternBuilder requestPattern) {
		QueryParameters queryParameters = request?.urlPath?.queryParameters ?: request?.url?.queryParameters
		queryParameters?.parameters?.each {
			requestPattern.withQueryParam(it.name, (StringValuePattern)
					convertToValuePattern(it.clientValue))
		}
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	private ContentPattern convertToValuePattern(Object object) {
		switch (object) {
		case Pattern:
		case RegexProperty:
			return WireMock.matching(new RegexProperty(object).pattern())
		case OptionalProperty:
			OptionalProperty value = object as OptionalProperty
			return WireMock.matching(value.optionalPattern())
		case MatchingStrategy:
			MatchingStrategy value = object as MatchingStrategy
			switch (value.type) {
			case MatchingStrategy.Type.NOT_MATCHING:
				return WireMock.notMatching(value.clientValue.toString())
			case MatchingStrategy.Type.ABSENT:
				return WireMock.absent()
			default:
				try {
					return WireMock."${value.type.name}"(
							clientBody(value.clientValue, contentType))
				}
				catch (Throwable t) {
					log.error("Exception occurred while trying to call WireMock.${value.type.name}(${value.clientValue})", t)
					throw t
				}
			}
		default:
			return WireMock.equalTo(object.toString())
		}
	}

	protected static Object clientBody(Object bodyValue, ContentType contentType) {
		if (FORM == contentType) {
			if (bodyValue instanceof Map) {
				// [a:3, b:4] == "a=3&b=4"
				return ((Map) bodyValue).collect {
					StringEscapeUtils.
							unescapeJavaScript(it.key.toString() + "=" + it.value)
				}.join("&")
			}
			else if (bodyValue instanceof List) {
				// ["a=3", "b=4"] == "a=3&b=4"
				return ((List) bodyValue).collect {
					StringEscapeUtils.unescapeJavaScript(it.toString())
				}.join("&")
			}
		}
		else if (bodyValue instanceof FromFileProperty) {
			return bodyValue.isByte() ? bodyValue.asBytes() : bodyValue.asString()
		}
		return bodyValue
	}

	private MatchingStrategy getMatchingStrategyFromBody(Body body) {
		if (!body) {
			return null
		}
		return getMatchingStrategy(body.clientValue)
	}

	private MatchingStrategy getMatchingStrategy(MatchingStrategy matchingStrategy) {
		return getMatchingStrategyIncludingContentType(matchingStrategy)
	}

	private MatchingStrategy getMatchingStrategy(GString gString) {
		if (!gString) {
			return new MatchingStrategy("", MatchingStrategy.Type.EQUAL_TO)
		}
		def extractedValue = ContentUtils.extractValue(gString) {
			it instanceof DslProperty ? it.clientValue : getStringFromGString(it)
		}
		def value = getStringFromGString(extractedValue)
		return getMatchingStrategy(value)
	}

	private def getStringFromGString(Object object) {
		return object instanceof GString ? object.toString() : object
	}

	private MatchingStrategy getMatchingStrategy(Object bodyValue) {
		return tryToFindMachingStrategy(bodyValue)
	}

	private MatchingStrategy getMatchingStrategy(FromFileProperty bodyValue) {
		return new MatchingStrategy(bodyValue, BINARY_EQUAL_TO)
	}

	private MatchingStrategy tryToFindMachingStrategy(Object bodyValue) {
		return new MatchingStrategy(MapConverter.transformToClientValues(bodyValue),
				getEqualsTypeFromContentType(contentType))
	}

	private MatchingStrategy getMatchingStrategyIncludingContentType(MatchingStrategy matchingStrategy) {
		MatchingStrategy.Type type = matchingStrategy.type
		Object value = matchingStrategy.clientValue
		ContentType contentType = ContentUtils.
				recognizeContentTypeFromMatchingStrategy(type)
		if (contentType == ContentType.UNKNOWN && type == MatchingStrategy.Type.EQUAL_TO) {
			contentType = ContentUtils.recognizeContentTypeFromContent(value)
			type = getEqualsTypeFromContentType(contentType)
		}
		return new MatchingStrategy(parseBody(value, contentType), type)
	}

	private MatchingStrategy appendBodyRegexpMatchPattern(Object value, ContentType contentType) {
		switch (contentType) {
		case ContentType.JSON:
			return new MatchingStrategy(
					buildJSONRegexpMatch(value), MatchingStrategy.Type.MATCHING)
		case ContentType.UNKNOWN:
			return new MatchingStrategy(
					buildGStringRegexpForStubSide(value), MatchingStrategy.Type.MATCHING)
		case ContentType.XML:
			throw new IllegalStateException("XML pattern matching is not implemented yet")
		}
	}

	private MatchingStrategy appendBodyRegexpMatchPattern(Object value) {
		return appendBodyRegexpMatchPattern(value, ContentType.UNKNOWN)
	}

	private boolean containsPattern(GString bodyAsValue) {
		return containsPattern(bodyAsValue.values)
	}

	private boolean containsPattern(Map map) {
		return containsPattern(map.entrySet())
	}

	private boolean containsPattern(Collection collection) {
		return collection.collect(this.&containsPattern).inject(false) { a, b -> a || b }
	}

	private boolean containsPattern(Object[] objects) {
		return containsPattern(objects.toList())
	}

	private boolean containsPattern(Map.Entry entry) {
		return containsPattern(entry.value)
	}

	private boolean containsPattern(DslProperty dslProperty) {
		return containsPattern(dslProperty.clientValue)
	}

	private boolean containsPattern(Pattern pattern) {
		return true
	}

	private boolean containsPattern(RegexProperty pattern) {
		return true
	}

	private boolean containsPattern(Object o) {
		return false
	}
}
