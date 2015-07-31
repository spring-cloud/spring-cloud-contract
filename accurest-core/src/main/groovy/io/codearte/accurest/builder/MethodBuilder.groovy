package io.codearte.accurest.builder

import groovy.util.logging.Slf4j
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.config.TestMode
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.util.NamesUtil

/**
 * @author Jakub Kubrynski
 */
@Slf4j
class MethodBuilder {

	private final String methodName
	private final GroovyDsl stubContent
	private final AccurestConfigProperties configProperties

	private MethodBuilder(String methodName, GroovyDsl stubContent, AccurestConfigProperties configProperties) {
		this.stubContent = stubContent
		this.methodName = methodName
		this.configProperties = configProperties
	}

	static MethodBuilder createTestMethod(File stubsFile, AccurestConfigProperties configProperties) {
		log.debug("Stub content from file [${stubsFile.text}]")
		GroovyDsl stubContent = new GroovyShell(this.classLoader).evaluate(stubsFile)
		log.debug("Stub content Groovy DSL [$stubContent]")
		String methodName = NamesUtil.camelCase(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
		return new MethodBuilder(methodName, stubContent, configProperties)
	}

	void appendTo(BlockBuilder blockBuilder) {
		if (configProperties.targetFramework == TestFramework.JUNIT) {
			blockBuilder.addLine('@Test')
		}
		blockBuilder.addLine(configProperties.targetFramework.methodModifier + "$methodName() {")
		getMethodBodyBuilder().appendTo(blockBuilder)
		blockBuilder.addLine('}')
	}

	private SpockMethodBodyBuilder getMethodBodyBuilder() {
		if (configProperties.testMode == TestMode.JAXRSCLIENT) {
			return new JaxRsClientSpockMethodBodyBuilder(stubContent)
		}
		return new MockMvcSpockMethodBodyBuilder(stubContent)
	}

}
