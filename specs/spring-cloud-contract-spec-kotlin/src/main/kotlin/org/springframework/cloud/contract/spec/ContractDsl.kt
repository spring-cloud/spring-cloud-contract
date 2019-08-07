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
import org.springframework.cloud.contract.spec.internal.Input
import org.springframework.cloud.contract.spec.internal.InputDsl
import org.springframework.cloud.contract.spec.internal.OutputMessage
import org.springframework.cloud.contract.spec.internal.OutputMessageDsl
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.RequestDsl
import org.springframework.cloud.contract.spec.internal.Response
import org.springframework.cloud.contract.spec.internal.ResponseDsl

/**
 * @author Tim Ysewyn
 */
@ContractDslMarker
class ContractDsl {

    companion object {
        fun contract(dsl: ContractDsl.() -> Unit): Contract = ContractDsl().apply(dsl).get()
    }

    var priority: Int? = null
    var label: String? = null
    var description: String? = null
    var name: String? = null
    var ignored: Boolean = false
    var inProgress: Boolean = false
    var request: Request? = null
    var response: Response? = null
    var input: Input? = null
    var outputMessage: OutputMessage? = null

    fun request(request: RequestDsl.() -> Unit) {
        this.request = RequestDsl().apply(request).get()
    }

    fun response(response: ResponseDsl.() -> Unit) {
        this.response = ResponseDsl().apply(response).get()
    }

    fun input(input: InputDsl.() -> Unit) {
        this.input = InputDsl().apply(input).get()
    }

    fun outputMessage(outputMessage: OutputMessageDsl.() -> Unit) {
        this.outputMessage = OutputMessageDsl().apply(outputMessage).get()
    }

    private fun get(): Contract {
        val contract = Contract()
        priority?.also { contract.priority = priority!! }
        label?.also { contract.label = label!! }
        description?.also { contract.description = description!! }
        name?.also { contract.name = name!! }
        contract.ignored = ignored
        contract.isInProgress = inProgress
        request?.also { contract.request = request!! }
        response?.also { contract.response = response!! }
        input?.also { contract.input = input!! }
        outputMessage?.also { contract.outputMessage = outputMessage!! }
        return contract
    }

}
