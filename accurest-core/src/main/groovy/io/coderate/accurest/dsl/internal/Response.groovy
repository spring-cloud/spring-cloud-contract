package io.coderate.accurest.dsl.internal

import groovy.transform.TypeChecked

import static io.coderate.accurest.dsl.internal.DelegateHelper.delegateToClosure

@TypeChecked
class Response {

    private int status
    private Headers headers

    void status(int status) {
        this.status = status
    }

    void headers(@DelegatesTo(Headers) Closure closure) {
        this.headers = new Headers()
        delegateToClosure(closure, headers)
    }

    int getStatus() {
        return status
    }

    Headers getHeaders() {
        return headers
    }
}
