package io.coderate.accurest.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false)
class Headers {

    private Map<String, WithValuePattern> assertionHeaders = [:]
    private Map<String, String> valueHeaders = [:]

    WithValuePattern header(String headerName) {
        WithValuePattern withValuePattern = new WithValuePattern()
        assertionHeaders[headerName] = withValuePattern
        return withValuePattern
    }

    void header(Map<String, String> singleHeader) {
        Map.Entry<String, String> first = singleHeader.entrySet().first()
        valueHeaders[first?.key] = first?.value
    }

    Map<String, String> valueHeaders() {
        return Collections.unmodifiableMap(valueHeaders)
    }

    Set<Map.Entry<String, WithValuePattern>> assertionEntries() {
        return Collections.unmodifiableSet(assertionHeaders.entrySet())
    }
}
