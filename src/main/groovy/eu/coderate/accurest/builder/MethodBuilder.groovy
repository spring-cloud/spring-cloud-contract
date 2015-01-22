package eu.coderate.accurest.builder

import eu.coderate.accurest.util.NamesUtil
import groovy.json.JsonSlurper

/**
 * @author Jakub Kubrynski
 */
class MethodBuilder {

	private final String methodName
	private final Map stubContent
	private final Lang lang

	private MethodBuilder(String methodName, Map stubContent, Lang lang) {
		this.stubContent = stubContent
		this.methodName = methodName
		this.lang = lang
	}

	static MethodBuilder createTestMethod(File stubsFile, Lang lang) {
		Map stubContent = new JsonSlurper().parse(stubsFile)
		String methodName = NamesUtil.uncapitalize(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
		return new MethodBuilder(methodName, stubContent, lang)
	}

	void appendTo(BlockBuilder blockBuilder) {
		String modifier
		if (lang == Lang.JAVA) {
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
