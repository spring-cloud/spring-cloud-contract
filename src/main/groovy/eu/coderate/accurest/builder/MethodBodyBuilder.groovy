package eu.coderate.accurest.builder

/**
 * @author Jakub Kubrynski
 */
class MethodBodyBuilder {
	private final Map stubDefinition

	MethodBodyBuilder(Map stubDefinition) {
		this.stubDefinition = stubDefinition
	}

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.startBlock()
		blockBuilder.addLine("//" + stubDefinition.request.method)
		blockBuilder.addLine("//" + stubDefinition.response.status)
		blockBuilder.endBlock()
	}
}
