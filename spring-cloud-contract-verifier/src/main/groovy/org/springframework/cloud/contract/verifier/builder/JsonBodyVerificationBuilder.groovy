package org.springframework.cloud.contract.verifier.builder

import java.util.regex.Pattern

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import groovy.json.JsonOutput
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.commons.beanutils.PropertyUtilsBean
import org.apache.commons.text.StringEscapeUtils

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractTemplate
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.template.TemplateProcessor
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */

@PackageScope
@CompileStatic
class JsonBodyVerificationBuilder implements BodyMethodGeneration, ClassVerifier {

	private static final String FROM_REQUEST_PREFIX = "request."
	private static final String FROM_REQUEST_BODY = "escapejsonbody"
	private static final String FROM_REQUEST_PATH = "path"

	private final ContractVerifierConfigProperties configProperties
	private final TemplateProcessor templateProcessor
	private final ContractTemplate contractTemplate
	private final Contract contract
	private final Optional<String> lineSuffix
	private final boolean shouldCommentOutBDDBlocks
	private final Closure<String> postProcessJsonPathCall

	// Passing way more arguments here than I would like to, but since we are planning a major
	// refactor of this module for Hoxton release, leaving it this way for now
	JsonBodyVerificationBuilder(ContractVerifierConfigProperties configProperties,
								TemplateProcessor templateProcessor,
								ContractTemplate contractTemplate,
								Contract contract,
								Optional<String> lineSuffix,
								boolean shouldCommentOutBDDBlocks,
								Closure postProcessJsonPathCall) {
		this.configProperties = configProperties
		this.templateProcessor = templateProcessor
		this.contractTemplate = contractTemplate
		this.contract = contract
		this.lineSuffix = lineSuffix
		this.shouldCommentOutBDDBlocks = shouldCommentOutBDDBlocks
		this.postProcessJsonPathCall = postProcessJsonPathCall
	}

	Object addJsonResponseBodyCheck(BlockBuilder bb, Object convertedResponseBody,
								  BodyMatchers bodyMatchers,
								  String responseString) {
		appendJsonPath(bb, responseString)
		DocumentContext parsedRequestBody
		if (contract.request?.body) {
			def testSideRequestBody = MapConverter
					.getTestSideValues(contract.request.body)
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
				returnReferencedEntries(templateModel))
		JsonPaths jsonPaths = new JsonToJsonPathsConverter(configProperties).
				transformToJsonPathWithTestsSideValues(convertedResponseBody)
		jsonPaths.each {
			String method = it.method()
			method = processIfTemplateIsPresent(method, parsedRequestBody)
			String postProcessedMethod = templateProcessor
					.containsJsonPathTemplateEntry(method) ?
					method : postProcessJsonPathCall(method)
			bb.addLine("assertThatJson(parsedJson)" + postProcessedMethod)
			addColonIfRequired(lineSuffix, bb)
		}
		doBodyMatchingIfPresent(bodyMatchers, bb, copiedBody)
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

	protected void methodForEqualityCheck(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path())
		Object retrievedValue = value(copiedBody, bodyMatcher)
		retrievedValue = retrievedValue instanceof Pattern ? ((Pattern) retrievedValue).
				pattern() : retrievedValue
		String valueAsParam = retrievedValue instanceof String ?
				quotedAndEscaped(retrievedValue.toString()) : retrievedValue.toString()
		if (arrayRelated(path) && MatchingType.regexRelated(bodyMatcher.matchingType())) {
			buildCustomMatchingConditionForEachElement(bb, path, valueAsParam)
		}
		else {
			String comparisonMethod = bodyMatcher.
					matchingType() == MatchingType.EQUALITY ? "isEqualTo" : "matches"
			String classToCastTo = "${retrievedValue.class.simpleName}.class"
			String method = "assertThat(parsedJson.read(${path}, ${classToCastTo})).${comparisonMethod}(${valueAsParam})"
			bb.addLine(postProcessJsonPathCall(method))
		}
		addColonIfRequired(lineSuffix, bb)
	}

	private void doBodyMatchingIfPresent(BodyMatchers bodyMatchers, BlockBuilder bb, copiedBody) {
		if (bodyMatchers?.hasMatchers()) {
			bb.endBlock()
			bb.addLine(getAssertionJoiner())
			bb.startBlock()
			// for the rest we'll do JsonPath matching in brute force
			bodyMatchers.matchers().each {
				if (it.matchingType() == MatchingType.NULL) {
					methodForNullCheck(it, bb)
				}
				else if (MatchingType.regexRelated(it.matchingType()) || it.
						matchingType() == MatchingType.EQUALITY) {
					methodForEqualityCheck(it, bb, copiedBody)
				}
				else if (it.matchingType() == MatchingType.COMMAND) {
					methodForCommandExecution(it, bb, copiedBody)
				}
				else {
					methodForTypeCheck(it, bb, copiedBody)
				}
			}
		}
	}

	String getAssertionJoiner() {
		return shouldCommentOutBDDBlocks ? '// and:' : 'and:'
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


	protected void methodForCommandExecution(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path())
		// assert that path exists
		retrieveObjectByPath(copiedBody, bodyMatcher.path())
		ExecutionProperty property = bodyMatcher.value() as ExecutionProperty
		bb.addLine(
				postProcessJsonPathCall(property.insertValue("parsedJson.read(${path})")))
		addColonIfRequired(lineSuffix, bb)
	}

	protected void methodForNullCheck(BodyMatcher bodyMatcher, BlockBuilder bb) {
		String quotedAndEscaptedPath = quotedAndEscaped(bodyMatcher.path())
		String method = "assertThat((Object) parsedJson.read(${quotedAndEscaptedPath})).isNull()"
		bb.addLine(postProcessJsonPathCall(method))
		addColonIfRequired(lineSuffix, bb)
	}

	protected boolean arrayRelated(String path) {
		return path.contains("[*]") || path.contains("..")
	}

	protected void methodForTypeCheck(BodyMatcher bodyMatcher, BlockBuilder bb, Object copiedBody) {
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


	protected Object value(def body, BodyMatcher bodyMatcher) {
		if (bodyMatcher.matchingType() == MatchingType.EQUALITY || !bodyMatcher.value()) {
			return retrieveObjectByPath(body, bodyMatcher.path())
		}
		return bodyMatcher.value()
	}

	protected Object retrieveObjectByPath(def body, String path) {
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
			if (templateProcessor.containsTemplateEntry(entryAsString) &&
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

	protected String quotedAndEscaped(String string) {
		return '"' + StringEscapeUtils.escapeJava(string) + '"'
	}
}
