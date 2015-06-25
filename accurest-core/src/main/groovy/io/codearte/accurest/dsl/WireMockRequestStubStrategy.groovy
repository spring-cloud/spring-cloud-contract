package io.codearte.accurest.dsl
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.internal.Body
import io.codearte.accurest.dsl.internal.ClientRequest
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.QueryParameters
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.util.ContentType

import java.util.regex.Pattern

import static io.codearte.accurest.util.ContentUtils.getEqualsTypeFromContentType
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromMatchingStrategy
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
	Map buildClientRequestContent() {
		return buildRequestContent(new ClientRequest(request))
	}

	private Map<String, Object> buildRequestContent(ClientRequest request) {
		return ([method    : request?.method?.clientValue,
				headers   : buildClientRequestHeadersSection(request.headers)
		] << appendUrl(request) << appendQueryParameters(request) << appendBody(request)).findAll { it.value }
	}

	private Map<String, Object> appendUrl(ClientRequest clientRequest) {
		Object urlPath = clientRequest?.urlPath?.clientValue
		if (urlPath) {
			return [urlPath: urlPath]
		}
		Object url = clientRequest?.url?.clientValue
		return url instanceof Pattern ? [urlPattern: url.pattern()] : [url: url]
	}

	private Map<String, Object> appendQueryParameters(ClientRequest clientRequest) {
		QueryParameters queryParameters = clientRequest?.urlPath?.queryParameters ?: clientRequest?.url?.queryParameters
		return queryParameters && !queryParameters.parameters.isEmpty() ?
				[queryParameters: buildUrlPathQueryParameters(queryParameters)] : [:]
	}

	private Map<String, Object> buildUrlPathQueryParameters(QueryParameters queryParameters) {
		return queryParameters.parameters.collectEntries { QueryParameter param ->
			parseQueryParameter(param.name, param.clientValue)
		}
	}

	protected Map<String, Object> parseQueryParameter(String name, MatchingStrategy matchingStrategy) {
		return buildQueryParameter(name, matchingStrategy.clientValue, matchingStrategy.type)
	}

	protected Map<String, Object> parseQueryParameter(String name, Object value) {
		return buildQueryParameter(name, value, MatchingStrategy.Type.EQUAL_TO)
	}

	protected Map<String, Object> parseQueryParameter(String name, Pattern pattern) {
		return buildQueryParameter(name, pattern.pattern(), MatchingStrategy.Type.MATCHING)
	}

	private Map<String, Object> buildQueryParameter(String name, Pattern pattern, MatchingStrategy.Type type) {
		return buildQueryParameter(name, pattern.pattern(), type)
	}

	private Map<String, Object> buildQueryParameter(String name, Object value, MatchingStrategy.Type type) {
		return [(name): [(type.name) : value]]
	}

	private Map<String, Object> appendBody(ClientRequest clientRequest) {
		return clientRequest.body? appendBody(clientRequest.body) : [:]
	}

	private Map<String, Object> appendBody(Body body) {
		return [bodyPatterns: (appendBodyPatterns(body.clientValue))]
	}

	private List<Map<String, Object>> appendBodyPatterns(MatchingStrategy matchingStrategy) {
		return [appendBodyPattern(matchingStrategy)]
	}

	private List<Map<String, Object>> appendBodyPatterns(Object bodyValue) {
		return appendBodyPatterns(new MatchingStrategy(bodyValue, getEqualsTypeFromContentTypeHeader()))
	}

	private Map<String, Object> appendBodyPattern(MatchingStrategy matchingStrategy) {
		MatchingStrategy.Type type = matchingStrategy.type
		Object value = matchingStrategy.clientValue
		ContentType contentType = recognizeContentTypeFromMatchingStrategy(type)
		if (contentType == ContentType.UNKNOWN && type == MatchingStrategy.Type.EQUAL_TO) {
			contentType = recognizeContentTypeFromContent(value)
			type = getEqualsTypeFromContentType(contentType)
		}
		if (containsPattern(value)) {
			return appendBodyRegexpMatchPattern(value, contentType)
		}
		return buildMatchPattern(new MatchingStrategy(parseBody(value, contentType), type))
	}

	private Map<String, Object> appendBodyRegexpMatchPattern(Object value, ContentType contentType) {
		switch (contentType) {
			case ContentType.JSON:
				return buildMatchPattern(new MatchingStrategy(buildJSONRegexpMatch(value), MatchingStrategy.Type.MATCHING))
			case ContentType.UNKNOWN:
				return buildMatchPattern(new MatchingStrategy(buildGStringRegexpMatch(value), MatchingStrategy.Type.MATCHING))
			case ContentType.XML:
				throw new IllegalStateException("XML pattern matching is not implemented yet")
		}
	}

	private Map<String, Object> buildMatchPattern(MatchingStrategy matchingStrategy) {
		Map<String, ? extends Object> result = [(matchingStrategy.type.name): matchingStrategy.clientValue.toString()]
		if (matchingStrategy.type == MatchingStrategy.Type.EQUAL_TO_JSON && matchingStrategy.jsonCompareMode) {
			return result << [jsonCompareMode : (matchingStrategy.jsonCompareMode.toString())]
		}
		return result
	}

	private boolean containsPattern(GString bodyAsValue) {
		return containsPattern(bodyAsValue.values)
	}

	private boolean containsPattern(Map map) {
		return containsPattern(map.entrySet())
	}

	private boolean containsPattern(Collection collection) {
		return collection.collect(this.&containsPattern).inject { a, b -> a || b }
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
