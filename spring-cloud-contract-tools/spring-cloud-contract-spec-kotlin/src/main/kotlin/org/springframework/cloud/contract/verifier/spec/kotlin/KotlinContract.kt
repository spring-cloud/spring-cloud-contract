package org.springframework.cloud.contract.spec

// We need to be in the same package as the Contract file.

import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response

class KotlinContract {

    companion object {
        /**
         * Top level function equivalent to [Contract.make]
         */
        fun make(init: KotlinContract.() -> Unit): KotlinContract {
            val kotlinContract = KotlinContract()
            init(kotlinContract)
            return kotlinContract
        }
    }

    val contract = Contract()

    fun KotlinContract.request(init: Request.() -> Unit): Request {
        val request = Request()
        request.init()
        contract.request = request
        return request
    }

    fun Request.headers(init: Headers.() -> Unit): Headers {
        val headers = Headers()
        headers.init()
        this.headers = headers
        return headers
    }

    fun KotlinContract.response(init: Response.() -> Unit): Response {
        val response = Response()
        response.init()
        contract.response = response
        return response
    }

    fun Response.headers(init: Headers.() -> Unit): Headers {
        val headers = Headers()
        headers.init()
        this.headers = headers
        return headers
    }
}