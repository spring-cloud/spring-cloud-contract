/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.spec

import org.springframework.cloud.contract.spec.internal.ContractDslMarker
import org.springframework.cloud.contract.spec.internal.InputDsl
import org.springframework.cloud.contract.spec.internal.OutputMessageDsl
import org.springframework.cloud.contract.spec.internal.RequestDsl
import org.springframework.cloud.contract.spec.internal.ResponseDsl

/**
 * @author Tim Ysewyn
 */
@ContractDslMarker
class ContractDsl {

    val contract = Contract()

    companion object {
        fun make(block: ContractDsl.() -> Unit): Contract = ContractDsl().apply(block).get()
    }

    private fun get(): Contract = contract

    infix fun priority(priority: Int) = contract.priority(priority)

    infix fun label(label: String) = contract.label(label)

    infix fun description(description: String) = contract.description(description)

    infix fun name(name: String) = contract.name(name)

    fun ignored() = contract.ignored()

    fun request(block: RequestDsl.() -> Unit) {
        contract.request = RequestDsl.make(block)
    }

    fun response(block: ResponseDsl.() -> Unit) {
        contract.response = ResponseDsl.make(block)
    }

    fun input(block: InputDsl.() -> Unit) {
        contract.input = InputDsl.make(block)
    }

    fun outputMessage(block: OutputMessageDsl.() -> Unit) {
        contract.outputMessage = OutputMessageDsl.make(block)
    }

}
