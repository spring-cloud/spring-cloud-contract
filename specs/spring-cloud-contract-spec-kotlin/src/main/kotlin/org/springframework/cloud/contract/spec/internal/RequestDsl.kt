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
open class RequestDsl : CommonDsl(), RegexCreatingProperty<ClientDslProperty> {
    
    private val delegate = Request()

    var method: DslProperty<Any>? = null
    var url: Url? = null
    var urlPath: UrlPath? = null
    var headers: Headers? = null
    var cookies: Cookies? = null
    var body: Body? = null
    var multipart: Multipart? = null
    var bodyMatchers: BodyMatchers? = null

    fun method(method: String) = method.toDslProperty()

    fun url(url: String) = Url(url)

    fun url(url: DslProperty<Any>) = Url(url)

    fun url(client: ClientDslProperty, server: ServerDslProperty) = Url(value(client, server))

    fun path(path: String) = UrlPath(path)

    fun path(path: DslProperty<Any>) = UrlPath(path)

    fun headers(headers: Headers.() -> Unit) {
        this.headers = Request.RequestHeaders().apply(headers)
    }

    fun cookies(cookies: Cookies.() -> Unit) {
        this.cookies = Request.RequestCookies().apply(cookies)
    }

    fun body(body: Map<String, Any>) = Body(body.toDslProperties())

    fun body(vararg body: Pair<String, Any>) = Body(body.toMap().toDslProperties())

    fun body(body: Pair<String, Any>) = Body(mapOf(body).toDslProperties())

    fun body(body: List<Any>) = Body(body.toDslProperties())

    fun body(body: DslProperty<Any>) = Body(body)

    fun body(body: Any) = Body(body)

    fun multipart(multipart: Map<String, Any>) = Multipart(multipart.toDslProperties())

    fun multipart(multipart: List<Any>) = Multipart(multipart.toDslProperties())

    fun multipart(multipart: DslProperty<Any>) = Multipart(multipart)

    fun multipart(multipart: Any) = Multipart(multipart)

    fun bodyMatchers(block: BodyMatchers.() -> Unit) {
        bodyMatchers = BodyMatchers().apply(block)
    }

    /* HELPER VARIABLES */

    /* HTTP METHODS */

    val GET = method(HttpMethods.HttpMethod.GET.name)

    val HEAD = method(HttpMethods.HttpMethod.HEAD.name)

    val POST = method(HttpMethods.HttpMethod.POST.name)

    val PUT = method(HttpMethods.HttpMethod.PUT.name)

    val PATCH = method(HttpMethods.HttpMethod.PATCH.name)

    val DELETE = method(HttpMethods.HttpMethod.DELETE.name)

    val OPTIONS = method(HttpMethods.HttpMethod.OPTIONS.name)

    val TRACE = method(HttpMethods.HttpMethod.TRACE.name)

    /* HELPER FUNCTIONS */

    /**
     * Sets the equality check to the given query parameter.
     * @param value value to check against
     * @return matching strategy
     */
    fun equalTo(value: Any) = MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO)

    /**
     * Sets the containing check to the given query parameter.
     * @param value value to check against
     * @return matching strategy
     */
    fun containing(value: Any) = MatchingStrategy(value, MatchingStrategy.Type.CONTAINS)

    /**
     * Sets the matching check to the given query parameter.
     * @param value value to check against
     * @return matching strategy
     */
    fun matching(value: Any) = MatchingStrategy(value, MatchingStrategy.Type.MATCHING)

    /**
     * Sets the not matching check to the given query parameter.
     * @param value value to check against
     * @return matching strategy
     */
    fun notMatching(value: Any) = MatchingStrategy(value, MatchingStrategy.Type.NOT_MATCHING)

    /**
     * Sets the XML equality check to the body.
     * @param value value to check against
     * @return matching strategy
     */
    fun equalToXml(value: Any) = MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_XML)

    /**
     * Sets the JSON equality check to the body.
     * @param value value to check against
     * @return matching strategy
     */
    fun equalToJson(value: Any) = MatchingStrategy(value, MatchingStrategy.Type.EQUAL_TO_JSON)

    /**
     * Sets absence check to the given query parameter.
     * @return matching strategy
     */
    fun absent() = MatchingStrategy(true, MatchingStrategy.Type.ABSENT)

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