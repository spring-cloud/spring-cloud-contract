// We need to be in the same package as the Contract to be able to instantiate it.
package org.springframework.cloud.contract.spec

import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.OutputMessage
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response
import java.util.regex.Pattern

/**
 * Top level Contract Dsl initializer
 *
 * @author Stephan Oudmaijer
 * @since 2.0.0
 */
fun contract(init: ContractDsl.() -> Unit) = ContractDsl().apply(init)

/**
 * Kotlin contract definition, delegates the real functionality to the Groovy Contract class.
 *
 * @author Stephan Oudmaijer
 * @since 2.0.0
 */
open class ContractDsl @JvmOverloads constructor(val contract: Contract = Contract()) {

    infix fun priority(priority: Int) = contract.priority(priority)

    infix fun label(label: String) = contract.label(label)

    infix fun description(description: String) = contract.description(description)

    infix fun name(name: String) = contract.name(name)

    fun ignored() = contract.ignored()

    fun dynamic(consumer: Pattern? = null, producer: String? = null) = DslProperty(consumer, producer)

    fun ContractDsl.input(init: Input.() -> Unit) {
        contract.input = Input().apply(init)
    }

    fun ContractDsl.outputMessage(init: OutputMessage.() -> Unit) {
        contract.outputMessage = OutputMessage().apply(init)
    }

    fun ContractDsl.request(init: Request.() -> Unit) {
        val request = Request()
        contract.request = request
        request.init()
    }

    fun ContractDsl.response(init: Response.() -> Unit) {
        val response = Response()
        contract.response = response
        response.init()
    }

    fun Request.body(vararg pairs: Pair<String, Any>) {
        contract.request.body = Body(convertObjectsToDslProperties(pairs.toMap()))
    }

    fun Response.body(vararg pairs: Pair<String, Any>) {
        contract.response.body = Body(convertObjectsToDslProperties(pairs.toMap()))
    }

    fun Request.headers(init: Headers.() -> Unit) {
        this.headers = Headers().also(init)
    }

    fun Response.headers(init: Headers.() -> Unit) {
        this.headers = Headers().apply(init)
    }
}