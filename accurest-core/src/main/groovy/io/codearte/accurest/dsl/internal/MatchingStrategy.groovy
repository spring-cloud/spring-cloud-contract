package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString;

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@CompileStatic
class MatchingStrategy extends DslProperty {

	Type type
	JSONCompareMode jsonCompareMode

	MatchingStrategy(Object value, Type type) {
		this(value, type, null)
	}

	MatchingStrategy(Object value, Type type, JSONCompareMode jsonCompareMode) {
		super(value)
		this.type = type
		this.jsonCompareMode = jsonCompareMode
	}

	MatchingStrategy(DslProperty value, Type type) {
		this(value, type, null)
	}

	MatchingStrategy(DslProperty value, Type type, JSONCompareMode jsonCompareMode) {
		super(value.clientValue, value.serverValue)
		this.type = type
		this.jsonCompareMode = jsonCompareMode
	}

	enum Type {

		EQUAL_TO("equalTo"), CONTAINS("contains"), MATCHING("matches"), NOT_MATCHING("doesNotMatch"),
		EQUAL_TO_JSON("equalToJson"), EQUAL_TO_XML("equalToXml")

		final String name

		Type(name) {
			this.name = name
		}
	}

}