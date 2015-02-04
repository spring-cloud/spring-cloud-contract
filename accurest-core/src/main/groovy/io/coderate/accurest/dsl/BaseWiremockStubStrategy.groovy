package io.coderate.accurest.dsl

import groovy.transform.CompileStatic
import io.coderate.accurest.dsl.internal.CustomizableProperty
import io.coderate.accurest.dsl.internal.Headers
import io.coderate.accurest.dsl.internal.WithValuePattern

@CompileStatic
abstract class BaseWiremockStubStrategy {
    protected Map buildClientHeadersSection(Headers headers) {
        return createHeadersSection(headers) {
            Map.Entry<String, WithValuePattern> entry -> [(entry.key): buildClientHeaderFromValuePattern(entry.value)]
        }
    }

    protected Map buildServerHeadersSection(Headers headers) {
        return createHeadersSection(headers) {
            Map.Entry<String, WithValuePattern> entry -> [(entry.key): buildServerHeaderFromValuePattern(entry.value)]
        }
    }

    private Map createHeadersSection(Headers headers, Closure closure) {
        return headers?.entries()?.collectEntries(closure)
    }

    private Map buildClientHeaderFromValuePattern(WithValuePattern valuePattern) {
        return getValuePatternSection(valuePattern)
                .findAll { it.value }
                .collectEntries { [(it.key): it.value.toClientSide()] }
    }

    private Map buildServerHeaderFromValuePattern(WithValuePattern valuePattern) {
        return getValuePatternSection(valuePattern)
                .findAll { it.value }
                .collectEntries { [(it.key): it.value.toServerSide()] }
    }

    private Map<String, CustomizableProperty> getValuePatternSection(WithValuePattern valuePattern) {
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
