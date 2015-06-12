package io.codearte.accurest.dsl
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.TypeChecked
import groovy.xml.XmlUtil
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.Header
import io.codearte.accurest.dsl.internal.Headers
import io.codearte.accurest.util.JsonConverter

import java.util.regex.Pattern

import static groovy.json.StringEscapeUtils.escapeJava

@TypeChecked
abstract class BaseWiremockStubStrategy {

	private static Closure transform = {
		it instanceof DslProperty ? JsonConverter.transformValues(it.clientValue, transform) : it
	}

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

    protected String parseBody(Object body) {
        String bodyAsString = body as String
        try {
            def json = new JsonSlurper().parseText(bodyAsString)
            return escapeJava(JsonOutput.toJson(bodyAsString))
        } catch (Exception jsonException) {
            try {
                def xml = new XmlSlurper().parseText(bodyAsString)
                return escapeJava(XmlUtil.serialize(bodyAsString))
            } catch (Exception xmlException) {
                return escapeJava(bodyAsString)
            }
        }
    }

    protected String parseBody(List body) {
        return JsonOutput.toJson(body)
    }

    protected String parseBody(Map body) {
		def transformedMap = JsonConverter.transformValues(body, transform)
        return JsonOutput.toJson(transformedMap)
    }
}
