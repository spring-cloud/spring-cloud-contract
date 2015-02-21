package io.coderate.accurest.builder

import io.coderate.accurest.config.TestFramework
import io.coderate.accurest.util.NamesUtil
import io.coderate.accurest.util.StubMappingConverter

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
		Map stubContent = StubMappingConverter.toStubMappingOnServerSide(stubsFile)
		String methodName = NamesUtil.camelCase(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
		return new MethodBuilder(methodName, stubContent, lang)
	}

	void appendTo(BlockBuilder blockBuilder) {
		if (lang == TestFramework.JUNIT) {
			blockBuilder.addLine('@Test')
		}
		blockBuilder.addLine(lang.methodModifier + "$methodName() {")
		new SpockMethodBodyBuilder(stubContent).appendTo(blockBuilder)
		blockBuilder.addLine('}')
	}
}
