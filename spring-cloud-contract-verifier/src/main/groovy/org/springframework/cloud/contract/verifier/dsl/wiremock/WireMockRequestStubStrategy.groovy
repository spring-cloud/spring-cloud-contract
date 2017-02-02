/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.matching.*
import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.*
import org.springframework.cloud.contract.verifier.util.*

import java.util.regex.Pattern

import static org.springframework.cloud.contract.verifier.util.RegexpBuilders.buildGStringRegexpForStubSide
import static org.springframework.cloud.contract.verifier.util.RegexpBuilders.buildJSONRegexpMatch

/**
 * Converts a {@link Request} into {@link RequestPattern}
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
class WireMockRequestStubStrategy extends BaseWireMockStubStrategy {

	private final Request request

	WireMockRequestStubStrategy(Contract groovyDsl) {
		this.request = groovyDsl.request
	}

	@PackageScope
	RequestPattern buildClientRequestContent() {
		if(!request) {
			return null
		}
		RequestPatternBuilder requestPatternBuilder = appendMethodAndUrl()
		appendHeaders(requestPatternBuilder)
		appendQueryParameters(requestPatternBuilder)
		appendBody(requestPatternBuilder)
		appendMultipart(requestPatternBuilder)
		return requestPatternBuilder.build()
	}

	private RequestPatternBuilder appendMethodAndUrl() {
		if(!request.method) {
			return null
		}
		RequestMethod requestMethod = RequestMethod.fromString(request.method.clientValue?.toString())
		UrlPattern urlPattern = urlPattern()
		return RequestPatternBuilder.newRequestPattern(requestMethod, urlPattern)
	}

	private void appendBody(RequestPatternBuilder requestPattern) {
		if (!request.body) {
			return
		}
		ContentType contentType = tryToGetContentType(request.body.clientValue, request.headers)
		if (contentType == ContentType.JSON) {
			def originalBody = getMatchingStrategyFromBody(request.body)?.clientValue
			def body = JsonToJsonPathsConverter.removeMatchingJsonPaths(originalBody, request.matchers)
			JsonPaths values = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(body)
			if ((values.empty && !request.matchers?.hasMatchers()) || onlySizeAssertionsArePresent(values)) {
				requestPattern.withRequestBody(WireMock.equalToJson(JsonOutput.toJson(getMatchingStrategy(request.body.clientValue).clientValue), false, false))
			} else {
				values.findAll{ !it.assertsSize() }.each {
					requestPattern.withRequestBody(WireMock.matchingJsonPath(it.jsonPath().replace("\\\\", "\\")))
				}
			}
			if (request.matchers?.hasMatchers()) {
				request.matchers.jsonPathMatchers().each {
					String newPath = JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(it, originalBody)
					requestPattern.withRequestBody(WireMock.matchingJsonPath(newPath.replace("\\\\", "\\")))
				}
			}
		} else if (contentType == ContentType.XML) {
			requestPattern.withRequestBody(WireMock.equalToXml(getMatchingStrategy(request.body.clientValue).clientValue.toString()))
		} else if (containsPattern(request?.body)) {
				MatchingStrategy matchingStrategy = appendBodyRegexpMatchPattern(request.body)
			requestPattern.withRequestBody(convertToValuePattern(matchingStrategy))
		} else {
			requestPattern.withRequestBody(convertToValuePattern(getMatchingStrategy(request.body.clientValue)))
		}
	}

	private boolean onlySizeAssertionsArePresent(JsonPaths values) {
		return !values.empty && !request.matchers?.hasMatchers() && values.every { it.assertsSize() }
	}

	private void appendMultipart(RequestPatternBuilder requestPattern) {
		if (!request.multipart) {
			return
		}
		if (request.multipart.clientValue instanceof Map) {
			List<StringValuePattern> multipartPatterns = (request.multipart.clientValue as Map).collect {
				(it.value instanceof NamedProperty 
				? WireMock.matching(RegexPatterns.multipartFile(it.key, (it.value as NamedProperty).name.clientValue, (it.value as NamedProperty).value.clientValue))
				: WireMock.matching(RegexPatterns.multipartParam(it.key, it.value)) )
			}
			multipartPatterns.each {
				requestPattern.withRequestBody(it)
			}
		}
	}

	private void appendHeaders(RequestPatternBuilder requestPattern) {
		if(!request.headers) {
			return
		}
		request.headers.entries.each {
			requestPattern.withHeader(it.name, convertToValuePattern(it.clientValue))
		}
	}

	private UrlPattern urlPattern() {
		Object urlPath = request?.urlPath?.clientValue
		if (urlPath) {
			if(urlPath instanceof Pattern) {
				return WireMock.urlPathMatching(getStubSideValue(urlPath.toString()) as String)
			} else {
				return WireMock.urlPathEqualTo(getStubSideValue(urlPath.toString()) as String)
			}
		}
		if(!request.url) {
			throw new IllegalStateException("URL is required!")
		}
		Object url = getUrlIfGstring(request?.url?.clientValue)
		if (url instanceof Pattern) {
			return WireMock.urlMatching(url.pattern())
		}
		return WireMock.urlEqualTo(url.toString())
	}

	private Object getUrlIfGstring(Object clientSide) {
		if (clientSide instanceof GString) {
			if (clientSide.values.any { getStubSideValue(it) instanceof Pattern }) {
				return Pattern.compile(getStubSideValue(clientSide).toString())
			} else {
				return getStubSideValue(clientSide).toString()
			}
		}
		return clientSide
	}

	private void appendQueryParameters(RequestPatternBuilder requestPattern) {
		QueryParameters queryParameters = request?.urlPath?.queryParameters ?: request?.url?.queryParameters
		queryParameters?.parameters?.each {
			requestPattern.withQueryParam(it.name, convertToValuePattern(it.clientValue))
		}
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	private static StringValuePattern convertToValuePattern(Object object) {
		switch (object) {
			case Pattern:
				Pattern value = object as Pattern
				return WireMock.matching(value.pattern())
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
						return WireMock."${value.type.name}"(value.clientValue)
				}
			default:
				return WireMock.equalTo(object.toString())
		}
	}

	private MatchingStrategy getMatchingStrategyFromBody(Body body) {
		if(!body) {
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

	private MatchingStrategy tryToFindMachingStrategy(Object bodyValue) {
		return new MatchingStrategy(MapConverter.transformToClientValues(bodyValue), getEqualsTypeFromContentTypeHeader())
	}

	private MatchingStrategy getMatchingStrategyIncludingContentType(MatchingStrategy matchingStrategy) {
		MatchingStrategy.Type type = matchingStrategy.type
		Object value = matchingStrategy.clientValue
		ContentType contentType = ContentUtils.recognizeContentTypeFromMatchingStrategy(type)
		if (contentType == ContentType.UNKNOWN && type == MatchingStrategy.Type.EQUAL_TO) {
			contentType = ContentUtils.recognizeContentTypeFromContent(value)
			type = ContentUtils.getEqualsTypeFromContentType(contentType)
		}
		return new MatchingStrategy(parseBody(value, contentType), type)
	}

	private MatchingStrategy appendBodyRegexpMatchPattern(Object value, ContentType contentType) {
		switch (contentType) {
			case ContentType.JSON:
				return new MatchingStrategy(buildJSONRegexpMatch(value), MatchingStrategy.Type.MATCHING)
			case ContentType.UNKNOWN:
				return new MatchingStrategy(buildGStringRegexpForStubSide(value), MatchingStrategy.Type.MATCHING)
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
		return collection.collect(this.&containsPattern).inject('') { a, b -> a || b }
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

	private boolean containsPattern(Object o) {
		return false
	}

	private MatchingStrategy.Type getEqualsTypeFromContentTypeHeader() {
		return ContentUtils.getEqualsTypeFromContentType(ContentUtils.recognizeContentTypeFromHeader(request.headers))
	}

}
