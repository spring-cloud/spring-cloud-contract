package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.NamedProperty
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Response
import io.codearte.accurest.dsl.internal.Url
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.MapConverter

import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader

/**
 * @author Olga Maciaszek-Sharma
 * @since 2016-02-17
 */
@TypeChecked
@PackageScope
abstract class RequestProcessingMethodBodyBuilder extends MethodBodyBuilder {

	protected final Request request
	protected final Response response

	RequestProcessingMethodBodyBuilder(GroovyDsl stubDefinition) {
		this.request = stubDefinition.request
		this.response = stubDefinition.response
	}

	protected abstract String getInputString(Request request)

	@Override
	protected boolean hasGivenSection() {
		return request.headers || request.body
	}

	protected boolean allowedQueryParameter(QueryParameter param) {
		return allowedQueryParameter(param.serverValue)
	}

	protected boolean allowedQueryParameter(MatchingStrategy matchingStrategy) {
		return matchingStrategy.type != MatchingStrategy.Type.ABSENT
	}

	protected boolean allowedQueryParameter(Object o) {
		return true
	}

	protected void processInput(BlockBuilder bb) {
		request.headers?.collect { Header header ->
			bb.addLine(getHeaderString(header))
		}
		if (request.body) {
			bb.addLine(getBodyString(bodyAsString))
		}
		if (request.multipart) {
			multipartParameters?.each { Map.Entry<String, Object> entry -> bb.addLine(getMultipartParameterLine(entry)) }
		}
	}

	protected void when(BlockBuilder bb) {
		bb.addLine(getInputString(request))
		bb.indent()

		String url = buildUrl(request)
		String method = request.method.serverValue.toString().toLowerCase()

		bb.addLine(/.${method}("$url")/)
		addColonIfRequired(bb)
		bb.unindent()
	}

	protected void then(BlockBuilder bb) {
		validateResponseCodeBlock(bb)
		if (response.headers) {
			validateResponseHeadersBlock(bb)
		}
		if (response.body) {
			bb.endBlock()
			bb.addLine(addCommentSignIfRequired('and:')).startBlock()
			validateResponseBodyBlock(bb, response.body.serverValue)
		}
	}

	protected ContentType getResponseContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(response.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(response.body.serverValue)
		}
		return contentType
	}

	protected String getBodyAsString() {
		Object bodyValue = extractServerValueFromBody(request.body.serverValue)
		String json = new JsonOutput().toJson(bodyValue)
		json = convertUnicodeEscapesIfRequired(json)
		return trimRepeatedQuotes(json)
	}

	protected Map<String, Object> getMultipartParameters() {
		return (Map<String, Object>) request?.multipart?.serverValue
	}

	protected ContentType getRequestContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(request.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(request.body.serverValue)
		}
		return contentType
	}

	protected String buildUrl(Request request) {
		if (request.url)
			return getTestSideValue(buildUrlFromUrlPath(request.url))
		if (request.urlPath)
			return getTestSideValue(buildUrlFromUrlPath(request.urlPath))
		throw new IllegalStateException("URL is not set!")
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	protected String buildUrlFromUrlPath(Url url) {
		if (hasQueryParams(url)) {
			String params = url.queryParameters.parameters
					.findAll(this.&allowedQueryParameter)
					.inject([] as List<String>) { List<String> result, QueryParameter param ->
				result << "${param.name}=${resolveParamValue(param).toString()}"
			}
			.join('&')
			return "${MapConverter.getTestSideValues(url.serverValue)}?$params"
		}
		return MapConverter.getTestSideValues(url.serverValue)
	}

	protected String getMultipartParameterLine(Map.Entry<String, Object> parameter) {
		if (parameter.value instanceof NamedProperty) {
			return ".multiPart(${getMultipartFileParameterContent(parameter.key, (NamedProperty) parameter.value)})"
		}
		return getParameterString(parameter)
	}


	private boolean hasQueryParams(Url url) {
		return url.queryParameters
	}
}
