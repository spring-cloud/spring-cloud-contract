package org.springframework.cloud.contract.verifier.spec.kotlin

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.FromRequest
import org.springframework.cloud.contract.spec.internal.Response as DelegateResponse

open class Response(val contract: Contract) {

    init {
        contract.response = org.springframework.cloud.contract.spec.internal.Response()
    }

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
        contract.response.status(code)
    }

    fun fromRequest(): FromRequest = contract.response.fromRequest() // TDO
}
