package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.Header

import java.util.regex.Pattern

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
		blockBuilder.startBlock()
		blockBuilder.addLine('given:').startBlock()
		blockBuilder.addLine('def request = given()')
		blockBuilder.indent()
		stubDefinition.request.headers?.collect { Header header ->
			blockBuilder.addLine(".header('${header.name}', '${header.serverValue}')")
		}
		if (stubDefinition.request.body) {
			String matches = new JsonOutput().toJson(stubDefinition.request.body.serverValue)
			blockBuilder.addLine(".body('$matches')")
		}

		blockBuilder.unindent().endBlock().addEmptyLine()

		blockBuilder.addLine('when:').startBlock()
		blockBuilder.addLine('def response = given().spec(request)')
		blockBuilder.indent()
		blockBuilder.addLine(".${stubDefinition.request.method.serverValue.toLowerCase()}(\"$stubDefinition.request.url.serverValue\")")
		blockBuilder.unindent().endBlock().addEmptyLine()

		blockBuilder.addLine('then:').startBlock()
		blockBuilder.addLine("response.statusCode == $stubDefinition.response.status.serverValue")

		stubDefinition.response.headers?.collect { Header header ->
			blockBuilder.addLine("response.header('$header.name') == '$header.serverValue'")
		}
		if (stubDefinition.response.body) {
			blockBuilder.endBlock()
			blockBuilder.addLine('and:').startBlock()
			blockBuilder.addLine('def responseBody = new JsonSlurper().parseText(response.body.asString())')
			def responseBody = stubDefinition.response.body.serverValue
			if (responseBody instanceof List) {
				processArrayElements(responseBody, "", blockBuilder)
			} else {
				processMapElement(responseBody, blockBuilder, "")
			}
		}
		blockBuilder.endBlock()

		blockBuilder.endBlock()
	}

	private void processBodyElement(BlockBuilder blockBuilder, String rootProperty, def element) {
		def value = element.value
		String property = rootProperty + "." + element.key
		if (value instanceof String) {
			if (value.startsWith('$')) {
				value = value.substring(1).replaceAll('\\$value', "responseBody$property")
				blockBuilder.addLine(value)
			} else {
				blockBuilder.addLine("responseBody$property == \"${value}\"")
			}
		} else if (value instanceof Map) {
			processMapElement(value, blockBuilder, property)
		} else if (value instanceof List) {
			processArrayElements(value, property, blockBuilder)
		} else if (value instanceof Pattern) {
			blockBuilder.addLine("responseBody$property ==~ java.util.regex.Pattern.compile('${value}')")
		} else {
			blockBuilder.addLine("responseBody$property == ${value}")
		}
	}

	private void processMapElement(def value, BlockBuilder blockBuilder, String property) {
		value.each { entry -> processBodyElement(blockBuilder, property, entry) }
	}

	private void processArrayElements(List responseBody, String property, BlockBuilder blockBuilder) {
		responseBody.eachWithIndex {
			listElement, listIndex ->
				listElement.each {
					entry -> processBodyElement(blockBuilder, property + "[$listIndex]", entry)
				}
		}
	}
	private void processClosure(Closure value, BlockBuilder blockBuilder, String property) {
		blockBuilder.addLine()
	}
}
