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
		blockBuilder.startBlock().startBlock()
		stubDefinition.request.headers.each {
			blockBuilder.addLine(".header('$it.key', '$it.value.equalTo')")
		}
		if (stubDefinition.request.bodyPatterns) {
			blockBuilder.addLine('.body(\'' + stubDefinition.request.bodyPatterns[0].matches + '\')')
		}

		blockBuilder.endBlock().endBlock().endBlock().addEmptyLine()

		blockBuilder.addLine('when:').startBlock()
		blockBuilder.addLine('def response = given().spec(request)')
		blockBuilder.startBlock().startBlock()
		blockBuilder.addLine('.' + stubDefinition.request.method.toLowerCase() + '("' + stubDefinition.request.url + '")')
		blockBuilder.endBlock().endBlock().endBlock().addEmptyLine()

		blockBuilder.addLine('then:').startBlock()
		blockBuilder.addLine('response.statusCode == ' + stubDefinition.response.status)

		stubDefinition.response.headers.each {
			blockBuilder.addLine("response.header('$it.key') == '$it.value'")
		}
		if (stubDefinition.response.body) {
			blockBuilder.addLine('def responseBody = new JsonSlurper().parseText(response.body.asString())')
			new groovy.json.JsonSlurper().parseText(stubDefinition.response.body).each {
				def value = it.value
				if (value instanceof String) {
					value = '"' + value + '"'
				}
				blockBuilder.addLine('responseBody.' + it.key + ' == ' + value)
			}
		}
		blockBuilder.endBlock()

		blockBuilder.endBlock()
	}
}
