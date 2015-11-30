package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class Multipart extends DslProperty {

    Multipart(Map<String, DslProperty> multipart) {
        super(extractValue(multipart, { DslProperty p -> p.clientValue}), extractValue(multipart, {DslProperty p -> p.serverValue}))
    }

    private static Map<String, Object> extractValue(Map<String, DslProperty> multipart, Closure valueProvider) {
        multipart.collectEntries { Map.Entry<String, DslProperty> entry ->
            [(entry.key): valueProvider(entry.value)]
        } as Map<String, Object>
    }

    Multipart(List<DslProperty> multipartAsList) {
        super(multipartAsList.collect { DslProperty p -> p.clientValue }, multipartAsList.collect { DslProperty p -> p.serverValue })
    }

    Multipart(Object multipartAsValue) {
        this("${multipartAsValue}")
    }

    Multipart(GString multipartAsValue) {
        super(multipartAsValue, multipartAsValue)
    }

    Multipart(DslProperty multipartAsValue) {
        super(multipartAsValue.clientValue, multipartAsValue.serverValue)
    }

    Multipart(MatchingStrategy matchingStrategy) {
        super(matchingStrategy, matchingStrategy)
    }
}
