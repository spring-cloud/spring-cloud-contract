package io.codearte.accurest.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.internal.*
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.JsonPaths
import io.codearte.accurest.util.JsonToJsonPathsConverter
import io.codearte.accurest.util.MapConverter

import java.util.regex.Pattern

import static io.codearte.accurest.util.ContentUtils.extractValue
/**
 * @author Olga Maciaszek-Sharma
 * @since 2016-02-17
 */
@TypeChecked
@PackageScope
abstract class MethodBodyBuilder {

	protected abstract void validateResponseCodeBlock(BlockBuilder bb)

	protected abstract void validateResponseHeadersBlock(BlockBuilder bb)

	protected abstract String getResponseAsString()

	protected abstract String addCommentSignIfRequired(String baseString)

	protected abstract BlockBuilder addColonIfRequired(BlockBuilder blockBuilder)

	protected abstract String getResponseBodyPropertyComparisonString(String property, String value)

	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec)

	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry)

	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern)

	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec)

	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, String value)

	protected abstract String getPropertyInListString(String property, Integer index)

	protected abstract String convertUnicodeEscapesIfRequired(String json)

	protected abstract String getParsedXmlResponseBodyString(String responseString)

	protected abstract String getSimpleResponseBodyString(String responseString)

	protected abstract String getInputString()

	protected abstract String getHeaderString(Header header)

	protected abstract String getBodyString(String bodyAsString)

	protected abstract String getMultipartFileParameterContent(String propertyName, NamedProperty propertyValue)

	protected abstract String getParameterString(Map.Entry<String, Object> parameter)

	protected abstract void processInput(BlockBuilder bb)

	protected abstract void when(BlockBuilder bb)

	protected abstract void then(BlockBuilder bb)

	protected abstract ContentType getResponseContentType()

	protected abstract String getBodyAsString()

	protected abstract boolean hasGivenSection()

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.startBlock()

		if (hasGivenSection()) {
			givenBlock(blockBuilder)
		}
		whenBlock(blockBuilder)
		thenBlock(blockBuilder)

		blockBuilder.endBlock()
	}

	protected void givenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('given:'))
		bb.startBlock()
		given(bb)
		bb.endBlock().addEmptyLine()
	}

	protected void whenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('when:'))
		bb.startBlock()
		when(bb)
		bb.endBlock().addEmptyLine()
	}

	protected void thenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('then:'))
		bb.startBlock()
		then(bb)
		bb.endBlock()
	}

	protected void given(BlockBuilder bb) {
		bb.addLine(getInputString())
		bb.indent()
		processInput(bb)
		addColonIfRequired(bb)
		bb.unindent()
	}

	protected void validateResponseBodyBlock(BlockBuilder bb, Object responseBody) {
		ContentType contentType = getResponseContentType()
		if (responseBody instanceof GString) {
			responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
		}
		if (contentType == ContentType.JSON) {
			appendJsonPath(bb, getResponseAsString())
			JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(responseBody)
			jsonPaths.each {
				String method = it.method()
				String postProcessedMethod = postProcessJsonPathCall(method)
				bb.addLine("assertThatJson(parsedJson)" + postProcessedMethod)
				addColonIfRequired(bb)
			}
			processBodyElement(bb, "", responseBody)
		} else if (contentType == ContentType.XML) {
			bb.addLine(getParsedXmlResponseBodyString(getResponseAsString()))
			addColonIfRequired(bb)
			// TODO xml validation
		} else {
			bb.addLine(getSimpleResponseBodyString(getResponseAsString()))
			processText(bb, "", responseBody as String)
			addColonIfRequired(bb)
		}
	}

	protected String postProcessJsonPathCall(String jsonPath) {
		return jsonPath
	}

	protected void appendJsonPath(BlockBuilder blockBuilder, String json) {
		blockBuilder.addLine(("DocumentContext parsedJson = JsonPath.parse($json)"))
		addColonIfRequired(blockBuilder)
	}

	protected void processText(BlockBuilder blockBuilder, String property, String value) {
		if (value.startsWith('$')) {
			value = stripFirstChar(value).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(value)
			addColonIfRequired(blockBuilder)
		} else {
			blockBuilder.addLine(getResponseBodyPropertyComparisonString(property, value))
		}
	}

	private String stripFirstChar(String s) {
		return s.substring(1);
	}

	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Object value) {
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {
	}

	protected String trimRepeatedQuotes(String toTrim) {
		return toTrim.startsWith('"') ? toTrim.replaceAll('"', '') : toTrim
	}

	protected Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = MapConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.serverValue : it })
		}
		return bodyValue
	}

	protected String resolveParamValue(QueryParameter param) {
		return resolveParamValue(param.serverValue)
	}

	protected String resolveParamValue(Object value) {
		return value.toString()
	}

	protected String resolveParamValue(MatchingStrategy matchingStrategy) {
		return matchingStrategy.serverValue.toString()
	}

	protected String getTestSideValue(Object object) {
		return MapConverter.getTestSideValues(object).toString()
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map map) {
		map.each {
			processBodyElement(blockBuilder, property, it)
		}
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, List list) {
		list.eachWithIndex { listElement, listIndex ->
			String prop = getPropertyInListString(property, listIndex as Integer)
			processBodyElement(blockBuilder, prop, listElement)
		}
	}


}
