package io.coderate.accurest.dsl.internal
import groovy.transform.CompileStatic

@CompileStatic
class ClientDslProperty extends DslProperty {

    ClientDslProperty(Object clientValue, Object serverValue) {
        super(clientValue, serverValue) }

    ClientDslProperty(Object singleValue) {
        super(singleValue)
    }
}
