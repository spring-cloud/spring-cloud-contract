package io.coderate.accurest.dsl.internal
import groovy.transform.CompileStatic

@CompileStatic
class ServerDslProperty extends DslProperty {

    ServerDslProperty(Object clientValue, Object serverValue) {
        super(clientValue, serverValue) }

    ServerDslProperty(Object singleValue) {
        super(singleValue)
    }
}
