package eu.coderate.accurest.builder

/**
 * @author Jakub Kubrynski
 */
class MethodBuilder {

	private final String methodName
	private final String returnType

	private MethodBuilder(String returnType, String methodName) {
		this.returnType = returnType
		this.methodName = methodName
	}

	static MethodBuilder createVoidMethod(String methodName) {
		return new MethodBuilder("void", methodName)
	}

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.addLine('@Test')
		blockBuilder.addLine("public $returnType $methodName {")
		.addLine('}')
	}
}
