package io.codearte.accurest.dsl.internal

import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.codearte.accurest.util.JsonConverter
import org.codehaus.groovy.runtime.GStringImpl

import java.util.regex.Matcher
import java.util.regex.Pattern

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
class Body extends DslProperty {

	private static final Pattern TEMPORARY_PATTERN_HOLDER = Pattern.compile('REGEXP>>(.*)<<')
	private static final String JSON_VALUE_PATTERN_FOR_REGEX = 'REGEXP>>%s<<'

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
	}

	Body(DslProperty bodyAsValue) {
		super(bodyAsValue.clientValue, bodyAsValue.serverValue)
	}

	private static Object extractValue(GString bodyAsValue, Closure valueProvider) {
		GString gString = new GStringImpl(bodyAsValue.values.clone(), bodyAsValue.strings.clone())
		Object[] values = bodyAsValue.values.collect { it instanceof DslProperty ? valueProvider(it) : it } as Object[]
		Object[] valuesWithRegexpsAsTransformedStrings = values.collect {
			it instanceof Pattern ? String.format(JSON_VALUE_PATTERN_FOR_REGEX, it.toString())  : it
		} as Object[]
		def parsedJson = new JsonSlurper().parseText(new GStringImpl(valuesWithRegexpsAsTransformedStrings, gString.strings))
		return convertAllTemporaryRegexPlaceholdersBackToPatterns(parsedJson)
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
