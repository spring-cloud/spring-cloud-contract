package io.codearte.accurest.dsl
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.internal.ClientRequest
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.QueryParameters
import io.codearte.accurest.dsl.internal.Request

import java.util.regex.Pattern

import static io.codearte.accurest.dsl.internal.JsonStructureConverter.TEMPORARY_PATTERN_HOLDER
import static io.codearte.accurest.dsl.internal.JsonStructureConverter.convertJsonStructureToObjectUnderstandingStructure

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
		def urlPath = clientRequest?.urlPath?.clientValue
		if (urlPath) {
			return [urlPath: urlPath]
		}
		def url = clientRequest?.url?.clientValue
		return url instanceof Pattern ? [urlPattern: url.pattern()] : [url: url]
	}

	private Map<String, Object> appendQueryParameters(ClientRequest clientRequest) {
		def queryParameters = clientRequest?.urlPath?.queryParameters ?: clientRequest?.url?.queryParameters
		return queryParameters && !queryParameters.parameters.isEmpty() ?
				[queryParameters: buildUrlPathQueryParameters(queryParameters)] : [:]
	}

	private Map buildUrlPathQueryParameters(QueryParameters queryParameters) {
		return queryParameters.parameters.collectEntries { QueryParameter param ->
			parseQueryParameter(param.name, param.clientValue)
		}
	}

	protected Map parseQueryParameter(String name, MatchingStrategy matchingStrategy) {
		return buildQueryParameter(name, matchingStrategy.clientValue, matchingStrategy.type)
	}

	protected Map parseQueryParameter(String name, Object value) {
		return buildQueryParameter(name, value, MatchingStrategy.Type.EQUAL_TO)
	}

	protected Map parseQueryParameter(String name, Pattern pattern) {
		return buildQueryParameter(name, pattern.pattern(), MatchingStrategy.Type.MATCHING)
	}

	private Map buildQueryParameter(String name, Object value, MatchingStrategy.Type type) {
		if (value instanceof Pattern) {
			value = value.pattern()
		}
		return [(name): [(type.name) : value]]
	}

	private Map<String, Object> appendBody(ClientRequest clientRequest) {
		Object body = clientRequest?.body?.clientValue
		if (body == null) {
			return [:]
		}
		if (containsRegex(body)) {
			return [bodyPatterns: [[matches: parseBody(convertJsonStructureToObjectUnderstandingStructure(body,
					{ it instanceof Pattern },
					{ String json -> json.collect {
							switch(it) {
								case ('{'): return '\\{'
								case ('}'): return '\\}'
								default: return it
							}
						} .join('')
					},
					{ LinkedList list, String json ->
						return json.replaceAll(TEMPORARY_PATTERN_HOLDER, { String a, String[] b -> list.pop() })
					}
			))]]]
		}
		return [bodyPatterns: [[equalTo: parseBody(body)]]]
	}

	protected String parseBody(Object body) {
		return body
	}

	boolean containsRegex(Object bodyObject) {
		String bodyString = bodyObject as String
		return (bodyString =~ /\^.*\$/).find()
	}

	boolean containsRegex(Map map) {
		return map.values().any { it instanceof Pattern }
	}

}
