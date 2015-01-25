package io.coderate.accurest.builder

import io.coderate.accurest.util.NamesUtil
import groovy.json.JsonSlurper

/**
 * @author Jakub Kubrynski
 */
class MethodBuilder {

	private final String methodName
	private final Map stubContent
	private final TestFramework lang

	private MethodBuilder(String methodName, Map stubContent, TestFramework lang) {
		this.stubContent = stubContent
		this.methodName = methodName
		this.lang = lang
	}

	static MethodBuilder createTestMethod(File stubsFile, TestFramework lang) {
		Map stubContent = new JsonSlurper().parse(stubsFile)
		String methodName = NamesUtil.uncapitalize(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
		return new MethodBuilder(methodName, stubContent, lang)
	}

	void appendTo(BlockBuilder blockBuilder) {
		String modifier
		if (lang == TestFramework.JUNIT) {
			blockBuilder.addLine('@Test')
			modifier = "public void "
		} else {
			modifier = "def "
		}
		blockBuilder.addLine(modifier + "$methodName() {")
		new SpockMethodBodyBuilder(stubContent).appendTo(blockBuilder)
		blockBuilder.addLine('}')
	}
}
