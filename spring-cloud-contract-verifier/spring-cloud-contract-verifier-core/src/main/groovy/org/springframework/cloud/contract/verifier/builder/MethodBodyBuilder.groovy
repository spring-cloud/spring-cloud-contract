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

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.springframework.cloud.contract.verifier.dsl.internal.NamedProperty
import org.springframework.cloud.contract.verifier.util.MapConverter
import org.springframework.cloud.contract.verifier.dsl.internal.DslProperty
import org.springframework.cloud.contract.verifier.dsl.internal.ExecutionProperty
import org.springframework.cloud.contract.verifier.dsl.internal.Header
import org.springframework.cloud.contract.verifier.dsl.internal.MatchingStrategy
import org.springframework.cloud.contract.verifier.dsl.internal.QueryParameter
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter

import java.util.regex.Pattern

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
	 * the given {@code value}
	 */
	protected abstract String getResponseBodyPropertyComparisonString(String property, String value)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header element
	 */
	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header element
	 */
	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec)

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header element
	 */
	protected abstract void processHeaderElement(BlockBuilder blockBuilder, String property, String value)

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
	protected abstract String getBodyString(String bodyAsString)

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
	protected void validateResponseBodyBlock(BlockBuilder bb, Object responseBody) {
		ContentType contentType = getResponseContentType()
		if (responseBody instanceof GString) {
			responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
		}
		if (contentType == ContentType.JSON) {
			appendJsonPath(bb, getResponseAsString())
			JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(responseBody)
			jsonPaths.each {
				String method = it.method()
				String postProcessedMethod = postProcessJsonPathCall(method)
				bb.addLine("assertThatJson(parsedJson)" + postProcessedMethod)
				addColonIfRequired(bb)
			}
			processBodyElement(bb, "", responseBody)
		} else if (contentType == ContentType.XML) {
			bb.addLine(getParsedXmlResponseBodyString(getResponseAsString()))
			addColonIfRequired(bb)
			// TODO xml validation
		} else {
			bb.addLine(getSimpleResponseBodyString(getResponseAsString()))
			processText(bb, "", responseBody as String)
			addColonIfRequired(bb)
		}
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
	protected void processText(BlockBuilder blockBuilder, String property, String value) {
		if (value.startsWith('$')) {
			value = stripFirstChar(value).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(value)
			addColonIfRequired(blockBuilder)
		} else {
			blockBuilder.addLine(getResponseBodyPropertyComparisonString(property, value))
		}
	}

	private String stripFirstChar(String s) {
		return s.substring(1);
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given header element
	 */
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Object value) {
	}

	/**
	 * Appends to the {@link BlockBuilder} the assertion for the given body element
	 */
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {
	}

	/**
	 * Removes unnecessary quotes
	 */
	protected String trimRepeatedQuotes(String toTrim) {
		return toTrim.startsWith('"') ? toTrim.replaceAll('"', '') : toTrim
	}

	/**
	 * Converts the passed body into ints server side representation. All {@link DslProperty}
	 * will return their server side values
	 */
	protected Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = MapConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.serverValue : it })
		}
		return bodyValue
	}

	/**
	 * Converts the {@link org.springframework.cloud.contract.verifier.dsl.internal.QueryParameter} server side value into its String
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
		return MapConverter.getTestSideValues(object).toString()
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
			processBodyElement(blockBuilder, prop, listElement)
		}
	}


}
