package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.QueryParameters

import java.util.regex.Pattern

import static io.codearte.accurest.config.TestFramework.JUNIT

/**
 * @author Olga Maciaszek-Sharma 
 @since 21.02.16
 */
class JaxRsClientJUnitMethodBodyBuilder extends JUnitMethodBodyBuilder {

	JaxRsClientJUnitMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void given(BlockBuilder bb) {}

	@Override
	protected void givenBlock(BlockBuilder bb) {
	}

	@Override
	protected void when(BlockBuilder bb) {
		bb.addLine("Response response = webTarget")
		bb.indent()

		appendUrlPathAndQueryParameters(bb)
		appendRequestWithRequiredResponseContentType(bb)
		appendHeaders(bb)
		appendMethodAndBody(bb)
		bb.addAtTheEnd(JUNIT.lineSuffix)

		bb.unindent()

		bb.addEmptyLine()
		bb.addLine("String responseAsString = response.readEntity(String.class);")
	}

	protected void appendUrlPathAndQueryParameters(BlockBuilder bb) {
		if (request.url) {
			bb.addLine(".path(\"$request.url.serverValue\")")
			appendQueryParams(request.url.queryParameters, bb)
		} else if (request.urlPath) {
			bb.addLine(".path(\"$request.urlPath.serverValue\")")
			appendQueryParams(request.urlPath.queryParameters, bb)
		}
	}

	private void appendQueryParams(QueryParameters queryParameters, BlockBuilder bb) {
		if (!queryParameters?.parameters) {
			return
		}
		queryParameters.parameters.findAll(this.&allowedQueryParameter).each { QueryParameter param ->
			bb.addLine(".queryParam(\"$param.name\", \"${resolveParamValue(param).toString()}\")")
		}
	}

	protected void appendMethodAndBody(BlockBuilder bb) {
		String method = request.method.serverValue.toString().toLowerCase()
		if (request.body) {
			String contentType = getHeader('Content-Type') ?: getRequestContentType().mimeType
			bb.addLine(".method(\"${method.toUpperCase()}\", entity(\"$bodyAsString\", \"$contentType\"))")
		} else {
			bb.addLine(".method(\"${method.toUpperCase()}\")")
		}
	}

	protected appendHeaders(BlockBuilder bb) {
		request.headers?.collect { Header header ->
			if (header.name == 'Content-Type' || header.name == 'Accept') return
			bb.addLine(".header(\"${header.name}\", \"${header.serverValue}\")")
		}
	}

	protected void appendRequestWithRequiredResponseContentType(BlockBuilder bb) {
		String acceptHeader = getHeader("Accept")
		if (acceptHeader) {
			bb.addLine(".request(\"$acceptHeader\")")
		} else {
			bb.addLine(".request()")
		}
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("assertThat(response.getStatus()).isEqualTo($response.status.serverValue);")
	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		response.headers?.collect { Header header ->
			processHeaderElement(bb, header.name, header.serverValue)
		}
	}

	protected String getHeader(String name) {
		return request.headers?.entries.find { it.name == name }?.serverValue
	}

	@Override
	protected String getResponseAsString() {
		return 'responseAsString'
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("assertThat(response.getHeaderString(\"$property\")).${createHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.getHeaderString(\"$property\")).${createHeaderComparison(pattern)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("response.getHeaderString(\"$property\")")};")
	}

}
