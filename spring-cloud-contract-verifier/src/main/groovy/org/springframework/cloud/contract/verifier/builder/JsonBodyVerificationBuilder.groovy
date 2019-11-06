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

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import groovy.json.JsonOutput
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.commons.beanutils.PropertyUtilsBean

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractTemplate
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.RegexProperty
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.template.TemplateProcessor
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@PackageScope
@CompileStatic
class JsonBodyVerificationBuilder implements BodyMethodGeneration, ClassVerifier {

	private static final String FROM_REQUEST_PREFIX = 'request.'
	private static final String FROM_REQUEST_BODY = 'escapejsonbody'
	private static final String FROM_REQUEST_PATH = 'path'

	private final ContractVerifierConfigProperties configProperties
	private final TemplateProcessor templateProcessor
	private final ContractTemplate contractTemplate
	private final Contract contract
	private final Optional<String> lineSuffix
	private final Closure<String> postProcessJsonPathCall

	// FIXME
	// Passing way more arguments here than I would like to, but since we are planning a major
	// refactoring of this module for Hoxton release, leaving it this way for now
	JsonBodyVerificationBuilder(ContractVerifierConfigProperties configProperties,
			TemplateProcessor templateProcessor,
			ContractTemplate contractTemplate,
			Contract contract,
			Optional<String> lineSuffix,
			Closure postProcessJsonPathCall) {
		this.configProperties = configProperties
		this.templateProcessor = templateProcessor
		this.contractTemplate = contractTemplate
		this.contract = contract
		this.lineSuffix = lineSuffix
		this.postProcessJsonPathCall = postProcessJsonPathCall
	}

	Object addJsonResponseBodyCheck(BlockBuilder bb, Object convertedResponseBody,
			BodyMatchers bodyMatchers,
			String responseString,
			boolean shouldCommentOutBDDBlocks) {
		appendJsonPath(bb, responseString)
		DocumentContext parsedRequestBody
		boolean dontParseStrings = convertedResponseBody instanceof Map
		Closure parsingClosure = dontParseStrings ? Closure.IDENTITY : MapConverter.JSON_PARSING_CLOSURE
		if (contract.request?.body) {
			def testSideRequestBody = MapConverter
					.getTestSideValues(contract.request.body, parsingClosure)
			parsedRequestBody = JsonPath.parse(testSideRequestBody)
			if (convertedResponseBody instanceof String && !
					textContainsJsonPathTemplate(convertedResponseBody)) {
				convertedResponseBody = templateProcessor
						.transform(contract.request, convertedResponseBody.toString())
			}
		}
		Object copiedBody = cloneBody(convertedResponseBody)
		convertedResponseBody = JsonToJsonPathsConverter
				.removeMatchingJsonPaths(convertedResponseBody, bodyMatchers)
		// remove quotes from fromRequest objects before picking json paths
		TestSideRequestTemplateModel templateModel = contract.request?.body ?
				TestSideRequestTemplateModel.from(contract.request) : null
		convertedResponseBody = MapConverter.transformValues(convertedResponseBody,
				returnReferencedEntries(templateModel), parsingClosure)
		JsonPaths jsonPaths = new JsonToJsonPathsConverter(configProperties).
				transformToJsonPathWithTestsSideValues(convertedResponseBody, parsingClosure)
		jsonPaths.each {
			String method = it.method()
			method = processIfTemplateIsPresent(method, parsedRequestBody)
			String postProcessedMethod = templateProcessor
					.containsJsonPathTemplateEntry(method) ?
					method : postProcessJsonPathCall(method)
			bb.addLine("assertThatJson(parsedJson)" + postProcessedMethod)
			addColonIfRequired(lineSuffix, bb)
		}
		doBodyMatchingIfPresent(bodyMatchers, bb, copiedBody, shouldCommentOutBDDBlocks)
		return convertedResponseBody
	}

	protected void checkType(BlockBuilder bb, BodyMatcher it, Object elementFromBody) {
		String method = "assertThat((Object) parsedJson.read(${quotedAndEscaped(it.path())})).isInstanceOf(${classToCheck(elementFromBody).name}.class)"
		bb.addLine(postProcessJsonPathCall(method))
		addColonIfRequired(lineSuffix, bb)
	}

	// we want to make the type more generic (e.g. not ArrayList but List)
	@CompileDynamic
	protected String sizeCheckMethod(BodyMatcher bodyMatcher, String quotedAndEscaptedPath) {
		String prefix = sizeCheckPrefix(bodyMatcher, quotedAndEscaptedPath)
		if (bodyMatcher.minTypeOccurrence() != null && bodyMatcher
				.maxTypeOccurrence() != null) {
			return "${prefix}Between(${bodyMatcher.minTypeOccurrence()}, ${bodyMatcher.maxTypeOccurrence()})"
		}
		else if (bodyMatcher.minTypeOccurrence() != null) {
			return "${prefix}GreaterThanOrEqualTo(${bodyMatcher.minTypeOccurrence()})"
		}
		else if (bodyMatcher.maxTypeOccurrence() != null) {
			return "${prefix}LessThanOrEqualTo(${bodyMatcher.maxTypeOccurrence()})"
		}
		return prefix
	}


	protected void buildCustomMatchingConditionForEachElement(BlockBuilder bb, String path, String valueAsParam) {
		String method = "assertThat((java.lang.Iterable) parsedJson.read(${path}, java.util.Collection.class)).as(${path}).allElementsMatch(${valueAsParam})"
		bb.addLine(postProcessJsonPathCall(method))
	}

	@Override
	void methodForEqualityCheck(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path())
		Object retrievedValue = value(copiedBody, bodyMatcher)
		retrievedValue = retrievedValue instanceof RegexProperty ?
				((RegexProperty) retrievedValue).getPattern().pattern() : retrievedValue
		String valueAsParam = retrievedValue instanceof String ?
				quotedAndEscaped(retrievedValue.toString()) : objectToString(retrievedValue)
		if (arrayRelated(path) && MatchingType.regexRelated(bodyMatcher.matchingType())) {
			buildCustomMatchingConditionForEachElement(bb, path, valueAsParam)
		}
		else {
			String comparisonMethod = bodyMatcher.
					matchingType() == MatchingType.EQUALITY ? "isEqualTo" : "matches"
			String classToCastTo = "${className(retrievedValue)}" + ".class"
			String method = "assertThat(parsedJson.read(${path}, ${classToCastTo})).${comparisonMethod}(${valueAsParam})"
			bb.addLine(postProcessJsonPathCall(method))
		}
		addColonIfRequired(lineSuffix, bb)
	}

	private String className(Object retrievedValue) {
		return retrievedValue.class.name.startsWith("java.lang") ?
				retrievedValue.class.simpleName : retrievedValue.class.name
	}

	private String objectToString(Object value) {
		if (value instanceof Long) {
			return String.valueOf(value).concat("L")
		} else if (value instanceof Double) {
			return String.valueOf(value).concat("D")
		} else if (value instanceof BigDecimal) {
			return quotedAndEscaped(value.toString())
		}
		return String.valueOf(value)
	}

	protected String processIfTemplateIsPresent(String method, DocumentContext parsedRequestBody) {
		if (textContainsJsonPathTemplate(method) && contract.request?.body) {
			// Unquoting the values of non strings
			String jsonPathEntry = templateProcessor.jsonPathFromTemplateEntry(method)
			Object object = parsedRequestBody.read(jsonPathEntry)
			if (!(object instanceof String)) {
				return method
						.replace('"' + contractTemplate.
						escapedOpeningTemplate(), contractTemplate.
						escapedOpeningTemplate())
						.replace(contractTemplate.
						escapedClosingTemplate() + '"', contractTemplate.
						escapedClosingTemplate())
						.replace('"' + contractTemplate.
						openingTemplate(), contractTemplate.openingTemplate())
						.replace(contractTemplate.
						closingTemplate() + '"', contractTemplate.closingTemplate())
			}
		}
		return method
	}

	@Override
	void methodForCommandExecution(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path())
		// assert that path exists
		retrieveObjectByPath(copiedBody, bodyMatcher.path())
		ExecutionProperty property = bodyMatcher.value() as ExecutionProperty
		bb.addLine(
				postProcessJsonPathCall(property.insertValue("parsedJson.read(${path})")))
		addColonIfRequired(lineSuffix, bb)
	}

	@Override
	void methodForNullCheck(BodyMatcher bodyMatcher, BlockBuilder bb) {
		String quotedAndEscapedPath = quotedAndEscaped(bodyMatcher.path())
		String method = "assertThat((Object) parsedJson.read(${quotedAndEscapedPath})).isNull()"
		bb.addLine(postProcessJsonPathCall(method))
		addColonIfRequired(lineSuffix, bb)
	}

	protected boolean arrayRelated(String path) {
		return path.contains("[*]") || path.contains("..")
	}

	@Override
	void methodForTypeCheck(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
		Object elementFromBody = value(copiedBody, bodyMatcher)
		if (bodyMatcher.minTypeOccurrence() != null || bodyMatcher
				.maxTypeOccurrence() != null) {
			checkType(bb, bodyMatcher, elementFromBody)
			String quotedAndEscaptedPath = quotedAndEscaped(bodyMatcher.path())
			String method = "assertThat((java.lang.Iterable) parsedJson.read(${quotedAndEscaptedPath}, java.util.Collection.class)).${sizeCheckMethod(bodyMatcher, quotedAndEscaptedPath)}"
			bb.addLine(postProcessJsonPathCall(method))
			addColonIfRequired(lineSuffix, bb)
		}
		else {
			checkType(bb, bodyMatcher, elementFromBody)
		}
	}


	private static Object value(Object body, BodyMatcher bodyMatcher) {
		if (bodyMatcher.matchingType() == MatchingType.EQUALITY || !bodyMatcher.value()) {
			return retrieveObjectByPath(body, bodyMatcher.path())
		}
		return bodyMatcher.value()
	}

	private static Object retrieveObjectByPath(Object body, String path) {
		try {
			return JsonPath.parse(body).read(path)
		}
		catch (PathNotFoundException e) {
			throw new IllegalStateException("Entry for the provided JSON path <${path}> doesn't exist in the body <${JsonOutput.toJson(body)}>", e)
		}
	}

	@CompileDynamic
	private Closure<Object> returnReferencedEntries(TestSideRequestTemplateModel templateModel) {
		return { entry ->
			if (!(entry instanceof String) || !templateModel) {
				return entry
			}
			String entryAsString = (String) entry
			if (templateProcessor.containsTemplateEntry(entryAsString)
					&&
					!templateProcessor.containsJsonPathTemplateEntry(entryAsString)) {
				// TODO: HANDLEBARS LEAKING VIA request.
				String justEntry = entryAsString - contractTemplate.
						escapedOpeningTemplate() -
						contractTemplate.openingTemplate() -
						contractTemplate.escapedClosingTemplate() -
						contractTemplate.closingTemplate() - FROM_REQUEST_PREFIX
				if (justEntry == FROM_REQUEST_BODY) {
					// the body should be transformed by standard mechanism
					return contractTemplate.
							escapedOpeningTemplate() + FROM_REQUEST_PREFIX +
							"escapedBody" + contractTemplate.escapedClosingTemplate()
				}
				try {
					Object result = new PropertyUtilsBean()
							.getProperty(templateModel, justEntry)
					// Path from the Test model is an object and we'd like to return its String representation
					if (justEntry == FROM_REQUEST_PATH) {
						return result.toString()
					}
					return result
				}
				catch (Exception ignored) {
					return entry
				}
			}
			return entry
		}
	}

	protected boolean textContainsJsonPathTemplate(String method) {
		return templateProcessor.containsTemplateEntry(method) &&
				templateProcessor.containsJsonPathTemplateEntry(method)
	}

	/**
	 * Appends to {@link BlockBuilder} parsing of the JSON Path document
	 */
	protected void appendJsonPath(BlockBuilder blockBuilder, String json) {
		blockBuilder.addLine(("DocumentContext parsedJson = JsonPath.parse($json)"))
		addColonIfRequired(lineSuffix, blockBuilder)
	}

	private String sizeCheckPrefix(BodyMatcher bodyMatcher, String quotedAndEscaptedPath) {
		String description = "as(" + quotedAndEscaptedPath + ")."
		String prefix = description + "has"
		if (arrayRelated(bodyMatcher.path())) {
			prefix = prefix + "Flattened"
		}
		return prefix + "Size"
	}

	private void doBodyMatchingIfPresent(BodyMatchers bodyMatchers, BlockBuilder bb,
			Object responseBody, boolean shouldCommentOutBDDBlocks) {
		if (bodyMatchers?.hasMatchers()) {
			addBodyMatchingBlock(bodyMatchers.
					matchers(), bb, responseBody, shouldCommentOutBDDBlocks)
		}
	}
}
