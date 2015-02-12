package io.coderate.accurest.dsl.internal

import groovy.transform.CompileStatic

@CompileStatic
class DslProperty<T> {

    final T clientValue
    final T serverValue

    DslProperty(T clientValue, T serverValue) {
        this.clientValue = clientValue
        this.serverValue = serverValue
    }

    DslProperty(T singleValue) {
        this.clientValue = singleValue
        this.serverValue = singleValue
    }
}
