package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Response
import io.codearte.accurest.dsl.internal.UrlPath
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.JsonConverter

import java.util.regex.Pattern

import static io.codearte.accurest.util.ContentUtils.extractValue
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent

/**
 * @author Jakub Kubrynski
 */
@PackageScope
class SpockMethodBodyBuilder {
	private final GroovyDsl stubDefinition

	SpockMethodBodyBuilder(GroovyDsl stubDefinition) {
		this.stubDefinition = stubDefinition
	}

	void appendTo(BlockBuilder blockBuilder) {
		Request request = stubDefinition.request
		Response response = stubDefinition.response
		blockBuilder.with {
			startBlock()
			addLine('given:').startBlock()
			addLine('def request = given()')
			indent()
			request.headers?.collect { Header header ->
				addLine(".header('${header.name}', '${header.serverValue}')")
			}
			if (request.body) {
				Object bodyValue = extractServerValueFromBody(request.body.serverValue)
				String matches = new JsonOutput().toJson(bodyValue)
				addLine(".body('$matches')")
			}

			unindent().endBlock().addEmptyLine()

			addLine('when:').startBlock()
			addLine('def response = given().spec(request)')
			indent()

			String url = buildUrl(request)
			String method = request.method.serverValue.toLowerCase()

			blockBuilder.addLine(/.${method}("$url")/)
			unindent().endBlock().addEmptyLine()

			addLine('then:').startBlock()
			addLine("response.statusCode == $response.status.serverValue")

			response.headers?.collect { Header header ->
				addLine("response.header('$header.name') == '$header.serverValue'")
			}
			if (response.body) {
				endBlock()
				addLine('and:').startBlock()
				def responseBody = response.body.serverValue
				ContentType contentType = recognizeContentTypeFromHeader(response.headers)
				if (contentType == ContentType.UNKNOWN) {
					contentType = recognizeContentTypeFromContent(responseBody)
				}
				if (responseBody instanceof GString) {
					responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
				}
				if (contentType == ContentType.JSON) {
					addLine('def responseBody = new JsonSlurper().parseText(response.body.asString())')
					if (responseBody instanceof List) {
						processArrayElements(responseBody, "", blockBuilder)
					} else {
						processMapElement(responseBody, blockBuilder, "")
					}
				} else if (contentType == ContentType.XML) {
					addLine('def responseBody = new XmlSlurper().parseText(response.body.asString())')
					// TODO xml validation
				}
			}
			endBlock()

			endBlock()
		}
	}

	private Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = JsonConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.serverValue : it })
		}
		return bodyValue
	}

	private String buildUrl(Request request) {
		if (request.url)
			return request.url.serverValue;
		if (request.urlPath)
			return buildUrlFromUrlPath(request.urlPath)
		throw new IllegalStateException("URL is not set!")
	}

	private String buildUrlFromUrlPath(UrlPath urlPath) {
		String params = urlPath.queryParameters.parameters.inject([]) { result, param ->
			result << "${param.name}=${URLEncoder.encode(resolveParamValue(param).toString(), "UTF8")}"
		}.join('&')
		return "$urlPath.serverValue?$params"
	}

	private String resolveParamValue(QueryParameter param) {
		resolveParamValue(param.serverValue)
	}

	private String resolveParamValue(Object value) {
		value.toString()
	}

	private String resolveParamValue(MatchingStrategy matchingStrategy) {
		matchingStrategy.serverValue.toString()
	}

	private void processBodyElement(BlockBuilder blockBuilder, String property, def value) {
		if (value instanceof String) {
			if (value.startsWith('$')) {
				value = value.substring(1).replaceAll('\\$value', "responseBody$property")
				blockBuilder.addLine(value)
			} else {
				blockBuilder.addLine("responseBody$property == \"${value}\"")
			}
		} else if (value instanceof Map) {
			processMapElement(value, blockBuilder, property)
		}else if (value instanceof Map.Entry) {
			processEntryElement(blockBuilder, property, value)
		} else if (value instanceof List) {
			processArrayElements(value, property, blockBuilder)
		} else if (value instanceof Pattern) {
			blockBuilder.addLine("responseBody$property ==~ java.util.regex.Pattern.compile('${value}')")
		} else if (value instanceof DslProperty) {
			processBodyElement(blockBuilder, property, value.serverValue)
		} else if (value instanceof ExecutionProperty) {
			ExecutionProperty exec = (ExecutionProperty) value
			blockBuilder.addLine("${exec.insertValue("responseBody$property")}")
		} else {
			blockBuilder.addLine("responseBody$property == ${value}")
		}
	}

	private void processMapElement(def value, BlockBuilder blockBuilder, String property) {
		value.each { entry -> processEntryElement(blockBuilder, property, entry) }
	}

	private def processEntryElement(BlockBuilder blockBuilder, String property, def entry) {
		return processBodyElement(blockBuilder, property + "." + entry.key, entry.value)
	}

	private void processArrayElements(List responseBody, String property, BlockBuilder blockBuilder) {
		responseBody.eachWithIndex {
			listElement, listIndex ->
				listElement.each { entry ->
					String prop = "$property[$listIndex]" ?: ''
					processBodyElement(blockBuilder, prop, entry)
				}
		}
	}
}
