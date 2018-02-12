package org.springframework.cloud.contract.spec

// We need to be in the same package as the Contract file.

import org.springframework.cloud.contract.spec.internal.*

open class KContract {

    companion object {
        /**
         * Top level function equivalent to [Contract.make]
         */
        fun make(init: KContract.() -> Unit): KContract {
            val kotlinContract = KContract()
            init(kotlinContract)
            return kotlinContract
        }
    }

    val contract = Contract()

    fun priority(priority: Int) = contract.priority(priority)

    fun label(label: String) = contract.label(label)

    fun description(description: String) = contract.description(description)

    fun name(name: String) = contract.name(name)

    fun KContract.input(init: Input.() -> Unit): Input {
        val input = Input()
        input.init()
        contract.input = input
        return input
    }

    fun ignored() = contract.ignored()

    fun KContract.outputMessage(init: OutputMessage.() -> Unit): OutputMessage {
        val outputMessage = OutputMessage()
        outputMessage.init()
        contract.outputMessage = outputMessage
        return outputMessage
    }

    fun KContract.request(init: Request.() -> Unit): Request {
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

    fun KContract.response(init: Response.() -> Unit): Response {
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