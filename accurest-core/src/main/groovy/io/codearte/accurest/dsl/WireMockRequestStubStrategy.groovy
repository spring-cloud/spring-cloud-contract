package io.codearte.accurest.dsl
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.matching.RequestPattern
import com.github.tomakehurst.wiremock.matching.ValuePattern
import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import io.codearte.accurest.dsl.internal.*
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.ContentUtils
import io.codearte.accurest.util.JsonPathJsonConverter
import io.codearte.accurest.util.JsonPaths
import io.codearte.accurest.util.MapConverter

import java.util.regex.Pattern

import static io.codearte.accurest.util.ContentUtils.*
import static io.codearte.accurest.util.RegexpBuilders.buildGStringRegexpMatch
import static io.codearte.accurest.util.RegexpBuilders.buildJSONRegexpMatch

@TypeChecked
@PackageScope
class WireMockRequestStubStrategy extends BaseWireMockStubStrategy {

	private final Request request

	WireMockRequestStubStrategy(GroovyDsl groovyDsl) {
		this.request = groovyDsl.request
	}

	@PackageScope
	RequestPattern buildClientRequestContent() {
		RequestPattern requestPattern = new RequestPattern()
		appendMethod(requestPattern)
		appendHeaders(requestPattern)
		appendUrl(requestPattern)
		appendQueryParameters(requestPattern)
		appendBody(requestPattern)
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
		ContentType contentType = tryToGetContentType()
		if (contentType == ContentType.JSON) {
			JsonPaths values = JsonPathJsonConverter.transformToJsonPathWithStubsSideValues(getMatchingStrategyFromBody(request.body)?.clientValue)
			if (values.empty) {
				requestPattern.bodyPatterns = [new ValuePattern(jsonCompareMode: org.skyscreamer.jsonassert.JSONCompareMode.LENIENT,
						equalToJson: JsonOutput.toJson(getMatchingStrategy(request.body.clientValue).clientValue) ) ]
			} else {
				requestPattern.bodyPatterns = values.collect { new ValuePattern(matchesJsonPath: it.jsonPath) } ?: null
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

	private ContentType tryToGetContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(request.headers)
		if (contentType == ContentType.UNKNOWN) {
			if (!request.body.clientValue) {
				return ContentType.UNKNOWN
			}
			return ContentUtils.getClientContentType(request.body.clientValue)
		}
		return contentType
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
			requestPattern.setUrlPath(urlPath.toString())
		}
		if(!request.url) {
			return
		}
		Object url = request?.url?.clientValue
		if(url instanceof Pattern) {
			requestPattern.setUrlPattern(url.pattern())
		} else {
			requestPattern.setUrl(url.toString())
		}
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
				return new MatchingStrategy(buildGStringRegexpMatch(value), MatchingStrategy.Type.MATCHING)
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
