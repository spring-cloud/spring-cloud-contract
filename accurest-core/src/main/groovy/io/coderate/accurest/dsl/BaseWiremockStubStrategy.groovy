package io.coderate.accurest.dsl
import groovy.transform.CompileStatic
import io.coderate.accurest.dsl.internal.DslProperty
import io.coderate.accurest.dsl.internal.Headers
import io.coderate.accurest.dsl.internal.WithValuePattern

@CompileStatic
abstract class BaseWiremockStubStrategy {
    protected Map buildClientHeadersSection(Headers headers) {
        if (!headers) {
            return null
        }
        return withAssertionHeaders(headers) {
            Map.Entry<String, WithValuePattern> entry -> [(entry.key): buildClientHeaderFromValuePattern(entry.value)]
        } <<  headers.valueHeaders()
    }

    protected Map buildServerHeadersSection(Headers headers) {
        if (!headers) {
            return null
        }
        return withAssertionHeaders(headers) {
            Map.Entry<String, WithValuePattern> entry -> [(entry.key): buildServerHeaderFromValuePattern(entry.value)]
        } << headers.valueHeaders()
    }

    private Map withAssertionHeaders(Headers headers, Closure closure) {
        return headers?.assertionEntries()?.collectEntries(closure)
    }

    private Map buildClientHeaderFromValuePattern(WithValuePattern valuePattern) {
        return getValuePatternSection(valuePattern)
                .findAll { it.value }
                .collectEntries { [(it.key): it.value.clientValue] }
    }

    private Map buildServerHeaderFromValuePattern(WithValuePattern valuePattern) {
        return getValuePatternSection(valuePattern)
                .findAll { it.value }
                .collectEntries { [(it.key): it.value.serverValue] }
    }

    private Map<String, DslProperty> getValuePatternSection(WithValuePattern valuePattern) {
        return [equalToJson    : valuePattern.equalToJson,
                equalToXml     : valuePattern.equalToXml,
                matchesXPath   : valuePattern.matchesXPath,
                jsonCompareMode: valuePattern.jsonCompareMode,
                equalTo        : valuePattern.equalTo,
                contains       : valuePattern.contains,
                matches        : valuePattern.matches,
                doesNotMatch   : valuePattern.doesNotMatch,
                absent         : valuePattern.absent,
                matchesJsonPath: valuePattern.matchesJsonPath]
    }
}
