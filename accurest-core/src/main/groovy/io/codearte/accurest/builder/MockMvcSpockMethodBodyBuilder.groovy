package io.codearte.accurest.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Url
import io.codearte.accurest.util.MapConverter

import java.util.regex.Pattern

@PackageScope
@TypeChecked
class MockMvcSpockMethodBodyBuilder extends SpockMethodBodyBuilder {

	MockMvcSpockMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	protected void given(BlockBuilder bb) {
		bb.addLine('def request = given()')
		bb.indent()
		request.headers?.collect { Header header ->
			bb.addLine(".header('${getTestSideValue(header.name)}', '${getTestSideValue(header.serverValue)}')")
		}
		if (request.body) {
			bb.addLine(".body('''$bodyAsString''')")
		}
		if (request.multipart) {
			multipartParameters.each { entry -> bb.addLine(getMultipartParameterLine(entry)) }
		}
		bb.unindent()
	}

	protected void when(BlockBuilder bb) {
		bb.addLine('def response = given().spec(request)')
		bb.indent()

		String url = buildUrl(request)
		String method = request.method.serverValue.toString().toLowerCase()

		bb.addLine(/.${method}("$url")/)
		bb.unindent()
	}

	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("response.statusCode == $response.status.serverValue")
	}

	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		response.headers?.collect { Header header ->
			bb.addLine("response.header('$header.name') ${convertHeaderComparison(header.serverValue)}")
		}
	}

	private String convertHeaderComparison(Object headerValue) {
		return " == '$headerValue'"
	}

	private String convertHeaderComparison(Pattern headerValue) {
		return "==~ java.util.regex.Pattern.compile('$headerValue')"
	}

	@Override
	protected String getResponseAsString() {
		return 'response.body.asString()'
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

	private boolean hasQueryParams(Url url) {
		return url.queryParameters
	}
}
