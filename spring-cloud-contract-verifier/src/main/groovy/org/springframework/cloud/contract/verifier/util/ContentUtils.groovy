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

package org.springframework.cloud.contract.verifier.util
import groovy.json.JsonException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.internal.Headers
import org.codehaus.groovy.runtime.GStringImpl
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.OptionalProperty

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava
import static org.apache.commons.lang3.StringEscapeUtils.escapeJson
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml11

@TypeChecked
@Slf4j
class ContentUtils {

	public static final Closure GET_STUB_SIDE = {
		it instanceof DslProperty ? it.clientValue : it
	}

	public static final Closure GET_TEST_SIDE = {
		it instanceof DslProperty ? it.serverValue : it
	}

	private static final Pattern TEMPORARY_PATTERN_HOLDER = Pattern.compile('.*REGEXP>>(.*)<<.*')
	private static final Pattern TEMPORARY_EXECUTION_PATTERN_HOLDER = Pattern.compile('["]?EXECUTION>>(.*)<<["]?')
	private static final Pattern TEMPORARY_OPTIONAL_PATTERN_HOLDER = Pattern.compile('OPTIONAL>>(.*)<<')
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
	public static Object extractValue(GString bodyAsValue, ContentType contentType, Closure valueProvider) {
		if (bodyAsValue.isEmpty()){
			return bodyAsValue
		}
		if (contentType == ContentType.TEXT) {
			return extractValueForText(bodyAsValue, valueProvider)
		}
		if (contentType == ContentType.JSON) {
			return extractValueForJSON(bodyAsValue, valueProvider)
		}
		if (contentType == ContentType.XML) {
			return extractValueForXML(bodyAsValue, valueProvider)
		}
		// else Brute force :(
		try {
			log.trace("No content type provided so trying to parse as JSON")
			return extractValueForJSON(bodyAsValue, valueProvider)
		} catch(JsonException e) {
			// Not a JSON format
			log.trace("Failed to parse as JSON - trying to parse as XML", e)
			try {
				return extractValueForXML(bodyAsValue, valueProvider)
			} catch (Exception exception) {
				log.trace("No content type provided and failed to parse as XML - returning the value back to the user", exception)
				return extractValueForGString(bodyAsValue, valueProvider)
			}
		}
	}

	public static ContentType getClientContentType(GString bodyAsValue) {
		try {
			extractValueForJSON(bodyAsValue, GET_STUB_SIDE)
			return ContentType.JSON
		} catch(JsonException e) {
			try {
				new XmlSlurper().parseText(extractValueForXML(bodyAsValue, GET_STUB_SIDE).toString())
				return ContentType.XML
			} catch (Exception exception) {
				extractValueForGString(bodyAsValue, GET_STUB_SIDE)
				return ContentType.UNKNOWN
			}
		}
	}

	public static ContentType getClientContentType(String bodyAsValue) {
		try {
			new JsonSlurper().parseText(bodyAsValue)
			return ContentType.JSON
		} catch(JsonException e) {
			try {
				new XmlSlurper().parseText(bodyAsValue)
				return ContentType.XML
			} catch (Exception exception) {
				return ContentType.UNKNOWN
			}
		}
	}

	public static ContentType getClientContentType(Object bodyAsValue) {
		return ContentType.UNKNOWN
	}

	public static ContentType getClientContentType(Map bodyAsValue) {
		try {
			JsonOutput.toJson(bodyAsValue)
			return ContentType.JSON
		} catch (Exception ignore) {
			return ContentType.UNKNOWN
		}
	}

	public static ContentType getClientContentType(List bodyAsValue) {
		try {
			JsonOutput.toJson(bodyAsValue)
			return ContentType.JSON
		} catch (Exception ignore) {
			return ContentType.UNKNOWN
		}
	}

	private static GStringImpl extractValueForGString(GString bodyAsValue, Closure valueProvider) {
		return new GStringImpl(
				bodyAsValue.values.collect { it instanceof DslProperty ? valueProvider(it) : it } as String[],
				bodyAsValue.strings.clone() as String[]
		)
	}

	public static Object extractValue(GString bodyAsValue, Closure valueProvider) {
		return extractValue(bodyAsValue, ContentType.UNKNOWN, valueProvider)
	}

	private static String extractValueForText(GString bodyAsValue, Closure valueProvider) {
		GString transformedString = new GStringImpl(
				bodyAsValue.values.collect { valueProvider(it) } as String[],
				bodyAsValue.strings.clone() as String[]
		)
		return transformedString.toString()
	}

	private static Object extractValueForJSON(GString bodyAsValue, Closure valueProvider) {
		GString transformedString = new GStringImpl(
				bodyAsValue.values.collect { transformJSONStringValue(it, valueProvider) } as String[],
				bodyAsValue.strings.clone() as String[]
		)
		def parsedJson = new JsonSlurper().parseText(transformedString.toString().replace('\\', '\\\\'))
		return convertAllTemporaryRegexPlaceholdersBackToPatterns(parsedJson)
	}

	private static GStringImpl extractValueForXML(GString bodyAsValue, Closure valueProvider) {
		return new GStringImpl(
				bodyAsValue.values.collect { transformXMLStringValue(it, valueProvider) } as String[],
				bodyAsValue.strings.clone() as String[]
		)
	}

	protected static Object transformJSONStringValue(Object obj, Closure valueProvider) {
		return obj
	}

	protected static Object transformJSONStringValue(DslProperty dslProperty, Closure valueProvider) {
		return transformJSONStringValue(valueProvider(dslProperty), valueProvider)
	}

	protected static Object transformJSONStringValue(Pattern pattern, Closure valueProvider) {
		return String.format(JSON_VALUE_PATTERN_FOR_REGEX, pattern.pattern())
	}

	protected static Object transformJSONStringValue(OptionalProperty optional, Closure valueProvider) {
		return String.format(JSON_VALUE_PATTERN_FOR_OPTIONAL, optional.value)
	}

	protected static Object transformJSONStringValue(ExecutionProperty property, Closure valueProvider) {
		return String.format(JSON_VALUE_PATTERN_FOR_EXECUTION, property.executionCommand)
	}

	private static String transformXMLStringValue(Object obj, Closure valueProvider) {
		return escapeXml11(obj.toString())
	}

	private static String transformXMLStringValue(DslProperty dslProperty, Closure valueProvider) {
		return transformXMLStringValue(valueProvider(dslProperty), valueProvider)
	}

	protected static Object convertDslPropsToTemporaryRegexPatterns(parsedJson) {
		MapConverter.transformValues(parsedJson, { Object value ->
			return transformJSONStringValue(value, GET_TEST_SIDE)
		})
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
		Matcher executionMatcher = TEMPORARY_EXECUTION_PATTERN_HOLDER.matcher(string.trim())
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

	public static ContentType recognizeContentTypeFromHeader(Headers headers) {
		String content = headers?.entries.find { it.name == "Content-Type" } ?.clientValue?.toString()
		if (content?.endsWith("json")) {
			return ContentType.JSON
		}
		if (content?.endsWith("xml")) {
			return ContentType.XML
		}
		if (content?.contains("text")) {
			return ContentType.TEXT
		}
		return ContentType.UNKNOWN
	}

	public static MatchingStrategy.Type getEqualsTypeFromContentType(ContentType contentType) {
		switch (contentType) {
			case ContentType.JSON:
				return MatchingStrategy.Type.EQUAL_TO_JSON
			case ContentType.XML:
				return MatchingStrategy.Type.EQUAL_TO_XML
		}
		return MatchingStrategy.Type.EQUAL_TO
	}

	public static ContentType recognizeContentTypeFromContent(GString gstring) {
		if (isJsonType(gstring)) {
			return ContentType.JSON
		}
		if (isXmlType(gstring)) {
			return ContentType.XML
		}
		return ContentType.UNKNOWN
	}

	public static ContentType recognizeContentTypeFromContent(Map jsonMap) {
		return ContentType.JSON
	}

	public static ContentType recognizeContentTypeFromContent(List jsonList) {
		return ContentType.JSON
	}

	public static ContentType recognizeContentTypeFromContent(String string) {
		try {
			new JsonSlurper().parseText(string)
			return ContentType.JSON
		} catch (Exception e){
			return ContentType.UNKNOWN
		}
	}

	public static ContentType recognizeContentTypeFromContent(Object gstring) {
		return ContentType.UNKNOWN
	}

	public static boolean isJsonType(GString gstring) {
		if (gstring.isEmpty()) {
			return false
		}
		GString stringWithoutValues = new GStringImpl(
				gstring.values.collect({
					it instanceof String || it instanceof GString ? it.toString() : escapeJson(it.toString())
				}) as Object[],
				gstring.strings.clone() as String[]
		)
		try {
			new JsonSlurper().parseText(stringWithoutValues.toString())
			return true
		} catch (JsonException e) {
			// Not JSON
		}
		return false
	}

	public static boolean isXmlType(GString gstring) {
		GString stringWithoutValues = new GStringImpl(
				gstring.values.collect({
					it instanceof String || it instanceof GString ? it.toString() : escapeXml11(it.toString())
				}) as Object[],
				gstring.strings.clone() as String[]
		)
		try {
			new XmlSlurper().parseText(stringWithoutValues.toString())
			return true
		} catch (Exception e) {
			// Not XML
		}
		return false
	}

	public static ContentType recognizeContentTypeFromMatchingStrategy(MatchingStrategy.Type type) {
		switch (type) {
			case MatchingStrategy.Type.EQUAL_TO_XML:
				return ContentType.XML
			case MatchingStrategy.Type.EQUAL_TO_JSON:
				return ContentType.JSON
		}
		return ContentType.UNKNOWN
	}

	static String getGroovyMultipartFileParameterContent(String propertyName, NamedProperty propertyValue) {
		return "'$propertyName', '$propertyValue.name.serverValue', '$propertyValue.value.serverValue'.bytes"
	}

	static String getJavaMultipartFileParameterContent(String propertyName, NamedProperty propertyValue) {
		return """"${escapeJava(propertyName)}", "${escapeJava(propertyValue.name.serverValue as String)}", "${escapeJava(propertyValue.value.serverValue as String)}".getBytes()"""
	}


}
