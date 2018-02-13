// We need to be in the same package as the Contract to be able to instantiate it.
package org.springframework.cloud.contract.spec

import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.OutputMessage
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response

/**
 * Kotlin contract definition, delegates the real functionality to the Groovy Contract class.
 *
 * @author Stephan Oudmaijer
 * @since 2.0.0
 */
open class KContract @JvmOverloads constructor(val contract: Contract = Contract()) {

    companion object {
        /**
         * Top level function equivalent to [Contract.make]
         */
        fun make(init: KContract.() -> Unit): KContract {
            return KContract().apply(init)
        }
    }

    fun priority(priority: Int) = contract.priority(priority)

    fun label(label: String) = contract.label(label)

    fun description(description: String) = contract.description(description)

    fun name(name: String) = contract.name(name)

    fun ignored() = contract.ignored()

    fun KContract.input(init: Input.() -> Unit) {
        contract.input = Input().apply(init)
    }

    fun KContract.outputMessage(init: OutputMessage.() -> Unit) {
        contract.outputMessage = OutputMessage().apply(init)
    }

    fun KContract.request(init: Request.() -> Unit){
        contract.request = Request().apply(init)
    }

    fun Request.headers(init: Headers.() -> Unit) {
        this.headers = Headers().also(init)
    }

    fun KContract.response(init: Response.() -> Unit) {
        contract.response = Response().apply(init)
    }

    fun Response.headers(init: Headers.() -> Unit) {
        this.headers = Headers().apply(init)
    }
}