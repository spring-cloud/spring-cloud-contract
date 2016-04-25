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

import static io.codearte.accurest.config.TestFramework.JUNIT
/**
 * @author Jakub Kubrynski
 */
@PackageScope
@TypeChecked
class JUnitMessagingMethodBodyBuilder extends MessagingMethodBodyBuilder {

	JUnitMessagingMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected String getInputString(Input request) {
		if (request.triggeredBy) {
			return request.triggeredBy.executionCommand
		}
		return "accurestMessaging.send(inputMessage, \"${request.messageFrom.serverValue}\")"
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
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("assertThat(response.getHeader(\"$property\")).${createHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.getHeader(\"$property\")).${createHeaderComparison(pattern)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("response.getHeader(\"$property\")")};")
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {

	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		bb.addLine("""AccurestMessage response = accurestMessaging.receiveMessage("${outputMessage.sentTo.serverValue}");""")
		bb.addLine("""assertThat(response).isNotNull();""")
		outputMessage.headers?.collect { Header header ->\
			processHeaderElement(bb, header.name, header.serverValue)
		}
	}

	@Override
	protected String getResponseAsString() {
		return 'accurestObjectMapper.writeValueAsString(response.getPayload())'
	}

	@Override
	protected String addCommentSignIfRequired(String baseString) {
		return "// $baseString"
	}

	@Override
	protected BlockBuilder addColonIfRequired(BlockBuilder blockBuilder) {
		blockBuilder.addAtTheEnd(JUNIT.lineSuffix)
		return blockBuilder
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
		return "Object responseBody = new XmlSlurper().parseText($responseString);"
	}

	@Override
	protected String getSimpleResponseBodyString(String responseString) {
		return "Object responseBody = ($responseString);"
	}

	@Override
	protected String getInputString() {
		String request = 'AccurestMessage inputMessage = accurestMessaging.create('
		if (inputMessage.messageBody) {
			request = "${request}\n      \"${StringEscapeUtils.escapeJava(bodyAsString)}\"\n    "
		}
		if (inputMessage.messageHeaders) {
			request = "${request}, headers()\n"
		}
		inputMessage.messageHeaders?.collect { Header header ->
			request = "${request}      ${getHeaderString(header)}"
		}
		return "${request})"
	}

	@Override
	protected String getHeaderString(Header header) {
		return ".header(\"${getTestSideValue(header.name)}\", \"${getTestSideValue(header.serverValue)}\")"
	}

	@Override
	protected String getBodyString(String bodyAsString) {
		return ''
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
		return " == '$headerValue'"
	}

	protected String convertHeaderComparison(Pattern headerValue) {
		return "==~ java.util.regex.Pattern.compile('$headerValue')"
	}

	protected String createHeaderComparison(Object headerValue) {
		String escapedHeader = convertUnicodeEscapesIfRequired("$headerValue")
		return "isEqualTo(\"$escapedHeader\");"
	}

	protected String createHeaderComparison(Pattern headerValue) {
		String escapedHeader = convertUnicodeEscapesIfRequired("$headerValue")
		return "matches(\"$escapedHeader\");"
	}

}
