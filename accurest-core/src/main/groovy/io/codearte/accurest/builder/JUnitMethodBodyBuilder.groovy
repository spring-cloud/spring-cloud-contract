package io.codearte.accurest.builder

import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.NamedProperty
import io.codearte.accurest.dsl.internal.Request

import java.util.regex.Pattern

import static groovy.json.StringEscapeUtils.escapeJava
import static io.codearte.accurest.config.TestFramework.JUNIT
import static io.codearte.accurest.util.ContentUtils.getJavaMultipartFileParameterContent

/**
 * @author Jakub Kubrynski
 * @author Olga Maciaszek-Sharma
 */
@TypeChecked
@PackageScope
abstract class JUnitMethodBodyBuilder extends RequestProcessingMethodBodyBuilder {

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
		blockBuilder.addAtTheEnd(JUNIT.lineSuffix)
		return blockBuilder
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, String value) {
		return "assertThat(responseBody${property}).isEqualTo(\"${value}\")"
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
		return escapeJava(unescapedJson)
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + getMapKeyReferenceString(entry), entry.value)
	}

	private String getMapKeyReferenceString(Map.Entry entry) {
		if (entry.value instanceof ExecutionProperty) {
			return "." + entry.key
		}
		return """.get(\\\"$entry.key\\\")"""
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
	protected String getInputString(Request request) {
		return 'ResponseOptions response = given().spec(request)'
	}

	@Override
	protected String getInputString() {
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

	@Override
	protected String getMultipartFileParameterContent(String propertyName, NamedProperty propertyValue) {
		return getJavaMultipartFileParameterContent(propertyName, propertyValue)
	}

	@Override
	protected String getParameterString(Map.Entry<String, Object> parameter) {
		return """.param("${escapeJava(parameter.key)}", "${escapeJava(parameter.value as String)}")"""
	}

	protected String createHeaderComparison(Object headerValue) {
		String escapedHeader = convertUnicodeEscapesIfRequired("$headerValue")
		return "isEqualTo(\"$escapedHeader\");"
	}

	protected String createHeaderComparison(Pattern headerValue) {
		String escapedHeader = convertUnicodeEscapesIfRequired("$headerValue")
		return "matches(\"$escapedHeader\");"
	}

}
