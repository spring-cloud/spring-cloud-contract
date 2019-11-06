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

import java.nio.charset.Charset
import java.util.regex.Pattern

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.apache.commons.text.StringEscapeUtils

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractTemplate
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.Cookie
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.OptionalProperty
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.spec.internal.RegexProperty
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor
import org.springframework.cloud.contract.verifier.template.TemplateProcessor
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.ContentUtils
import org.springframework.cloud.contract.verifier.util.MapConverter
import org.springframework.util.StringUtils

import static org.springframework.cloud.contract.verifier.util.ContentType.DEFINED
import static org.springframework.cloud.contract.verifier.util.ContentType.FORM
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON
import static org.springframework.cloud.contract.verifier.util.ContentType.TEXT
import static org.springframework.cloud.contract.verifier.util.ContentType.XML
import static org.springframework.cloud.contract.verifier.util.ContentUtils.extractValue

/**
 * Main class for building method body.
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
abstract class MethodBodyBuilder implements ClassVerifier {

	private static final Closure GET_SERVER_VALUE = {
		it instanceof DslProperty ? it.serverValue : it
	}

	protected final ContractVerifierConfigProperties configProperties
	protected final TemplateProcessor templateProcessor
	protected final ContractTemplate contractTemplate
	protected final Contract contract
	protected final GeneratedClassDataForMethod classDataForMethod
	private final JsonBodyVerificationBuilder jsonBodyVerificationBuilder
	private final XmlBodyVerificationBuilder xmlBodyVerificationBuilder

	protected MethodBodyBuilder(ContractVerifierConfigProperties configProperties,
			Contract contract,
			GeneratedClassDataForMethod classDataForMethod) {
		this.configProperties = configProperties
		this.templateProcessor = processor()
		this.contractTemplate = template()
		this.contract = contract
		this.classDataForMethod = classDataForMethod
		this.jsonBodyVerificationBuilder = new JsonBodyVerificationBuilder(this.configProperties,
				templateProcessor, contractTemplate, this.contract,
				lineSuffix(), { String jsonPath ->
			postProcessJsonPathCall(jsonPath)
				})
		this.xmlBodyVerificationBuilder = new XmlBodyVerificationBuilder(contract,
				lineSuffix())
	}

	private String byteBodyToAFileForTestMethod(FromFileProperty property, CommunicationType side) {
		String newFileName = this.classDataForMethod.methodName + "_" + side.name().toLowerCase() + "_" + property.fileName()
		File newFile = new File(this.classDataForMethod.testClassPath().parent.toFile(), newFileName)
		// for IDE
		newFile.bytes = property.asBytes()
		// for plugin
		generatedTestResourcesFileBytes(property, newFile)
		return newFileName
	}

	private void generatedTestResourcesFileBytes(FromFileProperty property, File newFile) {
		java.nio.file.Path relativePath = this.configProperties.generatedTestSourcesDir.
				toPath().relativize(newFile.toPath())
		File newFileInGeneratedTestSources = new File(this.configProperties.generatedTestResourcesDir, relativePath.
				toString())
		newFileInGeneratedTestSources.parentFile.mkdirs()
		newFileInGeneratedTestSources.bytes = property.asBytes()
	}

	protected String readBytesFromFileString(FromFileProperty property, CommunicationType side) {
		String fileName = byteBodyToAFileForTestMethod(property, side)
		return "fileToBytes(this, \"${fileName}\")"
	}

	protected String readStringFromFileString(FromFileProperty property, CommunicationType side) {
		if (property.charset != Charset.defaultCharset().toString()) {
			return "new String(" + readBytesFromFileString(property, side) + ", \"" + property.charset + "\")"
		}
		return "new String(" + readBytesFromFileString(property, side) + ")"
	}

	private TemplateProcessor processor() {
		return new HandlebarsTemplateProcessor()
	}

	private ContractTemplate template() {
		return new HandlebarsTemplateProcessor()
	}

	protected ContentType getRequestContentType() {
		return ContentType.UNKNOWN
	}

	protected String quotedAndEscaped(String string) {
		return '"' + StringEscapeUtils.escapeJava(string) + '"'
	}

	/**
	 * Builds the response body validation code block
	 */
	protected abstract void validateResponseCodeBlock(BlockBuilder bb)

	/**
	 * Builds the response headers validation code block
	 */
	protected abstract void validateResponseHeadersBlock(BlockBuilder bb)

	/**
	 * Builds the response cookies validation code block
	 */
	protected abstract void validateResponseCookiesBlock(BlockBuilder bb)

	/**
	 * Builds the code that returns response in the string format
	 */
	protected abstract String getResponseAsString()

	/**
	 * @return the given string with comment sign if required by the given implementation
	 */
	protected abstract String addCommentSignIfRequired(String baseString)

	/**
	 * @return true if the BDD-syntax blocks should be commented out for a given framework
	 */
	protected abstract boolean shouldCommentOutBDDBlocks()

	/**
	 * Adds a colon sign at the end of each line if necessary
	 */
	protected abstract BlockBuilder addColonIfRequired(BlockBuilder blockBuilder)

	/**
	 * @return line suffix appropriate for test builder if required
	 */
	protected abstract Optional<String> lineSuffix()

	/**
	 * Builds the code that for the given {@code property} will compare it to
	 * the given Object {@code value}
	 */
	protected abstract String getResponseBodyPropertyComparisonString(String property, Object value)

	/**
	 * Builds the code that for the given {@code property} will compare it to
	 * the given byte[] {@code value}
	 */
	protected abstract String getResponseBodyPropertyComparisonString(String property, FromFileProperty value)

	/**
	 * Builds the code that for the given {@code property} will compare it to
	 * the given String {@code value}
	 */
	protected abstract String getResponseBodyPropertyComparisonString(String property, String value)

	/**
	 * Builds the code that for the given {@code property} will match it to
	 * the given regular expression {@code value}
	 */
	protected abstract String getResponseBodyPropertyComparisonString(String property, Pattern value)

	/**
	 * Builds the code that for the given {@code property} will match it to
	 * the given {@link ExecutionProperty} value
	 */
	protected abstract String getResponseBodyPropertyComparisonString(String property, ExecutionProperty value)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header path
	 */
	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header path
	 */
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, RegexProperty regexProperty) {
		processHeaderElement(blockBuilder, property, regexProperty.pattern)
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header path
	 */
	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header path
	 */
	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, String value)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header path
	 */
	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, GString value)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header path
	 */
	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, Number value)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given cookie path
	 */
	protected abstract void processCookieElement(BlockBuilder blockBuilder, String key, Pattern pattern)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given cookie path
	 */
	protected abstract void processCookieElement(BlockBuilder blockBuilder, String key, String value)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given cookie path
	 */
	protected abstract void processCookieElement(BlockBuilder blockBuilder, String key, GString value)

	/**
	 * Appends to the {@link BlockBuilder} the code to retrieve a value for a property
	 * from the list with the given index
	 */
	protected abstract String getPropertyInListString(String property, Integer index)

	protected abstract String convertUnicodeEscapesIfRequired(String json)

	/**
	 * Builds the code that returns String from a body that is plain text
	 */
	protected abstract String getSimpleResponseBodyString(String responseString)

	/**
	 * Builds the code that returns the "message". For messaging it will be an input
	 * message. For REST it will be an input request.
	 */
	protected abstract String getInputString()

	/**
	 * Builds the code to append a header to the request / message
	 */
	protected abstract String getHeaderString(Header header)

	/**
	 * Builds the code to append a cookie to the request / message
	 */
	protected abstract String getCookieString(Cookie cookie)

	/**
	 * Builds the code to append body to the request / message
	 */
	protected abstract String getBodyString(Object body)

	/**
	 * Builds the code to append multipart content to the request.
	 * Not applicable for messaging.
	 */
	protected abstract String getMultipartFileParameterContent(String propertyName, NamedProperty propertyValue)

	/**
	 * Builds the code to append a form parameter to the request.
	 * Not applicable for messaging.
	 */
	protected abstract String getParameterString(Map.Entry<String, Object> parameter)

	/**
	 * Appends the code to {@link BlockBuilder} for initial request processing
	 */
	protected abstract void processInput(BlockBuilder bb)

	/**
	 * Appends the code to {@link BlockBuilder} for the {@code //when} block
	 */
	protected abstract void when(BlockBuilder bb)

	/**
	 * Appends the code to {@link BlockBuilder} for the {@code //then} block
	 */
	protected abstract void then(BlockBuilder bb)

	/**
	 * @return a{@link org.springframework.cloud.contract.verifier.util.ContentType} for the given request
	 */
	protected abstract ContentType getResponseContentType()

	/**
	 * Converts the body into String format
	 */
	protected abstract String getBodyAsString()

	/**
	 * @return {@code true} if given section should be created
	 */
	protected abstract boolean hasGivenSection()

	/**
	 * Post processing of each JSON path entry
	 */
	protected String postProcessJsonPathCall(String jsonPath) {
		return jsonPath
	}

	/**
	 * Builds the test contents and appends them to {@link BlockBuilder}
	 */
	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.startBlock()

		if (hasGivenSection()) {
			givenBlock(blockBuilder)
		}
		whenBlock(blockBuilder)
		thenBlock(blockBuilder)

		blockBuilder.endBlock()
	}

	/**
	 * Prepares the {@code given} block section together with comments and indents
	 */
	protected void givenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('given:'))
		bb.startBlock()
		given(bb)
		bb.endBlock().addEmptyLine()
	}

	/**
	 * Prepares the {@code when} block section together with comments and indents
	 */
	protected void whenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('when:'))
		bb.startBlock()
		when(bb)
		bb.endBlock().addEmptyLine()
	}

	/**
	 * Prepares the {@code then} block section together with comments and indents
	 */
	protected void thenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('then:'))
		bb.startBlock()
		then(bb)
		bb.endBlock()
	}

	/**
	 * Builds the {@code given} block section together with comments and indents
	 */
	protected void given(BlockBuilder bb) {
		bb.addLine(getInputString())
		bb.indent()
		processInput(bb)
		addColonIfRequired(bb)
		bb.unindent()
	}

	/**
	 * Builds the response body verification part. The code will differ depending on the
	 * ContentType, type of response etc. The result will be appended to {@link BlockBuilder}
	 */
	protected void validateResponseBodyBlock(BlockBuilder bb, BodyMatchers bodyMatchers, Object responseBody) {
		ContentType contentType = getResponseContentType()
		Object convertedResponseBody = responseBody
		if (convertedResponseBody instanceof FromFileProperty) {
			if (convertedResponseBody.isByte()) {
				byteResponseBodyCheck(bb, convertedResponseBody)
				return
			}
			convertedResponseBody = convertedResponseBody.asString()
		}
		if (convertedResponseBody instanceof GString) {
			convertedResponseBody =
					extractValue(convertedResponseBody as GString, contentType, { Object o -> o instanceof DslProperty ? o.serverValue : o })
		}
		if (TEXT != contentType && FORM != contentType && DEFINED != contentType) {
			boolean dontParseStrings = contentType == JSON && convertedResponseBody instanceof Map
			Closure parsingClosure = dontParseStrings ? Closure.IDENTITY : MapConverter.JSON_PARSING_CLOSURE
			convertedResponseBody = MapConverter.
					getTestSideValues(convertedResponseBody, parsingClosure)
		}
		else {
			convertedResponseBody = StringEscapeUtils.
					escapeJava(convertedResponseBody.toString())
		}
		if (JSON == contentType) {
			addJsonBodyVerification(bb, convertedResponseBody, bodyMatchers)
		}
		else if (XML == contentType) {
			xmlBodyVerificationBuilder.addXmlResponseBodyCheck(bb, convertedResponseBody,
					bodyMatchers, getResponseAsString(), shouldCommentOutBDDBlocks())
		}
		else {
			simpleTextResponseBodyCheck(bb, convertedResponseBody)
		}
	}


	private void addJsonBodyVerification(BlockBuilder bb, Object responseBody, BodyMatchers bodyMatchers) {
		Object convertedResponseBody = jsonBodyVerificationBuilder
				.addJsonResponseBodyCheck(bb, responseBody,
				bodyMatchers, getResponseAsString(), shouldCommentOutBDDBlocks())
		if (!(convertedResponseBody instanceof Map || convertedResponseBody instanceof List)) {
			simpleTextResponseBodyCheck(bb, convertedResponseBody)
		}
		processBodyElement(bb, "", "", convertedResponseBody)
	}

	private void simpleTextResponseBodyCheck(BlockBuilder bb, convertedResponseBody) {
		bb.addLine(getSimpleResponseBodyString(getResponseAsString()))
		processText(bb, "", convertedResponseBody)
		addColonIfRequired(bb)
	}

	protected String trailingKey(String key) {
		if (key.startsWith(".")) {
			return key.substring(1)
		}
		return key
	}

	private String wrappedWithBracketsForDottedProp(String key) {
		String remindingKey = trailingKey(key)
		if (remindingKey.contains(".")) {
			return "['${remindingKey}']"
		}
		return remindingKey
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header path
	 */
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Object value) {
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given cookie
	 */
	protected void processCookieElement(BlockBuilder blockBuilder, String key, Object value) {
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected void processBodyElement(BlockBuilder blockBuilder, String oldProp, String property, Object value) {
		String propDiff = property - oldProp
		String prop = wrappedWithBracketsForDottedProp(propDiff)
		String mergedProp = StringUtils.hasText(property) ? "${oldProp}.${prop}" : ""
		processBodyElement(blockBuilder, mergedProp, value)
	}

	/**
	 * Removes unnecessary quotes
	 */
	protected String trimRepeatedQuotes(String toTrim) {
		if (toTrim.startsWith('"')) {
			return toTrim.replaceAll('"', '')
			//#261
		}
		else if (toTrim.startsWith('\\"') && toTrim.endsWith('\\"')) {
			return toTrim.substring(2, toTrim.length() - 2)
		}
		return toTrim
	}

	/**
	 * Converts the passed body into ints server side representation. All {@link DslProperty}
	 * will return their server side values
	 */
	protected Object extractServerValueFromBody(ContentType contentType, Object bodyValue) {
		if (bodyValue instanceof GString) {
			return extractValue(bodyValue, contentType, GET_SERVER_VALUE)
		}
		boolean dontParseStrings = contentType == JSON && bodyValue instanceof Map
		Closure parsingClosure = dontParseStrings ? Closure.IDENTITY : MapConverter.JSON_PARSING_CLOSURE
		return MapConverter.transformValues(bodyValue, GET_SERVER_VALUE, parsingClosure)
	}

	protected ContentType contentType() {
		Headers headers = this.contract.request?.headers ?: this.contract.input?.messageHeaders
		return ContentUtils.recognizeContentTypeFromTestHeader(headers)
	}

	/**
	 * Converts the {@link org.springframework.cloud.contract.spec.internal.QueryParameter} server side value into its String
	 * representation
	 */
	protected String resolveParamValue(QueryParameter param) {
		return resolveParamValue(param.serverValue)
	}

	/**
	 * Converts the query parameter value into String
	 */
	protected String resolveParamValue(Object value) {
		return value.toString()
	}

	/**
	 * Converts the query parameter DSL Property until a concrete value is reached
	 */
	protected String resolveParamValue(OptionalProperty value) {
		return resolveParamValue(value.optionalPattern())
	}

	/**
	 * Converts the query parameter value into String
	 */
	protected String resolveParamValue(MatchingStrategy matchingStrategy) {
		return matchingStrategy.serverValue.toString()
	}

	/**
	 * Depending on the object type extracts the test side values and
	 * combines them into a String representation
	 */
	protected String getTestSideValue(Object object) {
		return '"' + MapConverter.getTestSideValues(object).toString() + '"'
	}

	/**
	 * Depending on the object type extracts the test side values and
	 * combines them into a String representation. Unlike the body transformation
	 * done via {@link MethodBodyBuilder#getTestSideValue(java.lang.Object)} will
	 * not try to guess the type of the value of the header (e.g. if it's a JSON).
	 */
	protected String getTestSideForNonBodyValue(Object object) {
		if (object instanceof ExecutionProperty) {
			return getTestSideValue((ExecutionProperty) object)
		}
		return quotedAndEscaped(MapConverter.getTestSideValuesForNonBody(object).toString())
	}

	/**
	 * Extracts the executable test side values and
	 * @return the code of the executable
	 */
	protected String getTestSideValue(ExecutionProperty executionProperty) {
		return executionProperty.toString()
	}


	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map map) {
		map.each {
			processBodyElement(blockBuilder, property, it)
		}
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected void processBodyElement(BlockBuilder blockBuilder, String property, List list) {
		list.eachWithIndex { listElement, listIndex ->
			String prop = getPropertyInListString(property, listIndex as Integer)
			processBodyElement(blockBuilder, property, prop, listElement)
		}
	}

	/**
	 * Appends to {@link BlockBuilder} processing of the given String value.
	 */
	protected void processText(BlockBuilder blockBuilder, String property, Object value) {
		if (value instanceof String && (value as String).startsWith('$')) {
			String newValue = stripFirstChar((value as String)).
					replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(newValue)
			addColonIfRequired(blockBuilder)
		}
		else {
			blockBuilder.addLine(getResponseBodyPropertyComparisonString(property, value))
		}
	}

	private void byteResponseBodyCheck(BlockBuilder bb,
			FromFileProperty convertedResponseBody) {
		processText(bb, "", convertedResponseBody)
		addColonIfRequired(bb)
	}

	private String stripFirstChar(String s) {
		return s.substring(1)
	}

}
