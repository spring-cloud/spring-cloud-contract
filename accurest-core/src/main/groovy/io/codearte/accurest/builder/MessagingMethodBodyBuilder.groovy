package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.Input
import io.codearte.accurest.dsl.internal.OutputMessage
import io.codearte.accurest.util.ContentType

import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader
/**
 * @author Olga Maciaszek-Sharma
 * @since 2016-02-17
 */
@TypeChecked
@PackageScope
abstract class MessagingMethodBodyBuilder extends MethodBodyBuilder {

	protected final Input inputMessage
	protected final OutputMessage outputMessage

	MessagingMethodBodyBuilder(GroovyDsl stubDefinition) {
		this.inputMessage = stubDefinition.inputMessage
		this.outputMessage = stubDefinition.outputMessage
	}

	protected abstract String getInputString(Input request)

	@Override
	protected boolean hasGivenSection() {
		return !inputMessage.triggeredBy
	}

	protected void processInput(BlockBuilder bb) {

	}

	protected void when(BlockBuilder bb) {
		bb.addLine(getInputString(inputMessage))
		bb.indent()

		addColonIfRequired(bb)
		bb.unindent()
	}

	protected void then(BlockBuilder bb) {
		validateResponseCodeBlock(bb)
		if (inputMessage.assertThat) {
			bb.addLine(inputMessage.assertThat.executionCommand)
			addColonIfRequired(bb)
		}
		if (outputMessage) {
			if (outputMessage.headers) {
				validateResponseHeadersBlock(bb)
			}
			if (outputMessage.body) {
				bb.endBlock()
				if (outputMessage.headers) {
					bb.addLine(addCommentSignIfRequired('and:')).startBlock()
				}
				validateResponseBodyBlock(bb, outputMessage.body.serverValue)
			}
			if (outputMessage.assertThat) {
				bb.addLine(outputMessage.assertThat.executionCommand)
				addColonIfRequired(bb)
			}
		}
	}

	protected ContentType getResponseContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(outputMessage.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(outputMessage.body.serverValue)
		}
		return contentType
	}

	protected String getBodyAsString() {
		Object bodyValue = extractServerValueFromBody(inputMessage.messageBody.serverValue)
		String json = new JsonOutput().toJson(bodyValue)
		json = convertUnicodeEscapesIfRequired(json)
		return trimRepeatedQuotes(json)
	}

}
