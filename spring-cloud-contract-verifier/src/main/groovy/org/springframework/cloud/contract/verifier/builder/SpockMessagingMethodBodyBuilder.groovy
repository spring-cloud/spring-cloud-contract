/*
 *  Copyright 2013-2017 the original author or authors.
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

import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

import java.util.regex.Pattern
/**
 * @author Jakub Kubrynski, codearte.io
 */
@PackageScope
@TypeChecked
class SpockMessagingMethodBodyBuilder extends MessagingMethodBodyBuilder {

	SpockMessagingMethodBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties) {
		super(stubDefinition, configProperties)
	}

	@Override
	protected String getInputString(Input request) {
		if (request.triggeredBy) {
			return request.triggeredBy.executionCommand
		}
		return "contractVerifierMessaging.send(inputMessage, '${request.messageFrom.serverValue}')"
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, String value) {
		return "responseBody$property == \"${value}\""
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
	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read('\\\$$property')")}")
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property, property + "." + entry.key, entry.value)
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("response.getHeader(\'$property\')")}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("response.getHeader('$property') ${convertHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern value) {
		blockBuilder.addLine("response.getHeader('$property') ${convertHeaderComparison(value)}")
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		if (outputMessage) {
			bb.addLine("""ContractVerifierMessage response = contractVerifierMessaging.receive('${outputMessage.sentTo.serverValue}')""")
			bb.addLine("""assert response != null""")
		} else {
			bb.addLine('noExceptionThrown()')
		}
	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		outputMessage.headers?.executeForEachHeader { Header header ->
			processHeaderElement(bb, header.name, header.serverValue)
		}
	}

	@Override
	protected String getResponseAsString() {
		return 'contractVerifierObjectMapper.writeValueAsString(response.payload)'
	}

	@Override
	protected String addCommentSignIfRequired(String baseString) {
		return baseString
	}

	@Override
	protected BlockBuilder addColonIfRequired(BlockBuilder blockBuilder) {
		return blockBuilder
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, Object value) {
		return ""
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
	protected String getParsedXmlResponseBodyString(String responseString) {
		return "def responseBody = new XmlSlurper().parseText($responseString)"
	}

	@Override
	protected String getSimpleResponseBodyString(String responseString) {
		return "def responseBody = ($responseString)"
	}

	@Override
	protected String getInputString() {
		String request = 'ContractVerifierMessage inputMessage = contractVerifierMessaging.create('
		if (inputMessage.messageBody) {
			request = "${request}'''${bodyAsString}'''\n    "
		}
		if (inputMessage.messageHeaders) {
			request = "${request},[\n"
		}
		def headers = []
		inputMessage.messageHeaders?.executeForEachHeader { Header header ->
			headers << "      ${getHeaderString(header)}"
		}
		request = "${request}${headers.join(',\n')}"
		if (inputMessage.messageHeaders) {
			request = "${request}\n    ]"
		}
		return "${request})"
	}

	@Override
	protected String getHeaderString(Header header) {
		return "${getTestSideValue(header.name)}: ${getTestSideValue(header.serverValue)}"
	}

	@Override
	protected String getBodyString(String bodyAsString) {
		return ''
	}

	@Override
	protected String getMultipartFileParameterContent(String propertyName, NamedProperty propertyValue) {
		return ''
	}

	@Override
	protected String getParameterString(Map.Entry<String, Object> parameter) {
		return ''
	}

	protected String convertHeaderComparison(String headerValue) {
		return " == '$headerValue'"
	}

	protected String convertHeaderComparison(Pattern headerValue) {
		return "==~ java.util.regex.Pattern.compile('$headerValue')"
	}

	// #273 - should escape $ for Groovy since it will try to make it a GString
	@Override
	protected String postProcessJsonPathCall(String method) {
		if (templateProcessor.containsTemplateEntry(method)) {
			return method
		}
		return method.replace('$', '\\$')
	}

}
