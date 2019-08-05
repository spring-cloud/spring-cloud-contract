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

/**
 * @author Tim Ysewyn
 */
@ContractDslMarker
open class RequestDsl : CommonDsl(), RegexCreatingProperty<ClientDslProperty> {

    var method: DslProperty<Any>? = null
    var url: Url? = null
    var urlPath: UrlPath? = null
    var headers: Headers? = null
    var cookies: Cookies? = null
    var body: Body? = null
    var multipart: Multipart? = null
    var bodyMatchers: BodyMatchers? = null

    fun method(method: String) {
        this.method = DslProperty(method)
    }

    fun method(method: HttpMethods.HttpMethod) {
        this.method(method.toString())
    }

    fun url(url: String): Url {
        return Url(url)
    }

    fun url(url: DslProperty<Any>): Url {
        return Url(url)
    }

    fun urlPath(url: String): UrlPath {
        return UrlPath(url)
    }

    fun urlPath(url: DslProperty<Any>): UrlPath {
        return UrlPath(url)
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
        this.body = Body(body)
    }

    fun multipart(multipart: Map<String, Any>) {
        this.multipart = Multipart(multipart.toDslProperties())
    }

    fun multipart(multipart: List<Any>) {
        this.multipart = Multipart(multipart.toDslProperties())
    }

    fun multipart(multipart: DslProperty<Any>) {
        this.multipart = Multipart(multipart)
    }

    fun multipart(multipart: Any) {
        this.multipart = Multipart(multipart)
    }

    fun bodyMatchers(block: BodyMatchers.() -> Unit) {
        bodyMatchers = BodyMatchers().apply(block)
    }

    fun value(value: DslProperty<Any>): DslProperty<Any> = Request().value(value)

    fun v(value: DslProperty<Any>): DslProperty<Any> = Request().value(value)

    fun `$`(value: DslProperty<Any>): DslProperty<Any> = Request().value(value)

    fun value(value: Any): DslProperty<Any> = Request().value(value)

    fun v(value: Any): DslProperty<Any> = Request().value(value)

    fun `$`(value: Any): DslProperty<Any> = Request().value(value)

    fun value(client: ClientDslProperty, server: ServerDslProperty): DslProperty<Any> = Request().value(client, server)

    fun v(client: ClientDslProperty, server: ServerDslProperty): DslProperty<Any> = Request().value(client, server)

    fun `$`(client: ClientDslProperty, server: ServerDslProperty): DslProperty<Any> = Request().value(client, server)

    fun value(server: ServerDslProperty, client: ClientDslProperty): DslProperty<Any> = Request().value(client, server)

    fun v(server: ServerDslProperty, client: ClientDslProperty): DslProperty<Any> = Request().value(client, server)

    fun `$`(server: ServerDslProperty, client: ClientDslProperty): DslProperty<Any> = Request().value(client, server)

    override fun anyAlphaUnicode(): ClientDslProperty = Request().anyAlphaUnicode()

    override fun anyAlphaNumeric(): ClientDslProperty = Request().anyAlphaNumeric()

    override fun anyNumber(): ClientDslProperty = Request().anyNumber()

    override fun anyInteger(): ClientDslProperty = Request().anyInteger()

    override fun anyPositiveInt(): ClientDslProperty = Request().anyPositiveInt()

    override fun anyDouble(): ClientDslProperty = Request().anyDouble()

    override fun anyHex(): ClientDslProperty = Request().anyHex()

    override fun aBoolean(): ClientDslProperty = Request().aBoolean()

    override fun anyIpAddress(): ClientDslProperty = Request().anyIpAddress()

    override fun anyHostname(): ClientDslProperty = Request().anyHostname()

    override fun anyEmail(): ClientDslProperty = Request().anyEmail()

    override fun anyUrl(): ClientDslProperty = Request().anyUrl()

    override fun anyHttpsUrl(): ClientDslProperty = Request().anyHttpsUrl()

    override fun anyUuid(): ClientDslProperty = Request().anyUuid()

    override fun anyDate(): ClientDslProperty = Request().anyDate()

    override fun anyDateTime(): ClientDslProperty = Request().anyDateTime()

    override fun anyTime(): ClientDslProperty = Request().anyTime()

    override fun anyIso8601WithOffset(): ClientDslProperty = Request().anyIso8601WithOffset()

    override fun anyNonBlankString(): ClientDslProperty = Request().anyNonBlankString()

    override fun anyNonEmptyString(): ClientDslProperty = Request().anyNonEmptyString()

    override fun anyOf(vararg values: String?): ClientDslProperty = Request().anyOf(*values)

    internal fun get(): Request {
        val request = Request()
        method?.also { request.method = method!! }
        url?.also { request.url = url!! }
        urlPath?.also { request.urlPath = urlPath!! }
        headers?.also { request.headers = headers!! }
        cookies?.also { request.cookies = cookies!! }
        body?.also { request.body = body!! }
        multipart?.also { request.multipart = multipart!! }
        bodyMatchers?.also { request.bodyMatchers = bodyMatchers!! }
        return request
    }
}