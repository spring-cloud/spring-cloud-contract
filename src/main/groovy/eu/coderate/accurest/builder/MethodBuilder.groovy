package eu.coderate.accurest.builder

import eu.coderate.accurest.util.NamesUtil
import groovy.json.JsonSlurper

/**
 * @author Jakub Kubrynski
 */
class MethodBuilder {

	private final String methodName
	private final Map stubContent

	private MethodBuilder(String methodName, Map stubContent) {
		this.stubContent = stubContent
		this.methodName = methodName
	}

	static MethodBuilder createTestMethod(File stubsFile) {
		Map stubContent = new JsonSlurper().parse(stubsFile)
		String methodName = NamesUtil.uncapitalize(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
		return new MethodBuilder(methodName, stubContent)
	}

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.addLine('@Test')
		blockBuilder.addLine("public void $methodName() {")
		new MethodBodyBuilder(stubContent).appendTo(blockBuilder)
		blockBuilder.addLine('}')
	}
}
