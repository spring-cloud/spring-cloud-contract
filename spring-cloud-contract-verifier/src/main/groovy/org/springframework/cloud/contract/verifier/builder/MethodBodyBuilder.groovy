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

import java.util.regex.Pattern

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.apache.commons.beanutils.PropertyUtilsBean
import org.apache.commons.text.StringEscapeUtils

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractTemplate
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.OptionalProperty
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor
import org.springframework.cloud.contract.verifier.template.TemplateProcessor
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.ContentUtils
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter
import org.springframework.cloud.contract.verifier.util.MapConverter
import org.springframework.util.SerializationUtils
import org.springframework.util.StringUtils

import static org.springframework.cloud.contract.verifier.util.ContentUtils.extractValue
/**
 * Main class for building method body.
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @author Olga Maciaszek-Sharma, codearte.io
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
abstract class MethodBodyBuilder {

	private static final Closure GET_SERVER_VALUE = { it instanceof DslProperty ? it.serverValue : it }
	private static final String FROM_REQUEST_PREFIX = "request."
	private static final String FROM_REQUEST_BODY = "body"
	private static final String FROM_REQUEST_PATH = "path"

	protected final ContractVerifierConfigProperties configProperties
	protected final TemplateProcessor templateProcessor
	protected final ContractTemplate contractTemplate
	protected final Contract contract

	protected MethodBodyBuilder(ContractVerifierConfigProperties configProperties, Contract contract) {
		this.configProperties = configProperties
		this.templateProcessor = processor()
		this.contractTemplate = template()
		this.contract = contract
	}

	private TemplateProcessor processor() {
		return new HandlebarsTemplateProcessor()
	}

	private ContractTemplate template() {
		return new HandlebarsTemplateProcessor()
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
	 * Builds the code that returns response in the string format
	 */
	protected abstract String getResponseAsString()

	/**
	 * Returns the given string with comment sign if required by the given implementation
	 */
	protected abstract String addCommentSignIfRequired(String baseString)

	/**
	 * Adds a colon sign at the end of each line if necessary
	 */
	protected abstract BlockBuilder addColonIfRequired(BlockBuilder blockBuilder)

	/**
	 * Builds the code that for the given {@code property} will compare it to
	 * the given Object {@code value}
	 */
	protected abstract String getResponseBodyPropertyComparisonString(String property, Object value)

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
	 * Appends to the {@link BlockBuilder} the code to retrieve a value for a property
	 * from the list with the given index
	 */
	protected abstract String getPropertyInListString(String property, Integer index)

	protected abstract String convertUnicodeEscapesIfRequired(String json)

	/**
	 * NOTE: XML support is experimental
	 */
	protected abstract String getParsedXmlResponseBodyString(String responseString)

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
	 * Returns a {@link org.springframework.cloud.contract.verifier.util.ContentType} for the given request
	 */
	protected abstract ContentType getResponseContentType()

	/**
	 * Converts the body into String format
	 */
	protected abstract String getBodyAsString()

	/**
	 * Returns {@code true} if given section should be created
	 */
	protected abstract boolean hasGivenSection()

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
		if (convertedResponseBody instanceof GString) {
			convertedResponseBody = extractValue(convertedResponseBody as GString, contentType, { Object o -> o instanceof DslProperty ? o.serverValue : o })
		}
		if (contentType != ContentType.TEXT && contentType != ContentType.FORM) {
			convertedResponseBody = MapConverter.getTestSideValues(convertedResponseBody)
		} else {
			convertedResponseBody = StringEscapeUtils.escapeJava(convertedResponseBody.toString())
		}
		if (contentType == ContentType.JSON) {
			addJsonResponseBodyCheck(bb, convertedResponseBody, bodyMatchers)
		} else if (contentType == ContentType.XML) {
			bb.addLine(getParsedXmlResponseBodyString(getResponseAsString()))
			addColonIfRequired(bb)
			// TODO xml validation
		} else {
			simpleTextResponseBodyCheck(bb, convertedResponseBody)
		}
	}

	private void simpleTextResponseBodyCheck(BlockBuilder bb, convertedResponseBody) {
		bb.addLine(getSimpleResponseBodyString(getResponseAsString()))
		processText(bb, "", convertedResponseBody)
		addColonIfRequired(bb)
	}
	
	private void addJsonResponseBodyCheck(BlockBuilder bb, convertedResponseBody, BodyMatchers bodyMatchers) {
		appendJsonPath(bb, getResponseAsString())
		Object copiedBody = cloneBody(convertedResponseBody)
		convertedResponseBody = JsonToJsonPathsConverter.removeMatchingJsonPaths(convertedResponseBody, bodyMatchers)
		// remove quotes from fromRequest objects before picking json paths
		TestSideRequestTemplateModel templateModel = contract.request?.body ?
				TestSideRequestTemplateModel.from(contract.request) : null
		convertedResponseBody = MapConverter.transformValues(convertedResponseBody, returnReferencedEntries(templateModel))
		JsonPaths jsonPaths = new JsonToJsonPathsConverter(configProperties).transformToJsonPathWithTestsSideValues(convertedResponseBody)
		DocumentContext parsedRequestBody
		if (contract.request?.body) {
			def requestBody = MapConverter.getTestSideValues(contract.request.body)
			parsedRequestBody = JsonPath.parse(requestBody)
		}
		jsonPaths.each {
			String method = it.method()
			method = processIfTemplateIsPresent(method, parsedRequestBody)
			String postProcessedMethod = templateProcessor.containsJsonPathTemplateEntry(method) ?
					method : postProcessJsonPathCall(method)
			bb.addLine("assertThatJson(parsedJson)" + postProcessedMethod)
			addColonIfRequired(bb)
		}
		doBodyMatchingIfPresent(bodyMatchers, bb, copiedBody)
		if (!(convertedResponseBody instanceof Map || convertedResponseBody instanceof List)) {
			simpleTextResponseBodyCheck(bb, convertedResponseBody)
		}
		processBodyElement(bb, "", "", convertedResponseBody)
	}

	private void doBodyMatchingIfPresent(BodyMatchers bodyMatchers, BlockBuilder bb, copiedBody) {
		if (bodyMatchers?.hasMatchers()) {
			bb.endBlock()
			bb.addLine(addCommentSignIfRequired('and:'))
			bb.startBlock()
			// for the rest we'll do JsonPath matching in brute force
			bodyMatchers.jsonPathMatchers().each {
				if (MatchingType.regexRelated(it.matchingType()) || it.matchingType() == MatchingType.EQUALITY) {
					methodForEqualityCheck(it, bb, copiedBody)
				} else if (it.matchingType() == MatchingType.COMMAND) {
					methodForCommandExecution(it, bb, copiedBody)
				} else {
					methodForTypeCheck(it, bb, copiedBody)
				}
			}
		}
	}

	private Closure<Object> returnReferencedEntries(TestSideRequestTemplateModel templateModel) {
		return { entry ->
			if (!(entry instanceof String) || !templateModel) {
				return entry
			}
			String entryAsString = (String) entry
			if (templateProcessor.containsTemplateEntry(entryAsString) &&
					!templateProcessor.containsJsonPathTemplateEntry(entryAsString)) {
				String justEntry = entryAsString - contractTemplate.openingTemplate() -
						contractTemplate.closingTemplate() - FROM_REQUEST_PREFIX
				if (justEntry == FROM_REQUEST_BODY) {
					// the body should be transformed by standard mechanism
					return entry
				}
				try {
					Object result = new PropertyUtilsBean().getProperty(templateModel, justEntry)
					// Path from the Test model is an object and we'd like to return its String representation
					if (justEntry == FROM_REQUEST_PATH) {
						return result.toString()
					}
					return result
				} catch (Exception e) {
					return entry
				}
			}
			return entry
		}
	}

	protected String processIfTemplateIsPresent(String method, DocumentContext parsedRequestBody) {
		if (templateProcessor.containsTemplateEntry(method) &&
				templateProcessor.containsJsonPathTemplateEntry(method) && contract.request?.body) {
			// Unquoting the values of non strings
			String jsonPathEntry = templateProcessor.jsonPathFromTemplateEntry(method)
			Object object = parsedRequestBody.read(jsonPathEntry)
			if (!(object instanceof String)) {
				return method
						.replace('"' + contractTemplate.openingTemplate(), contractTemplate.openingTemplate())
						.replace(contractTemplate.closingTemplate() + '"', contractTemplate.closingTemplate())
			}
		}
		return method
	}

	protected void methodForEqualityCheck(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path())
		Object retrievedValue = value(copiedBody, bodyMatcher)
		retrievedValue = retrievedValue instanceof Pattern ? ((Pattern) retrievedValue).pattern() : retrievedValue
		String valueAsParam = retrievedValue instanceof String ? quotedAndEscaped(retrievedValue.toString()) : retrievedValue.toString()
		if (arrayRelated(path) && MatchingType.regexRelated(bodyMatcher.matchingType())) {
			buildCustomMatchingConditionForEachElement(bb, path, valueAsParam)
		} else {
			String comparisonMethod = bodyMatcher.matchingType() == MatchingType.EQUALITY ? "isEqualTo" : "matches"
			String classToCastTo = "${retrievedValue.class.simpleName}.class"
			String method = "assertThat(parsedJson.read(${path}, ${classToCastTo})).${comparisonMethod}(${valueAsParam})"
			bb.addLine(postProcessJsonPathCall(method))
		}
		addColonIfRequired(bb)
	}

	protected void methodForCommandExecution(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path())
		// assert that path exists
		retrieveObjectByPath(copiedBody, bodyMatcher.path())
		ExecutionProperty property = bodyMatcher.value() as ExecutionProperty
		bb.addLine(postProcessJsonPathCall(property.insertValue("parsedJson.read(${path})")))
		addColonIfRequired(bb)
	}

	protected void methodForTypeCheck(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
		Object elementFromBody = value(copiedBody, bodyMatcher)
		if (bodyMatcher.minTypeOccurrence() != null || bodyMatcher.maxTypeOccurrence() != null) {
			checkType(bb, bodyMatcher, elementFromBody)
			String method = "assertThat((java.lang.Iterable) parsedJson.read(${quotedAndEscaped(bodyMatcher.path())}, java.util.Collection.class)).${sizeCheckMethod(bodyMatcher)}"
			bb.addLine(postProcessJsonPathCall(method))
			addColonIfRequired(bb)
		} else {
			checkType(bb, bodyMatcher, elementFromBody)
		}
	}

	protected boolean arrayRelated(String path) {
		return path.contains("[*]") || path.contains("..")
	}

	protected void buildCustomMatchingConditionForEachElement(BlockBuilder bb, String path, String valueAsParam) {
		String method = "assertThat((java.lang.Iterable) parsedJson.read(${path}, java.util.Collection.class)).allElementsMatch(${valueAsParam})"
		bb.addLine(postProcessJsonPathCall(method))
	}

	// Doing a clone doesn't work for nested lists...
	private Object cloneBody(Object object) {
		if (object instanceof List || object instanceof Map) {
			byte[] serializedObject = SerializationUtils.serialize(object)
			return SerializationUtils.deserialize(serializedObject)
		}
		try {
			return object.clone()
		} catch (CloneNotSupportedException e) {
			return object
		}
	}

	protected Object value(def body, BodyMatcher bodyMatcher) {
		if (bodyMatcher.matchingType() == MatchingType.EQUALITY || !bodyMatcher.value()) {
			return retrieveObjectByPath(body, bodyMatcher.path())
		}
		return bodyMatcher.value()
	}

	protected Object retrieveObjectByPath(def body, String path) {
		try {
			return JsonPath.parse(body).read(path)
		} catch (PathNotFoundException e) {
			throw new IllegalStateException("Entry for the provided JSON path <${path}> doesn't exist in the body <${JsonOutput.toJson(body)}>", e)
		}
	}

	protected void checkType(BlockBuilder bb, BodyMatcher it, Object elementFromBody) {
		String method = "assertThat((Object) parsedJson.read(${quotedAndEscaped(it.path())})).isInstanceOf(${classToCheck(elementFromBody).name}.class)"
		bb.addLine(postProcessJsonPathCall(method))
		addColonIfRequired(bb)
	}

	// we want to make the type more generic (e.g. not ArrayList but List)
	protected Class classToCheck(Object elementFromBody) {
		switch (elementFromBody.getClass()) {
			case List:
				return List
			case Set:
				return Set
			case Map:
				return Map
			default:
				return elementFromBody.class
		}
	}

	protected String sizeCheckMethod(BodyMatcher bodyMatcher) {
		String prefix = sizeCheckPrefix(bodyMatcher)
		if (bodyMatcher.minTypeOccurrence() != null && bodyMatcher.maxTypeOccurrence() != null) {
			return "${prefix}Between(${bodyMatcher.minTypeOccurrence()}, ${bodyMatcher.maxTypeOccurrence()})"
		} else if (bodyMatcher.minTypeOccurrence() != null ) {
			return "${prefix}GreaterThanOrEqualTo(${bodyMatcher.minTypeOccurrence()})"
		} else if (bodyMatcher.maxTypeOccurrence() != null) {
			return "${prefix}LessThanOrEqualTo(${bodyMatcher.maxTypeOccurrence()})"
		}
	}

	private String sizeCheckPrefix(BodyMatcher bodyMatcher) {
		String prefix = "has"
		if (arrayRelated(bodyMatcher.path())) {
			prefix = prefix + "Flattened"
		}
		return prefix + "Size"
	}

	protected String quotedAndEscaped(String string) {
		return '"' + StringEscapeUtils.escapeJava(string) + '"'
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
	 * Post processing of each JSON path entry
	 */
	protected String postProcessJsonPathCall(String jsonPath) {
		return jsonPath
	}

	/**
	 * Appends to {@link BlockBuilder} parsing of the JSON Path document
	 */
	protected void appendJsonPath(BlockBuilder blockBuilder, String json) {
		blockBuilder.addLine(("DocumentContext parsedJson = JsonPath.parse($json)"))
		addColonIfRequired(blockBuilder)
	}

	/**
	 * Appends to {@link BlockBuilder} processing of the given String value.
	 */
	protected void processText(BlockBuilder blockBuilder, String property, Object value) {
		if (value instanceof String && (value as String).startsWith('$')) {
			String newValue = stripFirstChar((value as String)).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(newValue)
			addColonIfRequired(blockBuilder)
		} else {
			blockBuilder.addLine(getResponseBodyPropertyComparisonString(property, value))
		}
	}

	private String stripFirstChar(String s) {
		return s.substring(1)
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header path
	 */
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Object value) {
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
		} else if (toTrim.startsWith('\\"') && toTrim.endsWith('\\"')) {
			return toTrim.substring(2, toTrim.length() - 2)
		}
		return toTrim
	}

	/**
	 * Converts the passed body into ints server side representation. All {@link DslProperty}
	 * will return their server side values
	 */
	protected Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			return extractValue(bodyValue, contentType(), GET_SERVER_VALUE)
		}
		return MapConverter.transformValues(bodyValue, GET_SERVER_VALUE)
	}

	protected ContentType contentType() {
		return ContentUtils.recognizeContentTypeFromTestHeader(this.contract.request?.headers)
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
	 * Extracts the executable test side values and
	 * returns the code of the executable
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


}
