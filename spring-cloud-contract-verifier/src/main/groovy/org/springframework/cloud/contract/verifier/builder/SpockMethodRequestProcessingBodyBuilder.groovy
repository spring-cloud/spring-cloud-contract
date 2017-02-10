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
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

import java.util.regex.Pattern

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava
import static org.springframework.cloud.contract.verifier.util.ContentUtils.getGroovyMultipartFileParameterContent

/**
 * A {@link RequestProcessingMethodBodyBuilder} implementation that uses Spock
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@PackageScope
@TypeChecked
abstract class SpockMethodRequestProcessingBodyBuilder extends RequestProcessingMethodBodyBuilder {

	SpockMethodRequestProcessingBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties) {
		super(stubDefinition, configProperties)
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, String value) {
		return "responseBody$property == \"${value}\""
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, Object value) {
		return getResponseBodyPropertyComparisonString(property, value as String)
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, Pattern value) {
		return "responseBody$property ${patternComparison(value)}"
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, ExecutionProperty value) {
		return value.insertValue("responseBody${property}")
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("parsedJson.read('\$$property')")}")
	}

	@Override
	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry) {
		processBodyElement(blockBuilder, property + "." + entry.key, entry.value)
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
	protected String getInputString(Request request) {
		def inputString =  'def response = given().spec(request)'
		if (response.async){
			inputString = inputString + '.when().async()'
		}
		return inputString
	}

	@Override
	protected String getInputString() {
		return 'def request = given()'
	}

	@Override
	protected String getHeaderString(Header header) {
		return ".header(${getTestSideValue(header.name)}, ${getTestSideValue(header.serverValue)})"
	}

	@Override
	protected String getBodyString(String bodyAsString) {
		return ".body('''$bodyAsString''')"
	}

	@Override
	protected String getMultipartFileParameterContent(String propertyName, NamedProperty propertyValue) {
		return getGroovyMultipartFileParameterContent(propertyName, propertyValue)
	}

	@Override
	protected String getParameterString(Map.Entry<String, Object> parameter) {
		return ".param('$parameter.key', '$parameter.value')"
	}

	protected String convertHeaderComparison(String headerValue) {
		return " == '$headerValue'"
	}

	protected String convertHeaderComparison(Pattern headerValue) {
		return patternComparison(headerValue)
	}

	protected String patternComparison(Pattern pattern) {
		return patternComparison(pattern.toString())
	}

	protected String patternComparison(String pattern) {
		String converted = escapeJava(convertUnicodeEscapesIfRequired(pattern))
		return "==~ java.util.regex.Pattern.compile('${converted}')"
	}
}
