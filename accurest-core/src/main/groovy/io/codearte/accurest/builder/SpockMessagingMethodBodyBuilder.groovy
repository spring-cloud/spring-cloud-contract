package io.codearte.accurest.builder

import groovy.json.StringEscapeUtils
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.Input
import io.codearte.accurest.dsl.internal.NamedProperty

import java.util.regex.Pattern
/**
 * @author Jakub Kubrynski
 */
@PackageScope
@TypeChecked
class SpockMessagingMethodBodyBuilder extends MessagingMethodBodyBuilder {

	SpockMessagingMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected String getInputString(Input request) {
		if (request.triggeredBy) {
			return request.triggeredBy.executionCommand
		}
		return "accurestMessaging.send(inputMessage, '${request.messageFrom}')"
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, String value) {
		return "responseBody$property == \"${value}\""
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read('\\\$$property')")}")
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + "." + entry.key, entry.value)
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
			bb.addLine("""def response = accurestMessaging.receiveMessage('${outputMessage.sentTo}')""")
			bb.addLine("""assert response != null""")
		} else {
			bb.addLine('noExceptionThrown()')
		}
	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		outputMessage.headers?.collect { Header header ->
			processHeaderElement(bb, header.name, header.serverValue)
		}
	}

	@Override
	protected String getResponseAsString() {
		return 'accurestObjectMapper.writeValueAsString(response.payload)'
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
	protected String getPropertyInListString(String property, Integer listIndex) {
		"$property[$listIndex]" ?: ''
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
		String request = 'def inputMessage = accurestMessaging.create('
		if (inputMessage.messageBody) {
			request = "${request}'''${bodyAsString}'''\n    "
		}
		if (inputMessage.messageHeaders) {
			request = "${request},[\n"
		}
		def headers = []
		inputMessage.messageHeaders?.collect { Header header ->
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
		return "'${getTestSideValue(header.name)}': '${getTestSideValue(header.serverValue)}'"
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

}
