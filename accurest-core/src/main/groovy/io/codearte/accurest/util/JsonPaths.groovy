package io.codearte.accurest.util

class JsonPaths extends HashSet<JsonPathEntry> {

	Object getAt(String key) {
		return find {
			it.jsonPath == key
		}?.value
	}

	Object putAt(String key, Object value) {
		JsonPathEntry entry = find {
			it.jsonPath == key
		}
		if (!entry) {
			return null
		}
		Object oldValue = entry.value
		add(new JsonPathEntry(entry.jsonPath, entry.optionalSuffix, value))
		return oldValue
	}
}

