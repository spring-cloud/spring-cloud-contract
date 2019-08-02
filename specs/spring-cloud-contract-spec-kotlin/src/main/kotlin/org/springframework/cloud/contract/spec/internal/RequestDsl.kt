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
class RequestDsl : CommonDsl(), RegexCreatingProperty<ClientDslProperty> {

    private val request = Request()

    companion object {
        fun make(block: RequestDsl.() -> Unit): Request = RequestDsl().apply(block).get()
    }

    private fun get(): Request = request

    fun method(value: String) = request.method(value)

    fun method(httpMethod: HttpMethods.HttpMethod) = request.method(httpMethod)

    fun url(url: String) = request.url(url)

    fun url(url: DslProperty<Any>) = request.url(url)

    fun urlPath(path: String) = request.urlPath(path)

    fun urlPath(path: DslProperty<Any>) = request.urlPath(path)

    fun headers(block: Headers.() -> Unit) {
        request.headers = Headers().apply(block)
    }

    fun body(vararg pairs: Pair<String, Any>) = request.body(pairs.toMap())

    fun body(pair: Pair<String, Any>) = request.body(mapOf(pair))

    fun body(value: String) = request.body(value)

    fun bodyMatchers(block: BodyMatchers.() -> Unit) {
        request.bodyMatchers = BodyMatchers().apply(block)
    }

    fun value(value: DslProperty<Any>): DslProperty<Any> = request.value(value)

    fun v(value: DslProperty<Any>): DslProperty<Any> = request.value(value)

    fun `$`(value: DslProperty<Any>): DslProperty<Any> = request.value(value)

    fun value(value: Any): DslProperty<Any> = request.value(value)

    fun v(value: Any): DslProperty<Any> = request.value(value)

    fun `$`(value: Any): DslProperty<Any> = request.value(value)

    fun value(client: ClientDslProperty, server: ServerDslProperty): DslProperty<Any> = request.value(client, server)

    fun v(client: ClientDslProperty, server: ServerDslProperty): DslProperty<Any> = request.value(client, server)

    fun `$`(client: ClientDslProperty, server: ServerDslProperty): DslProperty<Any> = request.value(client, server)

    fun value(server: ServerDslProperty, client: ClientDslProperty): DslProperty<Any> = request.value(client, server)

    fun v(server: ServerDslProperty, client: ClientDslProperty): DslProperty<Any> = request.value(client, server)

    fun `$`(server: ServerDslProperty, client: ClientDslProperty): DslProperty<Any> = request.value(client, server)

    override fun anyAlphaUnicode(): ClientDslProperty = request.anyAlphaUnicode()

    override fun anyAlphaNumeric(): ClientDslProperty = request.anyAlphaNumeric()

    override fun anyNumber(): ClientDslProperty = request.anyNumber()

    override fun anyInteger(): ClientDslProperty = request.anyInteger()

    override fun anyPositiveInt(): ClientDslProperty = request.anyPositiveInt()

    override fun anyDouble(): ClientDslProperty = request.anyDouble()

    override fun anyHex(): ClientDslProperty = request.anyHex()

    override fun aBoolean(): ClientDslProperty = request.aBoolean()

    override fun anyIpAddress(): ClientDslProperty = request.anyIpAddress()

    override fun anyHostname(): ClientDslProperty = request.anyHostname()

    override fun anyEmail(): ClientDslProperty = request.anyEmail()

    override fun anyUrl(): ClientDslProperty = request.anyUrl()

    override fun anyHttpsUrl(): ClientDslProperty = request.anyHttpsUrl()

    override fun anyUuid(): ClientDslProperty = request.anyUuid()

    override fun anyDate(): ClientDslProperty = request.anyDate()

    override fun anyDateTime(): ClientDslProperty = request.anyDateTime()

    override fun anyTime(): ClientDslProperty = request.anyTime()

    override fun anyIso8601WithOffset(): ClientDslProperty = request.anyIso8601WithOffset()

    override fun anyNonBlankString(): ClientDslProperty = request.anyNonBlankString()

    override fun anyNonEmptyString(): ClientDslProperty = request.anyNonEmptyString()

    override fun anyOf(vararg values: String?): ClientDslProperty = request.anyOf(*values)
}