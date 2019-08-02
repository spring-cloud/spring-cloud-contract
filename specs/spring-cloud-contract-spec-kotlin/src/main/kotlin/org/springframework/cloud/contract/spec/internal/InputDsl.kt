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

package org.springframework.cloud.contract.spec.internal

/**
 * @author Tim Ysewyn
 */
@ContractDslMarker
class InputDsl : CommonDsl(), RegexCreatingProperty<ClientDslProperty> {

    private val input = Input()

    companion object {
        fun make(block: InputDsl.() -> Unit): Input = InputDsl().apply(block).get()
    }

    private fun get(): Input = input

    fun messageFrom(messageFrom: String) = input.messageFrom(messageFrom)

    fun messageFrom(messageFrom: DslProperty<Any>) = input.messageFrom(messageFrom)

    fun messageBody(vararg pairs: Pair<String, Any>) = input.messageBody(pairs.toMap())

    fun messageBody(pair: Pair<String, Any>) = input.messageBody(mapOf(pair))

    fun messageBody(value: String) = input.messageBody(value)

    fun messageHeaders(block: Headers.() -> Unit) {
        input.messageHeaders = Headers().apply(block)
    }

    override fun anyAlphaUnicode(): ClientDslProperty = input.anyAlphaUnicode()

    override fun anyAlphaNumeric(): ClientDslProperty = input.anyAlphaNumeric()

    override fun anyNumber(): ClientDslProperty = input.anyNumber()

    override fun anyInteger(): ClientDslProperty = input.anyInteger()

    override fun anyPositiveInt(): ClientDslProperty = input.anyPositiveInt()

    override fun anyDouble(): ClientDslProperty = input.anyDouble()

    override fun anyHex(): ClientDslProperty = input.anyHex()

    override fun aBoolean(): ClientDslProperty = input.aBoolean()

    override fun anyIpAddress(): ClientDslProperty = input.anyIpAddress()

    override fun anyHostname(): ClientDslProperty = input.anyHostname()

    override fun anyEmail(): ClientDslProperty = input.anyEmail()

    override fun anyUrl(): ClientDslProperty = input.anyUrl()

    override fun anyHttpsUrl(): ClientDslProperty = input.anyHttpsUrl()

    override fun anyUuid(): ClientDslProperty = input.anyUuid()

    override fun anyDate(): ClientDslProperty = input.anyDate()

    override fun anyDateTime(): ClientDslProperty = input.anyDateTime()

    override fun anyTime(): ClientDslProperty = input.anyTime()

    override fun anyIso8601WithOffset(): ClientDslProperty = input.anyIso8601WithOffset()

    override fun anyNonBlankString(): ClientDslProperty = input.anyNonBlankString()

    override fun anyNonEmptyString(): ClientDslProperty = input.anyNonEmptyString()

    override fun anyOf(vararg values: String?): ClientDslProperty = input.anyOf(*values)

}
