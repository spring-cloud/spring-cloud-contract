package io.codearte.accurest.dsl.internal

import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.codearte.accurest.util.JsonConverter
import org.codehaus.groovy.runtime.GStringImpl

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.apache.commons.lang3.StringEscapeUtils.escapeXml11

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
class Body extends DslProperty {

	private static final Pattern TEMPORARY_PATTERN_HOLDER = Pattern.compile('REGEXP>>(.*)<<')
	private static final String JSON_VALUE_PATTERN_FOR_REGEX = 'REGEXP>>%s<<'

	boolean containsPattern

	Body(Map<String, DslProperty> body) {
		super(extractValue(body, {it.clientValue}), extractValue(body, {it.serverValue}))
	}

	private static Map<String, Object> extractValue(Map<String, DslProperty> body, Closure valueProvider) {
		body.collectEntries { Map.Entry<String, DslProperty> entry ->
			[(entry.key): valueProvider(entry.value)]
		} as Map<String, Object>
	}

	Body(List bodyAsList) {
		super(bodyAsList.collect { it.clientValue }, bodyAsList.collect { it.serverValue })
	}

	Body(Object bodyAsValue) {
		this("${bodyAsValue}")
	}

	Body(GString bodyAsValue) {
		super(extractValue(bodyAsValue, {it.clientValue}), extractValue(bodyAsValue, {it.serverValue}))
		containsPattern = containsPattern(bodyAsValue)
	}

	Body(DslProperty bodyAsValue) {
		super(bodyAsValue.clientValue, bodyAsValue.serverValue)
	}

	private boolean containsPattern(GString bodyAsValue) {
		return bodyAsValue.values.collect { it instanceof DslProperty ? it.clientValue : it }
				.find { it instanceof Pattern }
	}

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
	private static Object extractValue(GString bodyAsValue, Closure valueProvider) {
		try {
			return extractValueForJSON(bodyAsValue, valueProvider)
		} catch(JsonException e) {
			// Not a JSON format
			return extractValueForXML(bodyAsValue, valueProvider)
		}
		return bodyAsValue
	}

	private static Object extractValueForJSON(GString bodyAsValue, Closure valueProvider) {
		GString transformedString = new GStringImpl(
				bodyAsValue.values.collect { transformJSONStringValue(it, valueProvider) } as Object[],
				bodyAsValue.strings.clone()
		)
		def parsedJson = new JsonSlurper().parseText(transformedString)
		return convertAllTemporaryRegexPlaceholdersBackToPatterns(parsedJson)
	}

	private static GStringImpl extractValueForXML(GString bodyAsValue, Closure valueProvider) {
		return new GStringImpl(
				bodyAsValue.values.collect { transformXMLStringValue(it, valueProvider) } as Object[],
				bodyAsValue.strings.clone()
		)
	}

	private static String transformJSONStringValue(Object obj, Closure valueProvider) {
		return obj.toString()
	}

	private static String transformJSONStringValue(DslProperty dslProperty, Closure valueProvider) {
		return transformJSONStringValue(valueProvider(dslProperty), valueProvider)
	}

	private static String transformJSONStringValue(Pattern pattern, Closure valueProvider) {
		return String.format(JSON_VALUE_PATTERN_FOR_REGEX, pattern.pattern())
	}

	private static String transformXMLStringValue(Object obj, Closure valueProvider) {
		return escapeXml11(obj.toString())
	}

	private static String transformXMLStringValue(DslProperty dslProperty, Closure valueProvider) {
		return transformXMLStringValue(valueProvider(dslProperty), valueProvider)
	}

	private static Object convertAllTemporaryRegexPlaceholdersBackToPatterns(parsedJson) {
		JsonConverter.transformValues(parsedJson, { Object value ->
			if (value instanceof String) {
				String string = (String) value
				Matcher matcher = TEMPORARY_PATTERN_HOLDER.matcher(string)
				if (matcher.matches()) {
					String pattern = matcher[0][1]
					return Pattern.compile(pattern)
				}
				return value
			}
			return value
		})
	}

}
