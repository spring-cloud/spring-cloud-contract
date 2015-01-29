package io.coderate.accurest.dsl

import groovy.transform.TypeChecked

@TypeChecked
class GroovyDsl {

    Request request

    static GroovyDsl make(Closure closure) {
        GroovyDsl dsl = new GroovyDsl()
        closure.delegate = dsl
        closure()
        return dsl
    }

    void request(@DelegatesTo(Request) Closure closure) {
        Request request = new Request()
        this.request = request
        closure.delegate = request
        closure()
    }
}
