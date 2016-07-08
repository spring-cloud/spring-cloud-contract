/*
 *  Copyright 2013-2016 the original author or authors.
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

import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.matching.RequestPattern
import com.github.tomakehurst.wiremock.matching.ValuePattern
import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.QueryParameters
import org.springframework.cloud.contract.verifier.util.ContentUtils
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.MapConverter
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.OptionalProperty
import org.springframework.cloud.contract.spec.internal.RegexPatterns
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.verifier.util.ContentType

import java.util.regex.Pattern

import static ContentUtils.getEqualsTypeFromContentType
import static ContentUtils.recognizeContentTypeFromContent
import static ContentUtils.recognizeContentTypeFromHeader
import static ContentUtils.recognizeContentTypeFromMatchingStrategy
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
		RequestPattern requestPattern = new RequestPattern()
		appendMethod(requestPattern)
		appendHeaders(requestPattern)
		appendUrl(requestPattern)
		appendQueryParameters(requestPattern)
		appendBody(requestPattern)
		appendMultipart(requestPattern)
		return requestPattern
	}

	private void appendMethod(RequestPattern requestPattern) {
		if(!request.method) {
			return
		}
		requestPattern.setMethod(RequestMethod.fromString(request.method.clientValue?.toString()))
	}

	private void appendBody(RequestPattern requestPattern) {
		if (!request.body) {
			return
		}
		ContentType contentType = tryToGetContentType(request.body.clientValue, request.headers)
		if (contentType == ContentType.JSON) {
			JsonPaths values = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValues(getMatchingStrategyFromBody(request.body)?.clientValue)
			if (values.empty) {
				requestPattern.bodyPatterns = [new ValuePattern(jsonCompareMode: org.skyscreamer.jsonassert.JSONCompareMode.LENIENT,
						equalToJson: JsonOutput.toJson(getMatchingStrategy(request.body.clientValue).clientValue) ) ]
			} else {
				requestPattern.bodyPatterns = values.findAll{ !it.assertsSize() }.collect { new ValuePattern(matchesJsonPath: it.jsonPath().replace("\\\\", "\\")) } ?: null as List<ValuePattern>
			}
		} else if (contentType == ContentType.XML) {
			requestPattern.bodyPatterns = [new ValuePattern(equalToXml: getMatchingStrategy(request.body.clientValue).clientValue.toString())]
		} else if (containsPattern(request?.body)) {
				MatchingStrategy matchingStrategy = appendBodyRegexpMatchPattern(request.body)
				requestPattern.bodyPatterns = [convertToValuePattern(matchingStrategy)]
		} else {
			requestPattern.bodyPatterns = [convertToValuePattern(getMatchingStrategy(request.body.clientValue))]
		}
	}
	
	private void appendMultipart(RequestPattern requestPattern) {
		if (!request.multipart) {
			return
		}
		
		if (request.multipart.clientValue instanceof Map) {
			List<ValuePattern> multipartPatterns = (request.multipart.clientValue as Map).collect { 
				(it.value instanceof NamedProperty 
				? ValuePattern.matches(RegexPatterns.multipartFile(it.key, (it.value as NamedProperty).name.clientValue, (it.value as NamedProperty).value.clientValue))
				: ValuePattern.matches(RegexPatterns.multipartParam(it.key, it.value)) )
			}
			
			requestPattern.bodyPatterns ? requestPattern.bodyPatterns.addAll(multipartPatterns) : (requestPattern.bodyPatterns = multipartPatterns)
		}
	}

	private void appendHeaders(RequestPattern requestPattern) {
		if(!request.headers) {
			return
		}
		request.headers.entries.each {
			requestPattern.addHeader(it.name, convertToValuePattern(it.clientValue))
		}
	}

	private void appendUrl(RequestPattern requestPattern) {
		Object urlPath = request?.urlPath?.clientValue
		if (urlPath) {
			if(urlPath instanceof Pattern) {
				requestPattern.setUrlPathPattern(getStubSideValue(urlPath.toString()).toString())
			} else {
				requestPattern.setUrlPath(getStubSideValue(urlPath.toString()).toString())
			}
		}
		if(!request.url) {
			return
		}
		Object url = getUrlIfGstring(request?.url?.clientValue)
		if (url instanceof Pattern) {
			requestPattern.setUrlPattern(url.pattern())
		} else {
			requestPattern.setUrl(url.toString())
		}
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

	private void appendQueryParameters(RequestPattern requestPattern) {
		QueryParameters queryParameters = request?.urlPath?.queryParameters ?: request?.url?.queryParameters
		queryParameters?.parameters?.each {
			requestPattern.addQueryParam(it.name, convertToValuePattern(it.clientValue))
		}
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	private static ValuePattern convertToValuePattern(Object object) {
		switch (object) {
			case Pattern:
				Pattern value = object as Pattern
				return ValuePattern.matches(value.pattern())
			case OptionalProperty:
				OptionalProperty value = object as OptionalProperty
				return ValuePattern.matches(value.optionalPattern())
			case MatchingStrategy:
				MatchingStrategy value = object as MatchingStrategy
				switch (value.type) {
					case MatchingStrategy.Type.NOT_MATCHING:
						return new ValuePattern(doesNotMatch: value.clientValue)
					case MatchingStrategy.Type.ABSENT:
						return ValuePattern.absent()
					default:
						return ValuePattern."${value.type.name}"(value.clientValue)
				}
			default:
				return ValuePattern.equalTo(object.toString())
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
		ContentType contentType = recognizeContentTypeFromMatchingStrategy(type)
		if (contentType == ContentType.UNKNOWN && type == MatchingStrategy.Type.EQUAL_TO) {
			contentType = recognizeContentTypeFromContent(value)
			type = getEqualsTypeFromContentType(contentType)
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
		return getEqualsTypeFromContentType(recognizeContentTypeFromHeader(request.headers))
	}

}
