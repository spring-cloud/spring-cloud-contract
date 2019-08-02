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
class ResponseDsl : CommonDsl(), RegexCreatingProperty<ServerDslProperty> {

    private val response = Response()

    companion object {
        fun make(block: ResponseDsl.() -> Unit): Response = ResponseDsl().apply(block).get()
    }

    private fun get(): Response = response

    fun status(code: Int) = response.status(code)

    fun body(pair: Pair<String, Any>) = response.body(mapOf(pair))

    fun body(vararg pairs: Pair<String, Any>) = response.body(pairs.toMap())

    fun headers(block: Headers.() -> Unit) {
        response.headers = Headers().apply(block)
    }

    fun bodyMatchers(block: ResponseBodyMatchers.() -> Unit) {
        response.bodyMatchers = ResponseBodyMatchers().apply(block)
    }

    override fun anyAlphaUnicode(): ServerDslProperty = response.anyAlphaUnicode()

    override fun anyAlphaNumeric(): ServerDslProperty = response.anyAlphaNumeric()

    override fun anyNumber(): ServerDslProperty = response.anyNumber()

    override fun anyInteger(): ServerDslProperty = response.anyInteger()

    override fun anyPositiveInt(): ServerDslProperty = response.anyPositiveInt()

    override fun anyDouble(): ServerDslProperty = response.anyDouble()

    override fun anyHex(): ServerDslProperty = response.anyHex()

    override fun aBoolean(): ServerDslProperty = response.aBoolean()

    override fun anyIpAddress(): ServerDslProperty = response.anyIpAddress()

    override fun anyHostname(): ServerDslProperty = response.anyHostname()

    override fun anyEmail(): ServerDslProperty = response.anyEmail()

    override fun anyUrl(): ServerDslProperty = response.anyUrl()

    override fun anyHttpsUrl(): ServerDslProperty = response.anyHttpsUrl()

    override fun anyUuid(): ServerDslProperty = response.anyUuid()

    override fun anyDate(): ServerDslProperty = response.anyDate()

    override fun anyDateTime(): ServerDslProperty = response.anyDateTime()

    override fun anyTime(): ServerDslProperty = response.anyTime()

    override fun anyIso8601WithOffset(): ServerDslProperty = response.anyIso8601WithOffset()

    override fun anyNonBlankString(): ServerDslProperty = response.anyNonBlankString()

    override fun anyNonEmptyString(): ServerDslProperty = response.anyNonEmptyString()

    override fun anyOf(vararg values: String?): ServerDslProperty = response.anyOf(*values)
}
