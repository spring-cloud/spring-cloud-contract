package io.coderate.accurest.dsl

import groovy.transform.TypeChecked
import io.coderate.accurest.dsl.internal.Request
import io.coderate.accurest.dsl.internal.Response

@TypeChecked
class GroovyDsl {

    Request request
    Response response

    static GroovyDsl make(Closure closure) {
        GroovyDsl dsl = new GroovyDsl()
        closure.delegate = dsl
        closure()
        return dsl
    }

    void request(@DelegatesTo(Request) Closure closure) {
        this.request = new Request()
        closure.delegate = request
        closure()
    }

    void response(@DelegatesTo(Response) Closure closure) {
        this.response = new Response()
        closure.delegate = response
        closure()
    }
}
