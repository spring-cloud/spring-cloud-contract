package io.codearte.accurest.util
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.Headers
import io.codearte.accurest.dsl.internal.MatchingStrategy
import org.codehaus.groovy.runtime.GStringImpl

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.apache.commons.lang3.StringEscapeUtils.escapeJson
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml11

@TypeChecked
@Slf4j
class ContentUtils {

	private static final Pattern TEMPORARY_PATTERN_HOLDER = Pattern.compile('REGEXP>>(.*)<<')
	private static final String JSON_VALUE_PATTERN_FOR_REGEX = 'REGEXP>>%s<<'

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
		if (contentType == ContentType.JSON) {
			return extractValueForJSON(bodyAsValue, valueProvider)
		}
		if (contentType == ContentType.XML) {
			return extractValueForXML(bodyAsValue, valueProvider)
		}
		// else Brute force :(
		try {
			log.debug("No content type provided so trying to parse as JSON")
			return extractValueForJSON(bodyAsValue, valueProvider)
		} catch(JsonException e) {
			// Not a JSON format
			log.debug("Failed to parse as JSON - trying to parse as XML", e)
			try {
				return extractValueForXML(bodyAsValue, valueProvider)
			} catch (Exception exception) {
				log.debug("No content type provided and failed to parse as XML - returning the value back to the user", exception)
				return bodyAsValue
			}
		}

	}

	public static Object extractValue(GString bodyAsValue, Closure valueProvider) {
		return extractValue(bodyAsValue, ContentType.UNKNOWN, valueProvider)
	}

	private static Object extractValueForJSON(GString bodyAsValue, Closure valueProvider) {
		GString transformedString = new GStringImpl(
				bodyAsValue.values.collect { transformJSONStringValue(it, valueProvider) } as String[],
				bodyAsValue.strings.clone() as String[]
		)
		def parsedJson = new JsonSlurper().parseText(transformedString.toString())
		return convertAllTemporaryRegexPlaceholdersBackToPatterns(parsedJson)
	}

	private static GStringImpl extractValueForXML(GString bodyAsValue, Closure valueProvider) {
		return new GStringImpl(
				bodyAsValue.values.collect { transformXMLStringValue(it, valueProvider) } as String[],
				bodyAsValue.strings.clone() as String[]
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
					List val = matcher[0] as List
					String pattern = val[1]
					return Pattern.compile(pattern)
				}
				return value
			}
			return value
		})
	}

	public static ContentType recognizeContentTypeFromHeader(Headers headers) {
		String content = headers?.entries.find { it.name == "Content-Type" } ?.clientValue?.toString()
		if (content?.endsWith("json")) {
			return ContentType.JSON
		}
		if (content?.endsWith("xml")) {
			return ContentType.XML
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

	public static ContentType recognizeContentTypeFromContent(Object gstring) {
		return ContentType.UNKNOWN
	}

	public static boolean isJsonType(GString gstring) {
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

}
