package org.springframework.cloud.contract.verifier.spec.kotlin

import org.springframework.cloud.contract.spec.internal.FromRequest
import org.springframework.cloud.contract.spec.internal.Response as DelegateResponse

open class Response(val delegate: DelegateResponse) {

    var headers = Headers()

    fun body(pair: Pair<String, Any>) {
    }

    fun body(vararg pairs: Pair<String, Any>) {
    }

    fun headers(init: Headers.() -> Unit) {
        headers = Headers()
        headers.init()
        // TODO assign to delegate
    }

    fun status(code: Int) {
        delegate.status(code)
    }

    fun fromRequest(): FromRequest = delegate.fromRequest() // TDO
}
