package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.Header

import java.util.regex.Pattern

/**
 * @author Olga Maciaszek-Sharma
 * @since 2016-02-17
 */
class MockMvcJUnitMethodBodyBuilder extends JUnitMethodBodyBuilder {

	MockMvcJUnitMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("assertThat(response.statusCode()).isEqualTo($response.status.serverValue);")
	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		response.headers?.collect { Header header ->
			bb.addLine("assertThat(response.header(\"$header.name\")).${createHeaderComparison(header.serverValue)}")
		}
	}

	private String createHeaderComparison(Object headerValue) {
		String escapedHeader = convertUnicodeEscapesIfRequired("$headerValue")
		return "isEqualTo(\"$escapedHeader\");"
	}

	private String createHeaderComparison(Pattern headerValue) {
		String escapedHeader = convertUnicodeEscapesIfRequired("$headerValue")
		return "matches(\"$escapedHeader\");"
	}
}
