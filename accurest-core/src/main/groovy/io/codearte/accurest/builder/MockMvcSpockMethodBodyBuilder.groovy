package io.codearte.accurest.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.Header

import java.util.regex.Pattern

@PackageScope
@TypeChecked
class MockMvcSpockMethodBodyBuilder extends SpockMethodBodyBuilder {

	MockMvcSpockMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("response.statusCode == $response.status.serverValue")
	}

	@Override
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
}
