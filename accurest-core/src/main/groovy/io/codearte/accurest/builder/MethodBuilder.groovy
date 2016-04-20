package io.codearte.accurest.builder

import groovy.util.logging.Slf4j
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.config.TestMode
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.file.Contract
import io.codearte.accurest.util.NamesUtil
/**
 * @author Jakub Kubrynski
 */
@Slf4j
class MethodBuilder {

	private final String methodName
	private final GroovyDsl stubContent
	private final AccurestConfigProperties configProperties
	private final boolean ignored

	private MethodBuilder(String methodName, GroovyDsl stubContent, AccurestConfigProperties configProperties, boolean ignored) {
		this.ignored = ignored
		this.stubContent = stubContent
		this.methodName = methodName
		this.configProperties = configProperties
	}

	static MethodBuilder createTestMethod(Contract contract, File stubsFile, GroovyDsl stubContent, AccurestConfigProperties configProperties) {
		log.debug("Stub content Groovy DSL [$stubContent]")
		String methodName = NamesUtil.camelCase(NamesUtil.toLastDot(NamesUtil.afterLast(stubsFile.path, File.separator)))
		return new MethodBuilder(methodName, stubContent, configProperties, contract.ignored)
	}

	void appendTo(BlockBuilder blockBuilder) {
		if (configProperties.targetFramework == TestFramework.JUNIT) {
			blockBuilder.addLine('@Test')
		}
		if (ignored) {
			blockBuilder.addLine('@Ignore')
		}
		blockBuilder.addLine(configProperties.targetFramework.methodModifier + "validate_$methodName() throws Exception {")
		getMethodBodyBuilder().appendTo(blockBuilder)
		blockBuilder.addLine('}')
	}

	private MethodBodyBuilder getMethodBodyBuilder() {
		if (stubContent.inputMessage || stubContent.outputMessage) {
			if (configProperties.targetFramework == TestFramework.JUNIT){
				return new JUnitMessagingMethodBodyBuilder(stubContent)
			}
			return new SpockMessagingMethodBodyBuilder(stubContent)
		}
		if (configProperties.testMode == TestMode.MOCKMVC && configProperties.targetFramework == TestFramework.JUNIT){
				return new MockMvcJUnitMethodBodyBuilder(stubContent)
		}
		if (configProperties.testMode == TestMode.JAXRSCLIENT) {
			if (configProperties.targetFramework == TestFramework.JUNIT){
				return new JaxRsClientJUnitMethodBodyBuilder(stubContent)
			}
			return new JaxRsClientSpockMethodRequestProcessingBodyBuilder(stubContent)
		}
		return new MockMvcSpockMethodRequestProcessingBodyBuilder(stubContent)
	}

}
