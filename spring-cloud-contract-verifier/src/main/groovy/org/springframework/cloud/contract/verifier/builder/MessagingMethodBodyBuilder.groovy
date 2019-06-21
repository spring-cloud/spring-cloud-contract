/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.OutputMessage
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.ContentUtils

import static org.apache.commons.text.StringEscapeUtils.escapeJava
import static org.springframework.cloud.contract.verifier.util.ContentUtils.evaluateContentType

/**
 * Root class for messaging methodBuilder building.
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @author Olga Maciaszek-Sharma, codearte.io
 * @author Tim Ysewyn
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
abstract class MessagingMethodBodyBuilder extends MethodBodyBuilder {

	protected final Input inputMessage
	protected final OutputMessage outputMessage

	MessagingMethodBodyBuilder(Contract stubDefinition,
			ContractVerifierConfigProperties configProperties,
			GeneratedClassDataForMethod classDataForMethod) {
		super(configProperties, stubDefinition, classDataForMethod)
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
				else {
					bb.startBlock()
				}
				validateResponseBodyBlock(bb, outputMessage.bodyMatchers, outputMessage.body.serverValue)
			}
			if (outputMessage.assertThat) {
				bb.addLine(outputMessage.assertThat.executionCommand)
				addColonIfRequired(bb)
			}
		}
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, GString value) {
		String gstringValue = ContentUtils.
				extractValueForGString(value, ContentUtils.GET_TEST_SIDE).toString()
		processHeaderElement(blockBuilder, property, gstringValue)
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, FromFileProperty value) {
		return null
	}

	protected ContentType getResponseContentType() {
		return evaluateContentType(outputMessage?.headers,
				outputMessage?.body?.serverValue)
	}

	protected String getBodyAsString() {
		ContentType contentType = contentType()
		Object bodyValue =
				extractServerValueFromBody(contentType, inputMessage.messageBody.serverValue)
		if (bodyValue instanceof FromFileProperty) {
			FromFileProperty fileProperty = (FromFileProperty) bodyValue
			return fileProperty.isByte() ?
					indentBody(
							readBytesFromFileString(fileProperty, CommunicationType.REQUEST)) :
					indentStringBody(
							readStringFromFileString(fileProperty, CommunicationType.REQUEST))
		}
		String json = new JsonOutput().toJson(bodyValue)
		json = convertUnicodeEscapesIfRequired(json)
		return indentStringBody(trimRepeatedQuotes(json))
	}

	protected String indentBody(String text) {
		return "\n\t\t\t\t${text}\n"
	}

	protected String indentStringBody(String text) {
		return indentBody("${quotes()}${escapeJava(text)}${quotes()}")
	}

	protected String quotes() {
		return "\""
	}

}
