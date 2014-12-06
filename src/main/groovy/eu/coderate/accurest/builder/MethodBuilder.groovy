package eu.coderate.accurest.builder

import eu.coderate.accurest.util.NamesUtil
import groovy.json.JsonSlurper

/**
 * @author Jakub Kubrynski
 */
class MethodBuilder {

	private final String methodName
	private final String returnType
	private final Map stubContent

	private MethodBuilder(String returnType, String methodName, Map stubContent) {
		this.stubContent = stubContent
		this.returnType = returnType
		this.methodName = methodName
	}

	static MethodBuilder createVoidMethod(File stubsFile) {
		Map stubContent = new JsonSlurper().parse(stubsFile)
		String methodName = NamesUtil.uncapitalize(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
		return new MethodBuilder("void", methodName, stubContent)
	}

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.addLine('@Test')
		blockBuilder.addLine("public $returnType $methodName() {")
		new MethodBodyBuilder(stubContent).appendTo(blockBuilder)
		blockBuilder.addLine('}')
	}
}
