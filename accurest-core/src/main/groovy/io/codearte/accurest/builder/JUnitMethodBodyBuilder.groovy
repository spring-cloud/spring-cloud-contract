package io.codearte.accurest.builder

import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.ExecutionProperty

/**
 * @author Jakub Kubrynski
 * @author Olga Maciaszek-Sharma
 */
@TypeChecked
@PackageScope
abstract class JUnitMethodBodyBuilder extends MethodBodyBuilder {

	JUnitMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void when(BlockBuilder bb) {

	}

	@Override
	protected String getResponseAsString() {
		return null
	}

	@Override
	protected String addCommentSignIfRequired(String baseString) {
		return "// $baseString"
	}

	@Override
	protected String addColonIfRequired(String baseString) {
		return "$baseString;"
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, String value) {
		return "assertThat(responseBody${property}).isEqualTo(\"${value}\");"
	}

	@Override
	protected String getMultipartParameterLine(Map.Entry<String, Object> parameter) {
		return null //TODO
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read(\"\\\$$property\")")};")
	}

	@Override
	protected String getPropertyInListString(String property, Integer listIndex) {
		return "${property}.get($listIndex)" ?: ''
	}

	@Override
	protected String convertUnicodeEscapesIfRequired(String json) {
		return json             // TODO: verify if that's fine or escapeJava required
	}
}
