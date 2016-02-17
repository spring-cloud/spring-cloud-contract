package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.NamedProperty
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.MapConverter

import static io.codearte.accurest.util.ContentUtils.extractValue
import static io.codearte.accurest.util.ContentUtils.getMultipartFileParameterContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader

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
	protected String getMultipartParameterLine(Map.Entry<String, Object> parameter) {
		if (parameter.value instanceof NamedProperty) {
			return ".multiPart(${getMultipartFileParameterContent(parameter.key, (NamedProperty) parameter.value)})"
		}
		return ".param('$parameter.key', '$parameter.value')"
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
	protected String addColonIfRequired(String baseString) {
		return baseString
	}

	@Override
	protected String getPropertyInListString(String property, Integer listIndex) {
		"$property[$listIndex]" ?: ''
	}

	protected String convertUnicodeEscapesIfRequired(String json) {
		return StringEscapeUtils.unescapeJavaScript(json)
	}
}
