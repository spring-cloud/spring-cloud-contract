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

import static io.codearte.accurest.util.ContentUtils.extractValue
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader
import static io.codearte.accurest.util.ContentUtils.getEqualsTypeFromContentType
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromMatchingStrategy

@TypeChecked
@PackageScope
class WiremockRequestStubStrategy extends BaseWiremockStubStrategy {

	private final Request request

	WiremockRequestStubStrategy(GroovyDsl groovyDsl) {
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

	private List<Map<String, Object>> appendBodyPatterns(List<MatchingStrategy> matchingStrategies) {
		return matchingStrategies.collect { appendBodyPattern(it) }
	}

	private List<Map<String, Object>> appendBodyPatterns(GString gString) {
		if (containsPattern(gString)) {
			Object value = extractValue(gString, { DslProperty dslProperty -> dslProperty.clientValue })
			return appendBodyPatterns(extractReqexpMatching(value))
		}
		return appendBodyPatterns(new MatchingStrategy(gString, getEqualsTypeFromContentTypeHeader()))
	}

	private List<Map<String, Object>> appendBodyPatterns(Object bodyValue) {
		return appendBodyPatterns(new MatchingStrategy(bodyValue, MatchingStrategy.Type.EQUAL_TO))
	}

	private Map<String, Object> appendBodyPattern(MatchingStrategy matchingStrategy) {
		MatchingStrategy.Type type = matchingStrategy.type
		Object value= matchingStrategy.clientValue
		ContentType contentType = recognizeContentTypeFromMatchingStrategy(type)
		if (contentType == ContentType.UNKNOWN && type == MatchingStrategy.Type.EQUAL_TO) {
			contentType = recognizeContentTypeFromContent(value)
			type = getEqualsTypeFromContentType(contentType)
		}
		Map<String, ? extends Object> result = [(type.name): parseBody(value, contentType)]
		if (type == MatchingStrategy.Type.EQUAL_TO_JSON && matchingStrategy.jsonCompareMode) {
			return result << [jsonCompareMode : (matchingStrategy.jsonCompareMode.toString())]
		}
		return result
	}

	private boolean containsPattern(GString bodyAsValue) {
		return bodyAsValue.values.collect { it instanceof DslProperty ? it.clientValue : it }
								 .find { it instanceof Pattern }
	}

	private List<MatchingStrategy> extractReqexpMatching(Object responseBodyObject) {
		def matchingStrategies = new ArrayList<MatchingStrategy>()
		responseBodyObject.each { k, v ->
			if (v instanceof List) {
				v.each {
					matchingStrategies.addAll(extractReqexpMatching((Map<String, Object>)it))
				}
			} else {
				matchingStrategies.add(new MatchingStrategy(/.*${k}":.?"?${v}"?.*/, MatchingStrategy.Type.MATCHING))
			}
		}
		return matchingStrategies
	}

	private MatchingStrategy.Type getEqualsTypeFromContentTypeHeader() {
		return getEqualsTypeFromContentType(recognizeContentTypeFromHeader(request.headers))
	}

}
