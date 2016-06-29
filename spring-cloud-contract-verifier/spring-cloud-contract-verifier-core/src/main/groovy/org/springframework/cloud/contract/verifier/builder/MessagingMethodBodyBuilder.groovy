/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.OutputMessage
import org.springframework.cloud.contract.verifier.util.ContentType

import static org.springframework.cloud.contract.verifier.util.ContentUtils.recognizeContentTypeFromContent
import static org.springframework.cloud.contract.verifier.util.ContentUtils.recognizeContentTypeFromHeader

/**
 * Root class for messaging method building.
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @author Olga Maciaszek-Sharma, codearte.io
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
abstract class MessagingMethodBodyBuilder extends MethodBodyBuilder {

	protected final Input inputMessage
	protected final OutputMessage outputMessage

	MessagingMethodBodyBuilder(Contract stubDefinition) {
		this.inputMessage = stubDefinition.input
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
			validateResponseHeadersBlock(bb)
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
