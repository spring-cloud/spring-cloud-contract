package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.ExecutionProperty
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
		response.headers?.collect { Header header ->\
			processHeaderElement(bb, header.name, header.serverValue)
		}
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("assertThat(response.header(\"$property\")).${createHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.header(\"$property\")).${createHeaderComparison(pattern)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("response.header(\"$property\")")};")
	}

}
