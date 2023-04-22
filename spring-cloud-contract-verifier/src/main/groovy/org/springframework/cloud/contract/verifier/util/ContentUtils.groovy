/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.util

import org.springframework.cloud.contract.spec.internal.Part

import java.util.function.Function
import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.json.JsonException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.xml.XmlSlurper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.runtime.GStringImpl
import org.xml.sax.helpers.DefaultHandler

import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.OptionalProperty
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor
import org.springframework.util.StringUtils

import static org.apache.commons.text.StringEscapeUtils.escapeJava
import static org.apache.commons.text.StringEscapeUtils.escapeJson
import static org.apache.commons.text.StringEscapeUtils.escapeXml11
import static org.apache.commons.text.StringEscapeUtils.unescapeXml
import static org.springframework.cloud.contract.verifier.util.ContentType.DEFINED
import static org.springframework.cloud.contract.verifier.util.ContentType.JSON
import static org.springframework.cloud.contract.verifier.util.ContentType.UNKNOWN
import static org.springframework.cloud.contract.verifier.util.ContentType.XML

/**
 * A utility class that can operate on a message body basing on the provided Content Type.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @author Konstantin Shevchuk
 *
 * @since 1.0.0
 */
@CompileStatic
class ContentUtils {

	private static final Log log = LogFactory.getLog(ContentUtils)

	public static final Closure GET_STUB_SIDE = {
		it instanceof DslProperty ? it.clientValue : it
	}

	public static final Closure GET_TEST_SIDE = {
		it instanceof DslProperty ? it.serverValue : it
	}

	public static final Function GET_STUB_SIDE_FUNCTION = new Function() {
		@Override
		Object apply(Object it) {
			return it instanceof DslProperty ? it.clientValue : it
		}
	}

	public static final Function GET_TEST_SIDE_FUNCTION = new Function() {
		@Override
		Object apply(Object it) {
			return it instanceof DslProperty ? it.serverValue : it
		}
	}

	private static final Pattern TEMPORARY_PATTERN_HOLDER = Pattern.
			compile('.*REGEXP>>(.*)<<.*')
	private static final Pattern TEMPORARY_EXECUTION_PATTERN_HOLDER = Pattern.
			compile('["]?EXECUTION>>(.*)<<["]?')
	private static final Pattern TEMPORARY_OPTIONAL_PATTERN_HOLDER = Pattern.
			compile('OPTIONAL>>(.*)<<')
	private static final String JSON_VALUE_PATTERN_FOR_REGEX = 'REGEXP>>%s<<'
	private static final String JSON_VALUE_PATTERN_FOR_OPTIONAL = 'OPTIONAL>>%s<<'
	private static final String JSON_VALUE_PATTERN_FOR_EXECUTION = '"EXECUTION>>%s<<"'

	/**
	 * Due to the fact that we allow users to have a body with GString and different values inside
	 * we need to be prepared that they pass regexps around both on client and server side.
	 *
	 * In order to preserve the original JSON structure we need to convert the passed Regex patterns
	 * to a temporary string, then convert all to a legitimate JSON structure and then finally
	 * convert it back from string to a pattern.
	 *
	 * @param bodyAsValue - GString with passed values
	 * @param valueProvider - provider of values either for server or client side
	 * @return JSON structure with replaced client / server side parts
	 */
	static Object extractValue(GString bodyAsValue, ContentType contentType, Closure valueProvider) {
		if (!StringUtils.hasText(bodyAsValue.toString())) {
			return bodyAsValue
		}
		if (contentType == ContentType.TEXT || contentType == ContentType.FORM) {
			return extractValueForText(bodyAsValue, valueProvider)
		}
		if (JSON == contentType) {
			return extractValueForJSON(bodyAsValue, valueProvider)
		}
		if (contentType == XML) {
			return extractValueForXML(bodyAsValue, valueProvider)
		}
		// else Brute force :(
		try {
			log.trace("No content type provided so trying to parse as JSON")
			return extractValueForJSON(bodyAsValue, valueProvider)
		}
		catch (JsonException e) {
			// Not a JSON format
			log.trace("Failed to parse as JSON - trying to parse as XML", e)
			try {
				return extractValueForXML(bodyAsValue, valueProvider)
			}
			catch (Exception exception) {
				log.trace("No content type provided and failed to parse as XML - returning the value back to the user", exception)
				return extractValueForGString(bodyAsValue, valueProvider)
			}
		}
	}

	static Object extractValue(GString bodyAsValue, ContentType contentType, Function valueProvider) {
		return extractValue(bodyAsValue, contentType, { valueProvider.apply(it) })
	}

	static ContentType getClientContentType(GString bodyAsValue) {
		try {
			extractValueForJSON(bodyAsValue, GET_STUB_SIDE)
			return JSON
		}
		catch (JsonException e) {
			try {
				getXmlSlurperWithDefaultErrorHandler()
						.parseText(
						extractValueForXML(bodyAsValue, GET_STUB_SIDE).toString())
				return XML
			}
			catch (Exception ignored) {
				extractValueForGString(bodyAsValue, GET_STUB_SIDE)
				return UNKNOWN
			}
		}
	}

	static ContentType getClientContentType(String bodyAsValue) {
		try {
			new JsonSlurper().parseText(bodyAsValue)
			return JSON
		}
		catch (JsonException e) {
			try {
				getXmlSlurperWithDefaultErrorHandler()
						.parseText(bodyAsValue)
				return XML
			}
			catch (Exception ignored) {
				return UNKNOWN
			}
		}
	}

	static ContentType getClientContentType(Object bodyAsValue) {
		if (bodyAsValue instanceof GString) {
			return getClientContentType((GString) bodyAsValue)
		}
		else if (bodyAsValue instanceof String) {
			return getClientContentType((String) bodyAsValue)
		}
		else if (bodyAsValue instanceof Map) {
			return getClientContentType((Map) bodyAsValue)
		}
		else if (bodyAsValue instanceof List) {
			return getClientContentType((List) bodyAsValue)
		}
		else if (bodyAsValue instanceof MatchingStrategy) {
			return UNKNOWN
		}
		else if (bodyAsValue instanceof FromFileProperty) {
			return UNKNOWN
		}
		return tryToGuessContentType(bodyAsValue)
	}

	private static ContentType tryToGuessContentType(Object bodyAsValue) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("No content type passed, will try to guess the type of payload")
			}
			return getClientContentType(JsonOutput.toJson(bodyAsValue))
		}
		catch (Exception ignored) {
			if (log.isTraceEnabled()) {
				log.trace("Failed to assume that body [" + bodyAsValue + "] is json")
			}
		}
		return UNKNOWN
	}

	static ContentType getClientContentType(Object bodyAsValue, Headers headers) {
		ContentType contentType = recognizeContentTypeFromHeader(headers)
		if (contentType == UNKNOWN) {
			return getClientContentType(bodyAsValue)
		}
		return contentType
	}

	static ContentType getClientContentType(Map bodyAsValue) {
		try {
			JsonOutput.toJson(bodyAsValue)
			return JSON
		}
		catch (Exception ignore) {
			return UNKNOWN
		}
	}

	static ContentType getClientContentType(List bodyAsValue) {
		try {
			JsonOutput.toJson(bodyAsValue)
			return JSON
		}
		catch (Exception ignore) {
			return UNKNOWN
		}
	}

	static GStringImpl extractValueForGString(GString bodyAsValue, Closure valueProvider) {
		return new GStringImpl(
				bodyAsValue.values.collect {
					it instanceof DslProperty ? valueProvider(it) : it
				} as String[],
				CloneUtils.clone(bodyAsValue.strings) as String[]
		)
	}

	static Object extractValue(GString bodyAsValue, Function valueProvider) {
		return extractValue(bodyAsValue, UNKNOWN, { valueProvider.apply(it) })
	}

	static Object extractValue(GString bodyAsValue, Closure valueProvider) {
		return extractValue(bodyAsValue, UNKNOWN, valueProvider)
	}

	private static String extractValueForText(GString bodyAsValue, Closure valueProvider) {
		GString transformedString = new GStringImpl(
				bodyAsValue.values.collect { valueProvider(it) } as String[],
				CloneUtils.clone(bodyAsValue.strings) as String[]
		)
		return transformedString.toString()
	}

	private static Object extractValueForJSON(GString bodyAsValue, Closure valueProvider) {
		GString transformedString = new GStringImpl(
				bodyAsValue.values.
						collect {
							transformJSONStringValue(it, valueProvider)
						} as String[],
				CloneUtils.clone(bodyAsValue.strings) as String[]
		)
		def parsedJson = new JsonSlurper().
				parseText(transformedString.toString().replace('\\', '\\\\'))
		return convertAllTemporaryRegexPlaceholdersBackToPatterns(parsedJson)
	}

	private static GStringImpl extractValueForXML(GString bodyAsValue, Closure valueProvider) {
		GStringImpl impl = new GStringImpl(
				bodyAsValue.values.
						collect {
							transformXMLStringValue(it, valueProvider)
						} as String[],
				CloneUtils.clone(bodyAsValue.strings) as String[]
		)
		// try to convert it to XML
		getXmlSlurperWithDefaultErrorHandler()
				.parseText(impl.toString())
		return impl
	}

	protected static Object transformJSONStringValue(Object obj, Closure valueProvider) {
		if (obj instanceof DslProperty) {
			return transformJSONStringValue((DslProperty) obj, valueProvider)
		}
		else if (obj instanceof Pattern) {
			return transformJSONStringValue((Pattern) obj, valueProvider)
		}
		else if (obj instanceof OptionalProperty) {
			return transformJSONStringValue((OptionalProperty) obj, valueProvider)
		}
		else if (obj instanceof ExecutionProperty) {
			return transformJSONStringValue((ExecutionProperty) obj, valueProvider)
		}
		return obj
	}

	protected static Object transformJSONStringValue(DslProperty dslProperty, Closure valueProvider) {
		return transformJSONStringValue(valueProvider(dslProperty), valueProvider)
	}

	protected static Object transformJSONStringValue(Pattern pattern, Closure valueProvider) {
		return String.format(JSON_VALUE_PATTERN_FOR_REGEX, pattern.pattern())
	}

	protected static Object transformJSONStringValue(OptionalProperty optional, Closure valueProvider) {
		return String.format(JSON_VALUE_PATTERN_FOR_OPTIONAL, optional.value())
	}

	protected static Object transformJSONStringValue(ExecutionProperty property, Closure valueProvider) {
		return String.format(JSON_VALUE_PATTERN_FOR_EXECUTION, property.executionCommand)
	}

	private static String transformXMLStringValue(Object obj, Closure valueProvider) {
		if (obj instanceof DslProperty) {
			return transformJSONStringValue((DslProperty) obj, valueProvider)
		}
		return escapeXml11(unescapeXml(obj.toString()))
	}

	private static String transformXMLStringValue(DslProperty dslProperty, Closure valueProvider) {
		return transformXMLStringValue(valueProvider(dslProperty), valueProvider)
	}

	protected static Object convertDslPropsToTemporaryRegexPatterns(Object parsedJson,
			Closure parsingClosure = MapConverter.JSON_PARSING_CLOSURE) {
		MapConverter.transformValues(parsedJson, { Object value ->
			return transformJSONStringValue(value, GET_TEST_SIDE)
		}, parsingClosure)
	}

	private static Object convertAllTemporaryRegexPlaceholdersBackToPatterns(parsedJson) {
		MapConverter.transformValues(parsedJson, { Object value ->
			if (value instanceof String) {
				String string = (String) value
				return returnParsedObject(string)
			}
			return value
		})
	}

	/**
	 * <p>
	 *     If you wonder why there is val[1] without null-check then take a look at this:
	 * </p>
	 * <p>
	 *     Example:
	 * </p>
	 * <p>
	 *     Our string equals: {@code EXECUTION>>assertThatRejectionReasonIsNull($it)<<}
	 *     The matcher matches this group with the pattern {@code EXECUTION>>(.*)<<}
	 * </p>
	 * <p>
	 * So {@code executionMatcher[0]} returns 2 elements:
	 *     <ul>
	 *         <li> index0: EXECUTION>>assertThatRejectionReasonIsNull($it)<< </li>
	 *         <li> index1: assertThatRejectionReasonIsNull($it)<< </li>
	 *     </ul>
	 * </p>
	 * <p>
	 *    Thus one can safely write {@code executionMatcher[0][1]} to retrieve the matched group
	 * </p>
	 * @param string to match the regexps against
	 * @return object converted from temporary holders
	 */
	static Object returnParsedObject(Object object) {
		if (!(object instanceof String)) {
			return object
		}
		String string = (String) object
		Matcher matcher = TEMPORARY_PATTERN_HOLDER.matcher(string.trim())
		if (matcher.matches()) {
			return Pattern.compile(patternFromMatchingGroup(matcher))
		}
		Matcher executionMatcher = TEMPORARY_EXECUTION_PATTERN_HOLDER.
				matcher(string.trim())
		if (executionMatcher.matches()) {
			return new ExecutionProperty(patternFromMatchingGroup(executionMatcher))
		}
		Matcher optionalMatcher = TEMPORARY_OPTIONAL_PATTERN_HOLDER.matcher(string.trim())
		if (optionalMatcher.matches()) {
			String patternToMatch = patternFromMatchingGroup(optionalMatcher)
			return Pattern.compile(new OptionalProperty(patternToMatch).optionalPattern())
		}
		return string
	}

	private static String patternFromMatchingGroup(Matcher matcher) {
		List val = matcher[0] as List
		return val[1]
	}

	static ContentType recognizeContentTypeFromHeader(Headers headers, Closure<Object> closure) {
		Header header = headers?.entries?.find {
			it.name == "Content-Type" ||
					it.name == "contentType"
		}
		String content = closure(header)?.toString()
		if (content?.contains("json")) {
			return JSON
		}
		if (content?.contains("xml")) {
			return XML
		}
		if (content?.contains("text")) {
			return ContentType.TEXT
		}
		if (content?.contains("form-urlencoded")) {
			return ContentType.FORM
		}
		if (content?.contains("octet-stream")) {
			return UNKNOWN
		}
		if (content && isNotTemplate(content)) {
			return DEFINED
		}
		return UNKNOWN
	}

	static ContentType recognizeContentTypeFromHeader(Headers headers) {
		return recognizeContentTypeFromHeader(headers,
				{ Header header -> header?.clientValue })
	}

	static ContentType recognizeContentTypeFromTestHeader(Headers headers) {
		return recognizeContentTypeFromHeader(headers,
				{ Header header -> header?.serverValue })
	}

	static MatchingStrategy.Type getEqualsTypeFromContentType(ContentType contentType) {
		switch (contentType) {
			case JSON:
				return MatchingStrategy.Type.EQUAL_TO_JSON
			case XML:
				return MatchingStrategy.Type.EQUAL_TO_XML
		}
		return MatchingStrategy.Type.EQUAL_TO
	}

	static ContentType recognizeContentTypeFromContent(GString gstring) {
		if (isJsonType(gstring)) {
			return JSON
		}
		if (isXmlType(gstring)) {
			return XML
		}
		return UNKNOWN
	}

	static ContentType recognizeContentTypeFromContent(Map jsonMap) {
		return JSON
	}

	static ContentType recognizeContentTypeFromContent(byte[] bytes) {
		return UNKNOWN
	}

	static ContentType recognizeContentTypeFromContent(List jsonList) {
		return JSON
	}

	static ContentType recognizeContentTypeFromContent(String string) {
		try {
			new JsonSlurper().parseText(string)
			return JSON
		}
		catch (Exception ignored) {
			if (isXmlType("$string")) {
				return XML
			}
			return UNKNOWN
		}
	}

	static ContentType recognizeContentTypeFromContent(Number number) {
		return ContentType.TEXT
	}

	static ContentType recognizeContentTypeFromContent(Object object) {
		if (object instanceof FromFileProperty) {
			FromFileProperty property = (FromFileProperty) object;
			if (property.isJson()) {
				return JSON
			} else if (property.isXml()) {
				return XML
			}
			object = object.isByte() ? object.asBytes() : object.asString()
		}
		if (object instanceof GString) {
			return recognizeContentTypeFromContent((GString) object)
		}
		else if (object instanceof Map) {
			return recognizeContentTypeFromContent((Map) object)
		}
		else if (object instanceof byte[]) {
			return recognizeContentTypeFromContent((byte[]) object)
		}
		else if (object instanceof List) {
			return recognizeContentTypeFromContent((List) object)
		}
		else if (object instanceof String) {
			return recognizeContentTypeFromContent((String) object)
		}
		else if (object instanceof Number) {
			return recognizeContentTypeFromContent((Number) object)
		}
		return UNKNOWN
	}

	static boolean isJsonType(GString gstring) {
		if (gstring.isEmpty()) {
			return false
		}
		GString stringWithoutValues = new GStringImpl(
				gstring.values.collect({
					it instanceof String || it instanceof GString ? it.toString() :
							escapeJson(it.toString())
				}) as Object[],
				CloneUtils.clone(gstring.strings) as String[]
		)
		try {
			new JsonSlurper().parseText(stringWithoutValues.toString())
			return true
		}
		catch (JsonException e) {
			// Not JSON
		}
		return false
	}

	static boolean isXmlType(GString gString) {
		GString stringWithoutValues = new GStringImpl(
				gString.values.collect({
					it instanceof String || it instanceof GString ? it.toString() :
							escapeXml11(it.toString())
				}) as Object[],
				CloneUtils.clone(gString.strings) as String[]
		)
		try {
			getXmlSlurperWithDefaultErrorHandler()
					.parseText(stringWithoutValues.toString())
			return true
		}
		catch (Exception ignored) {
			// Not XML
		}
		return false
	}

	static ContentType recognizeContentTypeFromMatchingStrategy(MatchingStrategy.Type type) {
		switch (type) {
			case MatchingStrategy.Type.EQUAL_TO_XML:
				return XML
			case MatchingStrategy.Type.EQUAL_TO_JSON:
				return JSON
		}
		return UNKNOWN
	}

	static String getGroovyMultipartFileParameterContent(String propertyName, Part propertyValue,
			Closure<String> bytesFromFile) {
		return "'$propertyName', ${partName(propertyValue, "'")}, " +
				"${groovyPartPropertyValue(propertyValue, "'", bytesFromFile)}" +
				partContentTypeNameIfPresent(propertyValue, "'")
	}

	static String getGroovyMultipartFileParameterContent(String propertyName, Part propertyValue,
			Function<FromFileProperty, String> bytesFromFile) {
		return "'$propertyName', ${partName(propertyValue, "'")}, " +
				"${groovyPartPropertyValue(propertyValue, "'", { FromFileProperty property -> bytesFromFile.apply(property) })}" +
				partContentTypeNameIfPresent(propertyValue, "'")
	}

	static String getJavaMultipartFileParameterContent(String propertyName, Part propertyValue,
			Function<FromFileProperty, String> bytesFromFile) {
		return getJavaMultipartFileParameterContent(propertyName, propertyValue, { FromFileProperty property -> bytesFromFile.apply(property) })
	}

	static String getJavaMultipartFileParameterContent(String propertyName, Part propertyValue,
			Closure<String> bytesFromFile) {
		return """"${escapeJava(propertyName)}", ${
			partName(propertyValue, '"')
		}, """ +
				"""${
					javaPartPropertyValue(propertyValue, '"', bytesFromFile)
				}${
					partContentTypeNameIfPresent(propertyValue, '"')
				}"""
	}

	static String partName(Part property, String quote) {
		if (Objects.isNull(property.filename.serverValue)) {
			return null;
		} else if (property.filename.serverValue instanceof ExecutionProperty) {
			return property.filename.serverValue.toString();
		} else {
			return quote + escapeJava(property.filename.serverValue.toString()) + quote;
		}
	}

	static String partContentTypeNameIfPresent(Part property, String quote) {
		if (Objects.isNull(property.contentType.serverValue)) {
			return ""
		}
		String contentType = property.contentType.serverValue instanceof ExecutionProperty ?
				property.contentType.serverValue.toString() : quote +
				escapeJava(property.contentType.serverValue.toString()) + quote
		return ", " + contentType
	}

	static String groovyPartPropertyValue(Part property, String quote, Closure<String> bytesFromFile) {
		if (property.body.serverValue instanceof ExecutionProperty) {
			return property.body.serverValue.toString()
		}
		else if (property.body.serverValue instanceof byte[]) {
			byte[] bytes = (byte[]) property.body.serverValue
			return "[" + bytes.collect { it }.join(", ") + "] as byte[]"
		}
		else if (property.body.serverValue instanceof FromFileProperty) {
			FromFileProperty fromFileProperty = (FromFileProperty) property.body.serverValue
			if (fromFileProperty.isByte()) {
				return bytesFromFile(fromFileProperty)
			}
			return "[" + fromFileProperty.asBytes().collect { it }.
					join(", ") + "] as byte[]"
		}
		return quote +
				escapeJava(property.body.serverValue.toString()) + quote + ".bytes"
	}

	static String javaPartPropertyValue(Part property, String quote, Closure<String> bytesFromFile) {
		if (property.body.serverValue instanceof ExecutionProperty) {
			return property.body.serverValue.toString()
		}
		else if (property.body.serverValue instanceof byte[]) {
			byte[] bytes = (byte[]) property.body.serverValue
			return "new byte[] {" + bytes.collect { it }.join(", ") + "}"
		}
		else if (property.body.serverValue instanceof FromFileProperty) {
			FromFileProperty fromFileProperty = (FromFileProperty) property.body.serverValue
			if (fromFileProperty.isByte()) {
				return bytesFromFile(fromFileProperty)
			}
			return "new byte[] {" + fromFileProperty.asBytes().collect { it }.
					join(", ") + "}"
		}
		return quote +
				escapeJava(property.body.serverValue.toString()) + quote + ".getBytes()"
	}

	static ContentType evaluateClientSideContentType(Headers contractHeaders, Object body) {
		ContentType contentType = recognizeContentTypeFromHeader(contractHeaders)
		if (UNKNOWN == contentType) {
			contentType = recognizeContentTypeFromContent(body)
		}
		return contentType
	}

	static ContentType evaluateServerSideContentType(Headers contractHeaders, Object body) {
		ContentType contentType = recognizeContentTypeFromTestHeader(contractHeaders)
		if (UNKNOWN == contentType) {
			contentType = recognizeContentTypeFromContent(body)
		}
		return contentType
	}

	/**
	 * Creates new {@link XmlSlurper} with default error handler.
	 *
	 * @return XmlSlurper with default error handler
	 */
	static XmlSlurper getXmlSlurperWithDefaultErrorHandler() {
		XmlSlurper xmlSlurper = new XmlSlurper()
		xmlSlurper.setErrorHandler(new DefaultHandler())
		return xmlSlurper
	}

	private static boolean isNotTemplate(String content) {
		return !new HandlebarsTemplateProcessor().containsTemplateEntry(content)
	}


}
