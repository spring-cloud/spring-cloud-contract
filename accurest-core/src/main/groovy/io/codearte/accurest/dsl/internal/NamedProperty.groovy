package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includePackage = false, includeFields = true, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class NamedProperty {

    DslProperty name
    DslProperty value

    NamedProperty(DslProperty name, DslProperty value) {
        this.name = name
        this.value = value
    }
}
