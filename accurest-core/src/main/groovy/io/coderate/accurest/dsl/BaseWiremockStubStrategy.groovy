package io.coderate.accurest.dsl

import groovy.transform.TypeChecked
import io.coderate.accurest.dsl.internal.Header
import io.coderate.accurest.dsl.internal.Headers

import java.util.regex.Pattern

@TypeChecked
abstract class BaseWiremockStubStrategy {
	protected Map buildClientRequestHeadersSection(Headers headers) {
		if (!headers) {
			return null
		}
		return headers.entries.collectEntries { Header entry ->
			parseHeader(entry.name, entry.clientValue)
		}
	}

	protected Map buildClientResponseHeadersSection(Headers headers) {
		if (!headers) {
			return null
		}
		return headers.entries.collectEntries { Header entry ->
			[(entry.name) : entry.clientValue]
		}
	}

	protected Map parseHeader(String entryKey, Object entry) {
		return [(entryKey): [equalTo : entry]]
	}

	protected Map parseHeader(String entryKey, String entry) {
		return [(entryKey): [equalTo : entry]]
	}

	protected Map parseHeader(String entryKey, Pattern entry) {
		return [(entryKey): [matches : entry.pattern()]]
	}

}
