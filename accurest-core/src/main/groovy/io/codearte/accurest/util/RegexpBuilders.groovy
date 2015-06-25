package io.codearte.accurest.util

import io.codearte.accurest.dsl.internal.DslProperty
import org.codehaus.groovy.runtime.GStringImpl

import java.util.regex.Pattern

import static io.codearte.accurest.util.ContentUtils.extractValue
import static org.apache.commons.lang3.StringEscapeUtils.escapeJson

public class RegexpBuilders {

	public static String buildGStringRegexpMatch(GString gString) {
		new GStringImpl(
				gString.values.collect(this.&buildGStringRegexpMatch) as Object[],
				gString.strings.collect(this.&escapeSpecialRegexChars) as String[]
		)
	}

	public static String buildGStringRegexpMatch(Pattern pattern) {
		return pattern.pattern()
	}

	public static String buildGStringRegexpMatch(DslProperty dslProperty) {
		return buildGStringRegexpMatch(dslProperty.clientValue)
	}

	public static String buildGStringRegexpMatch(Object o) {
		return escapeSpecialRegexChars(o.toString())
	}

	private final static Pattern SPECIAL_REGEX_CHARS = Pattern.compile('[{}()\\[\\].+*?^$\\\\|]')

	private static String escapeSpecialRegexChars(String str) {
		return SPECIAL_REGEX_CHARS.matcher(str).replaceAll('\\\\$0')
	}

	private final static String WS = /\s*/

	public static String buildJSONRegexpMatch(GString gString) {
		return buildJSONRegexpMatch(extractValue(gString, ContentType.JSON, { DslProperty dslProperty -> dslProperty.clientValue }))
	}

	public static String buildJSONRegexpMatch(Map jsonMap) {
		return WS + "\\{" + jsonMap.collect(this.&buildJSONRegexpMatch).join(",") + "\\}" + WS
	}

	public static String buildJSONRegexpMatch(List jsonList) {
		return WS + "\\[" + jsonList.collect(this.&buildJSONRegexpMatch).join(",") + "\\]" + WS
	}

	public static String buildJSONRegexpMatch(Map.Entry<String, Object> entry) {
		return buildJSONRegexpMatchString(escapeJson(entry.key)) + ":" + buildJSONRegexpMatch(entry.value)
	}

	public static String buildJSONRegexpMatch(Object value) {
		return buildJSONRegexpMatchStringOptionalQuotes(escapeJson(value.toString()))
	}

	public static String buildJSONRegexpMatch(Pattern pattern) {
		return buildJSONRegexpMatchStringOptionalQuotes(pattern.pattern())
	}

	public static String buildJSONRegexpMatchString(String value) {
		return WS + '"' + value + '"' + WS
	}

	public static String buildJSONRegexpMatchStringOptionalQuotes(String value) {
		return WS + '"?' + value + '"?' + WS
	}

}
