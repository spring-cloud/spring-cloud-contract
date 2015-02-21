package io.coderate.accurest.builder

import groovy.transform.PackageScope

/**
 * @author Jakub Kubrynski
 */
@PackageScope
class SpockMethodBodyBuilder {
	private final Map stubDefinition

	SpockMethodBodyBuilder(Map stubDefinition) {
		this.stubDefinition = stubDefinition
	}

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.startBlock()
		blockBuilder.addLine('given:').startBlock()
		blockBuilder.addLine('def request = given()')
		blockBuilder.indent()
		stubDefinition.request.headers.each {
			blockBuilder.addLine(".header('$it.key', '$it.value.equalTo')")
		}
		if (stubDefinition.request.bodyPatterns) {
			String matches = stubDefinition.request.bodyPatterns[0].matches
			matches = matches.replaceAll('\\\\', '');
			blockBuilder.addLine(".body('$matches')")
		}

		blockBuilder.unindent().endBlock().addEmptyLine()

		blockBuilder.addLine('when:').startBlock()
		blockBuilder.addLine('def response = given().spec(request)')
		blockBuilder.indent()
		blockBuilder.addLine(".${stubDefinition.request.method.toLowerCase()}(\"$stubDefinition.request.url\")")
		blockBuilder.unindent().endBlock().addEmptyLine()

		blockBuilder.addLine('then:').startBlock()
		blockBuilder.addLine("response.statusCode == $stubDefinition.response.status")

		stubDefinition.response.headers.each {
			blockBuilder.addLine("response.header('$it.key') == '$it.value'")
		}
		if (stubDefinition.response.body) {
			blockBuilder.addLine('def responseBody = new JsonSlurper().parseText(response.body.asString())')
			stubDefinition.response.body.each {
				def value = it.value
				if (value instanceof String) {
					if (value.startsWith('$')) {
						value = value.substring(1).replaceAll('\\$it', "responseBody.$it.key")
						blockBuilder.addLine(value)
					} else {
						blockBuilder.addLine("responseBody.$it.key == \"$value\"")
					}
				} else {
					blockBuilder.addLine("responseBody.$it.key == $value")
				}
			}
		}
		blockBuilder.endBlock()

		blockBuilder.endBlock()
	}
}
