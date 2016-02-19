package io.codearte.accurest.builder

import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.Request

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
	protected String getResponseAsString() {
		return "response.getBody().asString()"
	}

	@Override
	protected String addCommentSignIfRequired(String baseString) {
		return "// $baseString"
	}

	@Override
	protected BlockBuilder addColonIfRequired(BlockBuilder blockBuilder) {
		blockBuilder.addAtTheEnd(';')
		return blockBuilder
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
		String unescapedJson = StringEscapeUtils.unescapeJavaScript(json)
		return StringEscapeUtils.escapeJava(unescapedJson)
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + """.get(\\\"$entry.key\\\")""", entry.value)
	}

	@Override
	protected String getParsedXmlResponseBodyString(String responseString) {
		return "Object responseBody = new XmlSlurper().parseText($responseString);"
	}

	@Override
	protected String getSimpleResponseBodyString(String responseString) {
		return "Object responseBody = ($responseString);"
	}

	@Override
	protected String getResponseString(Request request) {
		return 'ResponseOptions response = given().spec(request)'
	}

	@Override
	protected String getRequestString() {
		return 'MockMvcRequestSpecification request = given()'
	}

	@Override
	protected String getHeaderString(Header header) {
		return ".header(\"${getTestSideValue(header.name)}\", \"${getTestSideValue(header.serverValue)}\")"
	}

	@Override
	protected String getBodyString(String bodyAsString) {
		return ".body(\"$bodyAsString\")"
	}
}
