package org.springframework.cloud.contract.spec

import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response

object KotlinContract {

    /**
     * Top level function equivalent to [Contract.make]
     */
    fun make(init: Contract.() -> Unit): Contract {
        val contract = Contract()
        contract.init()
        return contract
    }

    fun Contract.request(init: Request.() -> Unit): Request {
        val request = Request()
        request.init()
        this.request = request
        return request
    }


    fun Request.headers(init: Headers.() -> Unit): Headers {
        val headers = Headers()
        headers.init()
        this.headers = headers
        return headers
    }

    fun Contract.response(init: Response.() -> Unit): Response {
        val response = Response()
        response.init()
        this.response = response
        return response
    }

    fun Response.headers(init: Headers.() -> Unit): Headers {
        val headers = Headers()
        headers.init()
        this.headers = headers
        return headers
    }
}