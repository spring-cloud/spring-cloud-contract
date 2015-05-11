package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic

@CompileStatic
class MethodProperty {

    final String closureDefinition

    MethodProperty(String closureDefinition) {
        this.closureDefinition = closureDefinition
    }
}
