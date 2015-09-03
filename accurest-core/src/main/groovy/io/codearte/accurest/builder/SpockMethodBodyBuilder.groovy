package io.codearte.accurest.builder
import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.*
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.MapConverter
import io.codearte.accurest.util.JsonToJsonPathsConverter
import io.codearte.accurest.util.JsonPaths

import static io.codearte.accurest.util.ContentUtils.*
/**
 * @author Jakub Kubrynski
 */
@PackageScope
@TypeChecked
abstract class SpockMethodBodyBuilder {

	private static final Boolean TEST_SIDE = false

	protected final Request request
	protected final Response response

	SpockMethodBodyBuilder(GroovyDsl stubDefinition) {
		this.request = stubDefinition.request
		this.response = stubDefinition.response
	}

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.startBlock()

		givenBlock(blockBuilder)
		whenBlock(blockBuilder)
		thenBlock(blockBuilder)

		blockBuilder.endBlock()
	}

	protected void thenBlock(BlockBuilder bb) {
		bb.addLine('then:')
		bb.startBlock()
		then(bb)
		bb.endBlock()
	}

	protected void whenBlock(BlockBuilder bb) {
		bb.addLine('when:')
		bb.startBlock()
		when(bb)
		bb.endBlock().addEmptyLine()
	}

	protected void givenBlock(BlockBuilder bb) {
		bb.addLine('given:')
		bb.startBlock()
		given(bb)
		bb.endBlock().addEmptyLine()
	}

	protected void given(BlockBuilder bb) {}

	protected abstract void when(BlockBuilder bb)

	protected abstract void validateResponseCodeBlock(BlockBuilder bb)

	protected abstract void validateResponseHeadersBlock(BlockBuilder bb)

	protected abstract String getResponseAsString()

	protected void then(BlockBuilder bb) {
		validateResponseCodeBlock(bb)
		if (response.headers) {
			validateResponseHeadersBlock(bb)
		}
		if (response.body) {
			bb.endBlock()
			bb.addLine('and:').startBlock()
			validateResponseBodyBlock(bb)
		}
	}

	protected void validateResponseBodyBlock(BlockBuilder bb) {
		def responseBody = response.body.serverValue
		ContentType contentType = getResponseContentType()
		if (responseBody instanceof GString) {
			responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
		}
		if (contentType == ContentType.JSON) {
			appendJsonPath(bb, responseAsString)
			JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(responseBody)
			jsonPaths.each {
				it.buildJsonPathComparison('parsedJson').each {
					bb.addLine(it)
				}
			}
			processBodyElement(bb, "", responseBody)
		} else if (contentType == ContentType.XML) {
			bb.addLine("def responseBody = new XmlSlurper().parseText($responseAsString)")
			// TODO xml validation
		}   else {
			bb.addLine("def responseBody = ($responseAsString)")
			processText(bb, "", responseBody as String)
		}
	}

	protected void processText(BlockBuilder blockBuilder, String property, String value) {
		if (value.startsWith('$')) {
			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(value)
		} else {
			blockBuilder.addLine("responseBody$property == \"${value}\"")
		}
	}

	protected String

	protected String getBodyAsString() {
		Object bodyValue = extractServerValueFromBody(request.body.serverValue)
		return trimRepeatedQuotes(new JsonOutput().toJson(bodyValue))
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

	protected boolean allowedQueryParameter(QueryParameter param) {
		return allowedQueryParameter(param.serverValue)
	}

	protected boolean allowedQueryParameter(MatchingStrategy matchingStrategy) {
		return matchingStrategy.type != MatchingStrategy.Type.ABSENT
	}

	protected boolean allowedQueryParameter(Object o) {
		return true
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

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {

	}

	protected void appendJsonPath(BlockBuilder blockBuilder, String json) {
		blockBuilder.addLine("DocumentContext parsedJson = JsonPath.parse($json)")
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read('\\\$$property')")}")
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + "." + entry.key, entry.value)
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map map) {
		map.each {
			processBodyElement(blockBuilder, property, it)
		}
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, List list) {
		list.eachWithIndex { listElement, listIndex ->
			String prop = "$property[$listIndex]" ?: ''
			processBodyElement(blockBuilder, prop, listElement)
		}
	}

	protected ContentType getRequestContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(request.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(request.body.serverValue)
		}
		return contentType
	}

	protected ContentType getResponseContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(response.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(response.body.serverValue)
		}
		return contentType
	}

	protected String getTestSideValue(Object object) {
		return MapConverter.getClientOrServerSideValues(object, TEST_SIDE).toString()
	}
}
