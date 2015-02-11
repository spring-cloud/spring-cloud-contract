package io.coderate.accurest.dsl.internal
import groovy.transform.CompileStatic

@CompileStatic
class DslProperty {

    final Object clientValue
    final Object serverValue

    DslProperty(Object clientValue, Object serverValue) {
        this.clientValue = clientValue
        this.serverValue = serverValue
    }

    DslProperty(Object singleValue) {
        this.clientValue = singleValue
        this.serverValue = singleValue
    }
}
