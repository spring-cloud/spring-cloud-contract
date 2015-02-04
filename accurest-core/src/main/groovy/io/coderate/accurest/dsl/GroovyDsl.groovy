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
        Request request = new Request()
        this.request = request
        closure.delegate = request
        closure()
    }

    void response(@DelegatesTo(Response) Closure closure) {
        Response response = new Response()
        this.response = response
        closure.delegate = response
        closure()
    }
}
