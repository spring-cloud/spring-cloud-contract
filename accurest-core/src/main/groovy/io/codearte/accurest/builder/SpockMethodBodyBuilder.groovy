package io.codearte.accurest.builder

import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.NamedProperty
import io.codearte.accurest.dsl.internal.Request

import static io.codearte.accurest.util.ContentUtils.getGroovyMultipartFileParameterContent

/**
 * @author Jakub Kubrynski
 */
@PackageScope
@TypeChecked
abstract class SpockMethodBodyBuilder extends MethodBodyBuilder {

	SpockMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, String value) {
		return "responseBody$property == \"${value}\""
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read('\\\$$property')")}")
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + "." + entry.key, entry.value)
	}

	@Override
	protected String addCommentSignIfRequired(String baseString) {
		return baseString
	}

	@Override
	protected BlockBuilder addColonIfRequired(BlockBuilder blockBuilder) {
		return blockBuilder
	}

	@Override
	protected String getPropertyInListString(String property, Integer listIndex) {
		"$property[$listIndex]" ?: ''
	}

	@Override
	protected String convertUnicodeEscapesIfRequired(String json) {
		return StringEscapeUtils.unescapeJavaScript(json)
	}

	@Override
	protected String getParsedXmlResponseBodyString(String responseString) {
		return "def responseBody = new XmlSlurper().parseText($responseString)"
	}

	@Override
	protected String getSimpleResponseBodyString(String responseString) {
		return "def responseBody = ($responseString)"
	}

	@Override
	protected String getResponseString(Request request) {
		return 'def response = given().spec(request)'
	}

	@Override
	protected String getRequestString() {
		return 'def request = given()'
	}

	@Override
	protected String getHeaderString(Header header) {
		return ".header('${getTestSideValue(header.name)}', '${getTestSideValue(header.serverValue)}')"
	}

	@Override
	protected String getBodyString(String bodyAsString) {
		return ".body('''$bodyAsString''')"
	}

	@Override
	protected String getMultipartFileParameterContent(String propertyName, NamedProperty propertyValue) {
		return getGroovyMultipartFileParameterContent(propertyName, propertyValue)
	}

	@Override
	protected String getParameterString(Map.Entry<String, Object> parameter) {
		return ".param('$parameter.key', '$parameter.value')"
	}

}
