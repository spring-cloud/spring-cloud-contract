package io.coderate.accurest.builder
import groovy.json.JsonOutput
import groovy.transform.PackageScope
import io.coderate.accurest.dsl.GroovyDsl
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
		stubDefinition.request.headers.headers.collectEntries { [(it.name): it.serverValue] }.each { Map.Entry entry ->
			blockBuilder.addLine(".header('${entry.key}', '${entry.value}')")
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

		stubDefinition.response.headers?.headers?.collectEntries { [(it.name): it.serverValue] }?.each { Map.Entry entry ->
			blockBuilder.addLine("response.header('$entry.key') == '$entry.value'")
		}
		if (stubDefinition.response.body) {
			blockBuilder.endBlock()
			blockBuilder.addLine('and:').startBlock()
			blockBuilder.addLine('def responseBody = new JsonSlurper().parseText(response.body.asString())')
			stubDefinition.response.body.serverValue.each {
				processBodyElement(blockBuilder, "", it)
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
			value.each { entry -> processBodyElement(blockBuilder, property, entry) }
		} else {
			blockBuilder.addLine("responseBody$property == ${value}")
		}
	}
}
