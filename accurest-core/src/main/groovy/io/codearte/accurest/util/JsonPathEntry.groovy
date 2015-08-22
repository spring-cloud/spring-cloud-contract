package io.codearte.accurest.util

import java.util.regex.Pattern

class JsonPathEntry {
	final String jsonPath
	final String optionalSuffix
	final Object value

	JsonPathEntry(String jsonPath, String optionalSuffix, Object value) {
		this.jsonPath = jsonPath
		this.optionalSuffix = optionalSuffix
		this.value = value
	}
	
	List<String> buildJsonPathComparison(String parsedJsonVariable) {
		if (optionalSuffix) {
			return ["!${parsedJsonVariable}.read('''${jsonPath}''', JSONArray).empty"]
		} else if (traversesOverCollections()) {
			return ["${parsedJsonVariable}.read('''${jsonPath}''', JSONArray).size() == 1",
					"${parsedJsonVariable}.read('''${jsonPath}''', JSONArray).get(0) ${operator()} ${potentiallyWrappedWithQuotesValue()}"]
		}
		return ["${parsedJsonVariable}.read('''${jsonPath}''') ${operator()} ${potentiallyWrappedWithQuotesValue()}"]
	}

	private boolean traversesOverCollections() {
		return jsonPath.contains('[*]')
	}

	String operator() {
		return value instanceof Pattern ? "==~" : "=="
	}

	String potentiallyWrappedWithQuotesValue() {
		return value instanceof Number ? value : "'''$value'''"
	}

	static JsonPathEntry simple(String jsonPath, Object value) {
		return new JsonPathEntry(jsonPath, "", value)
	}
}
