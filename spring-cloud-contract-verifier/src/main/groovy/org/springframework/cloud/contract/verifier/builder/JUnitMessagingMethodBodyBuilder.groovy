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

import java.util.regex.Pattern

import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Cookie
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern
import org.springframework.cloud.contract.spec.internal.RegexProperty
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.MapConverter

import static groovy.json.StringEscapeUtils.escapeJava
import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT

/**
 * Builds a JUnit methodBuilder for messaging
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @author Marcin Grzejszczak
 * @author Jakub Kubrynski, codearte.io
 * @author Tim Ysewyn
 *
 * @since 1.0.0
 */
@PackageScope
@TypeChecked
class JUnitMessagingMethodBodyBuilder extends MessagingMethodBodyBuilder {

	JUnitMessagingMethodBodyBuilder(Contract stubDefinition,
			ContractVerifierConfigProperties configProperties,
			GeneratedClassDataForMethod classDataForMethod) {
		super(stubDefinition, configProperties, classDataForMethod)
	}

	@Override
	protected String getInputString(Input request) {
		if (request.triggeredBy) {
			return request.triggeredBy.executionCommand
		}
		return "contractVerifierMessaging.send(inputMessage, \"${request.messageFrom.serverValue}\")"
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, Object value) {
		return ""
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, String value) {
		return ""
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, Pattern value) {
		return ""
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, ExecutionProperty value) {
		return ""
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, FromFileProperty value) {
		if (value.isByte()) {
			return "assertThat(response.getPayloadAsByteArray()).isEqualTo(" +
					readBytesFromFileString(value, CommunicationType.RESPONSE) + ")"
		}
		return getResponseBodyPropertyComparisonString(property, value.asString())
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read('\\\$$property')")}")
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property, property + "." + entry.key, entry.value)
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("assertThat(response.getHeader(\"$property\")).isNotNull();")
		blockBuilder.
				addLine("assertThat(response.getHeader(\"$property\").toString()).${createHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Number value) {
		blockBuilder.addLine("assertThat(response.getHeader(\"$property\")).isNotNull();")
		blockBuilder.
				addLine("assertThat(response.getHeader(\"$property\")).isEqualTo(${value});")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.getHeader(\"$property\")).isNotNull();")
		blockBuilder.
				addLine("assertThat(response.getHeader(\"$property\").toString()).${createHeaderComparison(pattern)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("assertThat(response.getHeader(\"$property\")).isNotNull();")
		blockBuilder.
				addLine("${exec.insertValue("response.getHeader(\"$property\").toString()")};")
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, String value) {
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, GString value) {

	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, Pattern pattern) {
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {

	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		bb.addLine("""ContractVerifierMessage response = contractVerifierMessaging.receive(${
			sentToValue(outputMessage.sentTo.serverValue)
		});""")
		bb.addLine("""assertThat(response).isNotNull();""")
		outputMessage.headers?.executeForEachHeader { Header header ->
			processHeaderElement(bb, header.name, header.serverValue instanceof NotToEscapePattern ?
					header.serverValue :
					MapConverter.getTestSideValues(header.serverValue))
		}
	}

	@Override
	protected void validateResponseCookiesBlock(BlockBuilder bb) {
	}

	private String sentToValue(Object sentTo) {
		if (sentTo instanceof ExecutionProperty) {
			return ((ExecutionProperty) sentTo).executionCommand
		}
		return '"' + sentTo.toString() + '"'
	}

	@Override
	protected String getResponseAsString() {
		return 'contractVerifierObjectMapper.writeValueAsString(response.getPayload())'
	}

	@Override
	protected String addCommentSignIfRequired(String baseString) {
		return "// $baseString"
	}

	@Override
	protected boolean shouldCommentOutBDDBlocks() {
		return true
	}

	@Override
	protected BlockBuilder addColonIfRequired(BlockBuilder blockBuilder) {
		blockBuilder.addAtTheEnd(JUNIT.lineSuffix)
		return blockBuilder
	}

	@Override
	protected Optional<String> lineSuffix() {
		return Optional.of(JUNIT.lineSuffix)
	}

	@Override
	protected String getPropertyInListString(String property, Integer listIndex) {
		return "$property[$listIndex]" ?: ''
	}

	@Override
	protected String convertUnicodeEscapesIfRequired(String json) {
		return StringEscapeUtils.unescapeJavaScript(json)
	}

	@Override
	protected String getSimpleResponseBodyString(String responseString) {
		return "Object responseBody = ($responseString);"
	}

	@Override
	protected String getInputString() {
		String request = 'ContractVerifierMessage inputMessage = contractVerifierMessaging.create('
		if (inputMessage.messageBody) {
			request = "${request}${getBodyAsString()}"
		}
		if (inputMessage.messageHeaders) {
			request = "${request}${indentHeadersString()}"
		}
		inputMessage.messageHeaders?.executeForEachHeader { Header header ->
			request = "${request}${indentedHeaderString(header)}"
		}
		return finishIndentation(request)
	}

	private String indentHeadersString() {
		return "\t\t\t\t, headers()"
	}

	private String indentedHeaderString(Header header) {
		return "\n\t\t\t\t\t\t${getHeaderString(header)}"
	}

	private String finishIndentation(String text) {
		return "${text}\n\t\t\t)"
	}

	@Override
	protected String getHeaderString(Header header) {
		return ".header(${getTestSideValue(header.name)}, ${getTestSideValue(header.serverValue)})"
	}

	@Override
	protected String getCookieString(Cookie cookie) {
		return ""
	}

	@Override
	protected String getBodyString(Object body) {
		return ""
	}

	@Override
	protected String getMultipartFileParameterContent(String propertyName, NamedProperty propertyValue) {
		return ""
	}

	@Override
	protected String getParameterString(Map.Entry<String, Object> parameter) {
		return ""
	}

	protected String convertHeaderComparison(String headerValue) {
		return ""
	}

	protected String convertHeaderComparison(Pattern headerValue) {
		return ""
	}

	protected String convertHeaderComparison(RegexProperty headerValue) {
		return convertHeaderComparison(headerValue.pattern)
	}

	protected String createHeaderComparison(Object headerValue) {
		String escapedHeader = convertUnicodeEscapesIfRequired("$headerValue")
		return "isEqualTo(\"$escapedHeader\");"
	}

	protected String createHeaderComparison(Pattern headerValue) {
		String escapedJavaHeader = escapeJava(headerValue.pattern())
		return "matches(\"$escapedJavaHeader\");"
	}
}
