// We need to be in the same package as the Contract to be able to instantiate it.
package org.springframework.cloud.contract.spec

import org.springframework.cloud.contract.verifier.spec.kotlin.DslProperty
import org.springframework.cloud.contract.verifier.spec.kotlin.ExecutionProperty
import org.springframework.cloud.contract.verifier.spec.kotlin.Input
import org.springframework.cloud.contract.verifier.spec.kotlin.OutputMessage
import org.springframework.cloud.contract.verifier.spec.kotlin.Request
import org.springframework.cloud.contract.verifier.spec.kotlin.Response
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

    fun regex(regex: String): Pattern {
        return Pattern.compile(regex)
    }

    fun execute(commandToExecute: String): ExecutionProperty {
        return ExecutionProperty(commandToExecute)
    }

    fun dynamic(consumer: Any? = null, producer: Any? = null) = DslProperty(consumer, producer)

    fun ContractDsl.input(init: Input.() -> Unit) {
        val input = Input(contract)
        input.init()
    }

    fun ContractDsl.outputMessage(init: OutputMessage.() -> Unit) {
        val outputMessage = OutputMessage(contract)
        outputMessage.init()
    }

    fun ContractDsl.request(init: Request.() -> Unit) {
        val request = Request(contract)
        request.init()
    }

    fun ContractDsl.response(init: Response.() -> Unit) {
        val response = Response(contract)
        response.init()
    }
}