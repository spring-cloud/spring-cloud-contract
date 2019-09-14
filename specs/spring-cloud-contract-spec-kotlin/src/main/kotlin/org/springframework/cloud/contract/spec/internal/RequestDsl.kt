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
import org.springframework.cloud.contract.spec.util.RegexpUtils
import java.util.regex.Pattern

/**
 * Represents the request side of the HTTP communication.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
@ContractDslMarker
open class RequestDsl : CommonDsl() {

    private val delegate = Request()

    /**
     * The HTTP method.
     */
    var method: DslProperty<*>? = null

    /**
     * The URL to which the request will be sent.
     */
    var url: Url? = null

    /**
     * The URL to which the request will be sent.
     */
    var urlPath: UrlPath? = null

    /**
     * The HTTP headers which should be sent with the request.
     */
    var headers: Headers? = null

    /**
     * The HTTP cookies which should be sent with the request.
     */
    var cookies: Cookies? = null

    /**
     * The HTTP request body which should be sent.
     */
    var body: Body? = null

    /**
     * The content that needs to be sent with a multipart HTTP request.
     */
    var multipart: Multipart? = null

    /**
     * The HTTP request body matchers.
     */
    var bodyMatchers: BodyMatchers? = null

    fun method(method: String) = method.toDslProperty()

    fun url(url: String) = Url(url)

    fun url(url: DslProperty<Any>) = Url(url)

    fun url(client: ClientDslProperty, server: ServerDslProperty) = Url(value(client, server))

    fun path(path: String) = UrlPath(path)

    fun path(path: DslProperty<Any>) = UrlPath(path)

    fun headers(headers: HeadersDsl.() -> Unit) {
        this.headers = RequestHeadersDsl().apply(headers).get()
    }

    fun cookies(cookies: CookiesDsl.() -> Unit) {
        this.cookies = RequestCookiesDsl().apply(cookies).get()
    }

    fun body(body: Map<String, Any>) = Body(body.toDslProperties())

    fun body(vararg body: Pair<String, Any>) = Body(body.toMap().toDslProperties())

    fun body(body: Pair<String, Any>) = Body(mapOf(body).toDslProperties())

    fun body(body: List<Any>) = Body(body.toDslProperties())

    fun body(body: Any) = Body(body)

    fun multipart(configurer: MultipartDsl.() -> Unit) {
        this.multipart = MultipartDsl().apply(configurer).get()
    }

    fun bodyMatchers(configurer: BodyMatchersDsl.() -> Unit) {
        this.bodyMatchers = BodyMatchersDsl().apply(configurer).get()
    }

    /* HELPER VARIABLES */

    /* HTTP METHODS */

    val GET: DslProperty<String>
        get() = method(HttpMethods.GET)

    val HEAD: DslProperty<String>
        get() = method(HttpMethods.HEAD)

    val POST: DslProperty<String>
        get() = method(HttpMethods.POST)

    val PUT: DslProperty<String>
        get() = method(HttpMethods.PUT)

    val PATCH: DslProperty<String>
        get() = method(HttpMethods.PATCH)

    val DELETE: DslProperty<String>
        get() = method(HttpMethods.DELETE)

    val OPTIONS: DslProperty<String>
        get() = method(HttpMethods.OPTIONS)

    val TRACE: DslProperty<String>
        get() = method(HttpMethods.TRACE)

    /* REGEX */

    val anyAlphaUnicode: ClientDslProperty
        get() = delegate.anyAlphaUnicode()

    val anyAlphaNumeric: ClientDslProperty
        get() = delegate.anyAlphaNumeric()

    val anyNumber: ClientDslProperty
        get() = delegate.anyNumber()

    val anyInteger: ClientDslProperty
        get() = delegate.anyInteger()

    val anyPositiveInt: ClientDslProperty
        get() = delegate.anyPositiveInt()

    val anyDouble: ClientDslProperty
        get() = delegate.anyDouble()

    val anyHex: ClientDslProperty
        get() = delegate.anyHex()

    val aBoolean: ClientDslProperty
        get() = delegate.aBoolean()

    val anyIpAddress: ClientDslProperty
        get() = delegate.anyIpAddress()

    val anyHostname: ClientDslProperty
        get() = delegate.anyHostname()

    val anyEmail: ClientDslProperty
        get() = delegate.anyEmail()

    val anyUrl: ClientDslProperty
        get() = delegate.anyUrl()

    val anyHttpsUrl: ClientDslProperty
        get() = delegate.anyHttpsUrl()

    val anyUuid: ClientDslProperty
        get() = delegate.anyUuid()

    val anyDate: ClientDslProperty
        get() = delegate.anyDate()

    val anyDateTime: ClientDslProperty
        get() = delegate.anyDateTime()

    val anyTime: ClientDslProperty
        get() = delegate.anyTime()

    val anyIso8601WithOffset: ClientDslProperty
        get() = delegate.anyIso8601WithOffset()

    val anyNonBlankString: ClientDslProperty
        get() = delegate.anyNonBlankString()

    val anyNonEmptyString: ClientDslProperty
        get() = delegate.anyNonEmptyString()

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

    fun value(value: ClientDslProperty): DslProperty<Any> = delegate.value(value)

    fun v(value: ClientDslProperty): DslProperty<Any> = delegate.value(value)

    fun value(value: DslProperty<Any>): DslProperty<Any> = delegate.value(value)

    fun v(value: DslProperty<Any>): DslProperty<Any> = delegate.value(value)

    fun value(value: Pattern): DslProperty<Any> = delegate.value(value)

    fun v(value: Pattern): DslProperty<Any> = delegate.value(value)

    fun value(value: RegexProperty): DslProperty<Any> = delegate.value(value)

    fun v(value: RegexProperty): DslProperty<Any> = delegate.value(value)

    fun value(value: Any?): DslProperty<Any> = delegate.value(value)

    fun v(value: Any?): DslProperty<Any> = delegate.value(value)

    fun value(client: ClientDslProperty, server: ServerDslProperty): DslProperty<Any> = delegate.value(client, server)

    fun v(client: ClientDslProperty, server: ServerDslProperty): DslProperty<Any> = delegate.value(client, server)

    fun value(server: ServerDslProperty, client: ClientDslProperty): DslProperty<Any> = delegate.value(client, server)

    fun v(server: ServerDslProperty, client: ClientDslProperty): DslProperty<Any> = delegate.value(client, server)

    fun anyOf(vararg values: String?): ClientDslProperty = delegate.anyOf(*values)

    internal fun get(): Request {
        val request = Request()
        method?.also { request.method = method }
        url?.also { request.url = url }
        urlPath?.also { request.urlPath = urlPath }
        headers?.also { request.headers = headers }
        cookies?.also { request.cookies = cookies }
        body?.also { request.body = body }
        multipart?.also { request.multipart = multipart }
        bodyMatchers?.also { request.bodyMatchers = bodyMatchers }
        return request
    }

    private class RequestHeadersDsl : HeadersDsl() {

        private val common = Common()

        override fun matching(value: Any?): Any? {
            return value?.also {
                return when (value) {
                    is String -> this.common.value(
                            c(regex(RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*")),
                            p(value)
                    )
                    else -> value
                }
            }
        }

    }

    private class RequestCookiesDsl : CookiesDsl() {

        private val common = Common()

        override fun matching(value: Any?): Any? {
            return value?.also {
                return when (value) {
                    is String -> this.common.value(
                            c(regex(RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*")),
                            p(value)
                    )
                    else -> value
                }
            }
        }

    }
}