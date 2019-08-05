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

/**
 * @author Tim Ysewyn
 */
@ContractDslMarker
class ResponseDsl : CommonDsl(), RegexCreatingProperty<ServerDslProperty> {

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

    override fun anyAlphaUnicode(): ServerDslProperty = Response().anyAlphaUnicode()

    override fun anyAlphaNumeric(): ServerDslProperty = Response().anyAlphaNumeric()

    override fun anyNumber(): ServerDslProperty = Response().anyNumber()

    override fun anyInteger(): ServerDslProperty = Response().anyInteger()

    override fun anyPositiveInt(): ServerDslProperty = Response().anyPositiveInt()

    override fun anyDouble(): ServerDslProperty = Response().anyDouble()

    override fun anyHex(): ServerDslProperty = Response().anyHex()

    override fun aBoolean(): ServerDslProperty = Response().aBoolean()

    override fun anyIpAddress(): ServerDslProperty = Response().anyIpAddress()

    override fun anyHostname(): ServerDslProperty = Response().anyHostname()

    override fun anyEmail(): ServerDslProperty = Response().anyEmail()

    override fun anyUrl(): ServerDslProperty = Response().anyUrl()

    override fun anyHttpsUrl(): ServerDslProperty = Response().anyHttpsUrl()

    override fun anyUuid(): ServerDslProperty = Response().anyUuid()

    override fun anyDate(): ServerDslProperty = Response().anyDate()

    override fun anyDateTime(): ServerDslProperty = Response().anyDateTime()

    override fun anyTime(): ServerDslProperty = Response().anyTime()

    override fun anyIso8601WithOffset(): ServerDslProperty = Response().anyIso8601WithOffset()

    override fun anyNonBlankString(): ServerDslProperty = Response().anyNonBlankString()

    override fun anyNonEmptyString(): ServerDslProperty = Response().anyNonEmptyString()

    override fun anyOf(vararg values: String?): ServerDslProperty = Response().anyOf(*values)

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
