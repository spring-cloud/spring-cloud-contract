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

package org.springframework.cloud.contract.verifier.builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import groovy.json.JsonOutput;
import groovy.lang.Closure;
import org.apache.commons.beanutils.PropertyUtilsBean;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractTemplate;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.cloud.contract.spec.internal.RegexProperty;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MapConverter;

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class JsonBodyVerificationBuilder implements BodyMethodGeneration, ClassVerifier {

	private static final String FROM_REQUEST_PREFIX = "request.";

	private static final String FROM_REQUEST_BODY = "escapejsonbody";

	private static final String FROM_REQUEST_PATH = "path";

	private final boolean assertJsonSize;

	private final TemplateProcessor templateProcessor;

	private final ContractTemplate contractTemplate;

	private final Contract contract;

	private final Optional<String> lineSuffix;

	private final Function<String, String> postProcessJsonPathCall;

	// FIXME
	// Passing way more arguments here than I would like to, but since we are planning a
	// major
	// refactoring of this module for Hoxton release, leaving it this way for now
	JsonBodyVerificationBuilder(boolean assertJsonSize,
			TemplateProcessor templateProcessor, ContractTemplate contractTemplate,
			Contract contract, Optional<String> lineSuffix,
			Function<String, String> postProcessJsonPathCall) {
		this.assertJsonSize = assertJsonSize;
		this.templateProcessor = templateProcessor;
		this.contractTemplate = contractTemplate;
		this.contract = contract;
		this.lineSuffix = lineSuffix;
		this.postProcessJsonPathCall = postProcessJsonPathCall;
	}

	Object addJsonResponseBodyCheck(BlockBuilder bb, Object convertedResponseBody,
			BodyMatchers bodyMatchers, String responseString,
			boolean shouldCommentOutBDDBlocks) {
		appendJsonPath(bb, responseString);
		DocumentContext parsedRequestBody = null;
		boolean dontParseStrings = convertedResponseBody instanceof Map;
		Closure parsingClosure = dontParseStrings ? Closure.IDENTITY
				: MapConverter.JSON_PARSING_CLOSURE;
		if (hasRequestBody()) {
			Object testSideRequestBody = MapConverter
					.getTestSideValues(contract.getRequest().getBody(), parsingClosure);
			parsedRequestBody = JsonPath.parse(testSideRequestBody);
			if (convertedResponseBody instanceof String
					&& !textContainsJsonPathTemplate(convertedResponseBody.toString())) {
				convertedResponseBody = templateProcessor.transform(contract.getRequest(),
						convertedResponseBody.toString());
			}
		}
		Object copiedBody = cloneBody(convertedResponseBody);
		convertedResponseBody = JsonToJsonPathsConverter
				.removeMatchingJsonPaths(convertedResponseBody, bodyMatchers);
		// remove quotes from fromRequest objects before picking json paths
		TestSideRequestTemplateModel templateModel = hasRequestBody()
				? TestSideRequestTemplateModel.from(contract.getRequest()) : null;
		convertedResponseBody = MapConverter.transformValues(convertedResponseBody,
				returnReferencedEntries(templateModel), parsingClosure);
		JsonPaths jsonPaths = new JsonToJsonPathsConverter(assertJsonSize)
				.transformToJsonPathWithTestsSideValues(convertedResponseBody,
						parsingClosure);
		DocumentContext finalParsedRequestBody = parsedRequestBody;
		jsonPaths.forEach(it -> {
			String method = it.method();
			method = processIfTemplateIsPresent(method, finalParsedRequestBody);
			String postProcessedMethod = templateProcessor.containsJsonPathTemplateEntry(
					method) ? method : postProcessJsonPathCall.apply(method);
			bb.addLine("assertThatJson(parsedJson)" + postProcessedMethod);
			addColonIfRequired(lineSuffix, bb);
		});
		doBodyMatchingIfPresent(bodyMatchers, bb, copiedBody, shouldCommentOutBDDBlocks);
		return convertedResponseBody;
	}

	private boolean hasRequestBody() {
		return contract.getRequest() != null && contract.getRequest().getBody() != null;
	}

	private void checkType(BlockBuilder bb, BodyMatcher it, Object elementFromBody) {
		String method = "assertThat((Object) parsedJson.read("
				+ quotedAndEscaped(it.path()) + ")).isInstanceOf("
				+ classToCheck(elementFromBody).getName() + ".class)";
		bb.addLine(postProcessJsonPathCall.apply(method));
		addColonIfRequired(lineSuffix, bb);
	}

	// we want to make the type more generic (e.g. not ArrayList but List)
	private String sizeCheckMethod(BodyMatcher bodyMatcher,
			String quotedAndEscaptedPath) {
		String prefix = sizeCheckPrefix(bodyMatcher, quotedAndEscaptedPath);
		if (bodyMatcher.minTypeOccurrence() != null
				&& bodyMatcher.maxTypeOccurrence() != null) {
			return prefix + "Between(" + bodyMatcher.minTypeOccurrence() + ", "
					+ bodyMatcher.maxTypeOccurrence() + ")";
		}
		else if (bodyMatcher.minTypeOccurrence() != null) {
			return prefix + "GreaterThanOrEqualTo(" + bodyMatcher.minTypeOccurrence()
					+ ")";
		}
		else if (bodyMatcher.maxTypeOccurrence() != null) {
			return prefix + "LessThanOrEqualTo(" + bodyMatcher.maxTypeOccurrence() + ")";
		}
		return prefix;
	}

	protected void buildCustomMatchingConditionForEachElement(BlockBuilder bb,
			String path, String valueAsParam) {
		String method = "assertThat((java.lang.Iterable) parsedJson.read(" + path
				+ ", java.util.Collection.class)).as(" + path + ").allElementsMatch("
				+ valueAsParam + ")";
		bb.addLine(postProcessJsonPathCall.apply(method));
	}

	@Override
	public void methodForEqualityCheck(BodyMatcher bodyMatcher, BlockBuilder bb,
			Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path());
		Object retrievedValue = value(copiedBody, bodyMatcher);
		retrievedValue = retrievedValue instanceof RegexProperty
				? ((RegexProperty) retrievedValue).getPattern().pattern()
				: retrievedValue;
		String valueAsParam = retrievedValue instanceof String
				? quotedAndEscaped(retrievedValue.toString())
				: objectToString(retrievedValue);
		if (arrayRelated(path) && MatchingType.regexRelated(bodyMatcher.matchingType())) {
			buildCustomMatchingConditionForEachElement(bb, path, valueAsParam);
		}
		else {
			String comparisonMethod = bodyMatcher.matchingType() == MatchingType.EQUALITY
					? "isEqualTo" : "matches";
			String classToCastTo = className(retrievedValue) + ".class";
			String method = "assertThat(parsedJson.read(" + path + ", " + classToCastTo
					+ "))." + comparisonMethod + "(" + valueAsParam + ")";
			bb.addLine(postProcessJsonPathCall.apply(method));
		}
		addColonIfRequired(lineSuffix, bb);
	}

	private String className(Object retrievedValue) {
		return retrievedValue.getClass().getName().startsWith("java.lang") ?
				retrievedValue.getClass().getSimpleName() : retrievedValue.getClass().getName();
	}

	private String objectToString(Object value) {
		if (value instanceof Long) {
			return String.valueOf(value).concat("L");
		} else if (value instanceof Double) {
			return String.valueOf(value).concat("D");
		} else if (value instanceof BigDecimal) {
			return quotedAndEscaped(value.toString());
		}
		return String.valueOf(value);
	}

	protected String processIfTemplateIsPresent(String method,
			DocumentContext parsedRequestBody) {
		if (textContainsJsonPathTemplate(method) && hasRequestBody()) {
			// Unquoting the values of non strings
			String jsonPathEntry = templateProcessor.jsonPathFromTemplateEntry(method);
			Object object = parsedRequestBody.read(jsonPathEntry);
			if (!(object instanceof String)) {
				return method
						.replace('"' + contractTemplate.escapedOpeningTemplate(),
								contractTemplate.escapedOpeningTemplate())
						.replace(contractTemplate.escapedClosingTemplate() + '"',
								contractTemplate.escapedClosingTemplate())
						.replace('"' + contractTemplate.openingTemplate(),
								contractTemplate.openingTemplate())
						.replace(contractTemplate.closingTemplate() + '"',
								contractTemplate.closingTemplate());
			}
		}
		return method;
	}

	@Override
	public void methodForCommandExecution(BodyMatcher bodyMatcher, BlockBuilder bb,
			Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path());
		// assert that path exists
		retrieveObjectByPath(copiedBody, bodyMatcher.path());
		ExecutionProperty property = (ExecutionProperty) bodyMatcher.value();
		bb.addLine(postProcessJsonPathCall
				.apply(property.insertValue("parsedJson.read(" + path + ")")));
		addColonIfRequired(lineSuffix, bb);
	}

	@Override
	public void methodForNullCheck(BodyMatcher bodyMatcher, BlockBuilder bb) {
		String quotedAndEscapedPath = quotedAndEscaped(bodyMatcher.path());
		String method = "assertThat((Object) parsedJson.read(" + quotedAndEscapedPath
				+ ")).isNull()";
		bb.addLine(postProcessJsonPathCall.apply(method));
		addColonIfRequired(lineSuffix, bb);
	}

	private boolean arrayRelated(String path) {
		return path.contains("[*]") || path.contains("..");
	}

	@Override
	public void methodForTypeCheck(BodyMatcher bodyMatcher, BlockBuilder bb,
			Object copiedBody) {
		Object elementFromBody = value(copiedBody, bodyMatcher);
		if (bodyMatcher.minTypeOccurrence() != null
				|| bodyMatcher.maxTypeOccurrence() != null) {
			checkType(bb, bodyMatcher, elementFromBody);
			String quotedAndEscaptedPath = quotedAndEscaped(bodyMatcher.path());
			String method = "assertThat((java.lang.Iterable) parsedJson.read("
					+ quotedAndEscaptedPath + ", java.util.Collection.class))."
					+ sizeCheckMethod(bodyMatcher, quotedAndEscaptedPath);
			bb.addLine(postProcessJsonPathCall.apply(method));
			addColonIfRequired(lineSuffix, bb);
		}
		else {
			checkType(bb, bodyMatcher, elementFromBody);
		}
	}

	private static Object value(Object body, BodyMatcher bodyMatcher) {
		if (bodyMatcher.matchingType() == MatchingType.EQUALITY
				|| bodyMatcher.value() == null) {
			return retrieveObjectByPath(body, bodyMatcher.path());
		}
		return bodyMatcher.value();
	}

	private static Object retrieveObjectByPath(Object body, String path) {
		try {
			return JsonPath.parse(body).read(path);
		}
		catch (PathNotFoundException e) {
			throw new IllegalStateException("Entry for the provided JSON path <" + path
					+ "> doesn't exist in the body <" + JsonOutput.toJson(body) + ">", e);
		}
	}

	private Closure<Object> returnReferencedEntries(
			TestSideRequestTemplateModel templateModel) {
		return MapConverter.fromFunction(entry -> {
			if (!(entry instanceof String) || templateModel == null) {
				return entry;
			}
			String entryAsString = (String) entry;
			if (this.templateProcessor.containsTemplateEntry(entryAsString)
					&& !this.templateProcessor
							.containsJsonPathTemplateEntry(entryAsString)) {
				// TODO: HANDLEBARS LEAKING VIA request.
				String justEntry = minus(entryAsString,
						contractTemplate.escapedOpeningTemplate());
				justEntry = minus(justEntry, contractTemplate.openingTemplate());
				justEntry = minus(justEntry, contractTemplate.escapedClosingTemplate());
				justEntry = minus(justEntry, contractTemplate.closingTemplate());
				justEntry = minus(justEntry, FROM_REQUEST_PREFIX);
				if (FROM_REQUEST_BODY.equalsIgnoreCase(justEntry)) {
					// the body should be transformed by standard mechanism
					return contractTemplate.escapedOpeningTemplate() + FROM_REQUEST_PREFIX
							+ "escapedBody" + contractTemplate.escapedClosingTemplate();
				}
				try {
					Object result = new PropertyUtilsBean().getProperty(templateModel,
							justEntry);
					// Path from the Test model is an object and we'd like to return its
					// String representation
					if (FROM_REQUEST_PATH.equals(justEntry)) {
						return result.toString();
					}
					return result;
				}
				catch (Exception ignored) {
					return entry;
				}
			}
			return entry;
		});
	}

	private static String minus(CharSequence self, Object target) {
		String s = self.toString();
		String text = target.toString();
		int index = s.indexOf(text);
		if (index == -1) {
			return s;
		}
		int end = index + text.length();
		if (s.length() > end) {
			return s.substring(0, index) + s.substring(end);
		}
		return s.substring(0, index);
	}

	private boolean textContainsJsonPathTemplate(String method) {
		return templateProcessor.containsTemplateEntry(method)
				&& templateProcessor.containsJsonPathTemplateEntry(method);
	}

	/**
	 * Appends to {@link BlockBuilder} parsing of the JSON Path document
	 */
	private void appendJsonPath(BlockBuilder blockBuilder, String json) {
		blockBuilder.addLine("DocumentContext parsedJson = JsonPath.parse(" + json + ")");
		addColonIfRequired(lineSuffix, blockBuilder);
	}

	private String sizeCheckPrefix(BodyMatcher bodyMatcher, String quotedAndEscapedPath) {
		String description = "as(" + quotedAndEscapedPath + ").";
		String prefix = description + "has";
		if (arrayRelated(bodyMatcher.path())) {
			prefix = prefix + "Flattened";
		}
		return prefix + "Size";
	}

	private void doBodyMatchingIfPresent(BodyMatchers bodyMatchers, BlockBuilder bb,
			Object responseBody, boolean shouldCommentOutBDDBlocks) {
		if (bodyMatchers != null && bodyMatchers.hasMatchers()) {
			bb.addEmptyLine();
			addBodyMatchingBlock(bodyMatchers.matchers(), bb, responseBody,
					shouldCommentOutBDDBlocks);
		}
	}

}
