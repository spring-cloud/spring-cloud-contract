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

import org.springframework.cloud.contract.spec.toDslProperties
import org.springframework.cloud.contract.spec.toDslProperty
import java.util.regex.Pattern

/**
 * @author Tim Ysewyn
 */
@ContractDslMarker
class ResponseDsl : CommonDsl(), RegexCreatingProperty<ServerDslProperty> {

    private val delegate = Response()

    var status: DslProperty<Any>? = null
    var delay: DslProperty<Any>? = null
    var headers: Headers? = null
    var cookies: Cookies? = null
    var body: Body? = null
    var async: Boolean = false
    var bodyMatchers: ResponseBodyMatchers? = null

    fun status(code: Int) {
        this.status = DslProperty(code)
    }

    fun fixedDelayMilliseconds(delay: Long) {
        this.delay = delay.toDslProperty()
    }

    fun headers(headers: Headers.() -> Unit) {
        this.headers = Headers().apply(headers)
    }

    fun cookies(cookies: Cookies.() -> Unit) {
        this.cookies = Cookies().apply(cookies)
    }

    fun body(body: Map<String, Any>) {
        this.body = Body(body.toDslProperties())
    }

    fun body(vararg body: Pair<String, Any>) {
        this.body = Body(body.toMap().toDslProperties())
    }

    fun body(body: Pair<String, Any>) {
        this.body = Body(mapOf(body).toDslProperties())
    }

    fun body(body: List<Any>) {
        this.body = Body(body.toDslProperties())
    }

    fun body(body: DslProperty<Any>) {
        this.body = Body(body.toDslProperty())
    }

    fun body(body: Any) {
        this.body = Body(body)
    }

    fun bodyMatchers(bodyMatchers: ResponseBodyMatchers.() -> Unit) {
        this.bodyMatchers = ResponseBodyMatchers().apply(bodyMatchers)
    }

    /* HELPER FUNCTIONS */

    fun value(value: ClientDslProperty) = delegate.value(value)

    fun v(value: ClientDslProperty) = delegate.value(value)

    fun `$`(value: ClientDslProperty) = delegate.value(value)

    fun value(value: DslProperty<Any>) = delegate.value(value)

    fun v(value: DslProperty<Any>) = delegate.value(value)

    fun `$`(value: DslProperty<Any>) = delegate.value(value)

    fun value(value: Pattern) = delegate.value(value)

    fun v(value: Pattern) = delegate.value(value)

    fun `$`(value: Pattern) = delegate.value(value)

    fun value(value: RegexProperty) = delegate.value(value)

    fun v(value: RegexProperty) = delegate.value(value)

    fun `$`(value: RegexProperty) = delegate.value(value)

    fun value(value: Any?) = delegate.value(value)

    fun v(value: Any?) = delegate.value(value)

    fun `$`(value: Any?) = delegate.value(value)

    fun value(client: ClientDslProperty, server: ServerDslProperty) = delegate.value(client, server)

    fun v(client: ClientDslProperty, server: ServerDslProperty) = delegate.value(client, server)

    fun `$`(client: ClientDslProperty, server: ServerDslProperty) = delegate.value(client, server)

    fun value(server: ServerDslProperty, client: ClientDslProperty) = delegate.value(client, server)

    fun v(server: ServerDslProperty, client: ClientDslProperty) = delegate.value(client, server)

    fun `$`(server: ServerDslProperty, client: ClientDslProperty) = delegate.value(client, server)

    override fun anyAlphaUnicode() = delegate.anyAlphaUnicode()

    override fun anyAlphaNumeric() = delegate.anyAlphaNumeric()

    override fun anyNumber() = delegate.anyNumber()

    override fun anyInteger() = delegate.anyInteger()

    override fun anyPositiveInt() = delegate.anyPositiveInt()

    override fun anyDouble() = delegate.anyDouble()

    override fun anyHex() = delegate.anyHex()

    override fun aBoolean() = delegate.aBoolean()

    override fun anyIpAddress() = delegate.anyIpAddress()

    override fun anyHostname() = delegate.anyHostname()

    override fun anyEmail() = delegate.anyEmail()

    override fun anyUrl() = delegate.anyUrl()

    override fun anyHttpsUrl() = delegate.anyHttpsUrl()

    override fun anyUuid() = delegate.anyUuid()

    override fun anyDate() = delegate.anyDate()

    override fun anyDateTime() = delegate.anyDateTime()

    override fun anyTime() = delegate.anyTime()

    override fun anyIso8601WithOffset() = delegate.anyIso8601WithOffset()

    override fun anyNonBlankString() = delegate.anyNonBlankString()

    override fun anyNonEmptyString() = delegate.anyNonEmptyString()

    override fun anyOf(vararg values: String?) = delegate.anyOf(*values)

    internal fun get(): Response {
        val response = Response()
        status?.also { response.status = status}
        delay?.also { response.delay = delay}
        headers?.also { response.headers = headers }
        cookies?.also { response.cookies = cookies }
        body?.also { response.body = body }
        response.async = async
        bodyMatchers?.also { response.bodyMatchers = bodyMatchers }
        return response
    }
}
