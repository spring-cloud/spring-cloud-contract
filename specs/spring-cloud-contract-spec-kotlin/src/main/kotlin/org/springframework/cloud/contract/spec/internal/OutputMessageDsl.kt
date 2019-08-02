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
class OutputMessageDsl : CommonDsl(), RegexCreatingProperty<ServerDslProperty> {

    private val outputMessage = OutputMessage()

    companion object {
        fun make(block: OutputMessageDsl.() -> Unit): OutputMessage = OutputMessageDsl().apply(block).get()
    }

    private fun get(): OutputMessage = outputMessage

    fun sentTo(value: String) = outputMessage.sentTo(value)

    fun sentTo(value: DslProperty<Any>) = outputMessage.sentTo(value)

    fun body(vararg pairs: Pair<String, Any>) = outputMessage.body(pairs.toMap())

    fun body(pair: Pair<String, Any>) = outputMessage.body(mapOf(pair))

    fun body(value: String) = outputMessage.body(value)

    fun headers(block: Headers.() -> Unit) {
        outputMessage.headers = Headers().apply(block)
    }

    override fun anyAlphaUnicode(): ServerDslProperty = outputMessage.anyAlphaUnicode()

    override fun anyAlphaNumeric(): ServerDslProperty = outputMessage.anyAlphaNumeric()

    override fun anyNumber(): ServerDslProperty = outputMessage.anyNumber()

    override fun anyInteger(): ServerDslProperty = outputMessage.anyInteger()

    override fun anyPositiveInt(): ServerDslProperty = outputMessage.anyPositiveInt()

    override fun anyDouble(): ServerDslProperty = outputMessage.anyDouble()

    override fun anyHex(): ServerDslProperty = outputMessage.anyHex()

    override fun aBoolean(): ServerDslProperty = outputMessage.aBoolean()

    override fun anyIpAddress(): ServerDslProperty = outputMessage.anyIpAddress()

    override fun anyHostname(): ServerDslProperty = outputMessage.anyHostname()

    override fun anyEmail(): ServerDslProperty = outputMessage.anyEmail()

    override fun anyUrl(): ServerDslProperty = outputMessage.anyUrl()

    override fun anyHttpsUrl(): ServerDslProperty = outputMessage.anyHttpsUrl()

    override fun anyUuid(): ServerDslProperty = outputMessage.anyUuid()

    override fun anyDate(): ServerDslProperty = outputMessage.anyDate()

    override fun anyDateTime(): ServerDslProperty = outputMessage.anyDateTime()

    override fun anyTime(): ServerDslProperty = outputMessage.anyTime()

    override fun anyIso8601WithOffset(): ServerDslProperty = outputMessage.anyIso8601WithOffset()

    override fun anyNonBlankString(): ServerDslProperty = outputMessage.anyNonBlankString()

    override fun anyNonEmptyString(): ServerDslProperty = outputMessage.anyNonEmptyString()

    override fun anyOf(vararg values: String?): ServerDslProperty = outputMessage.anyOf(*values)
}
