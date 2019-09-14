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
 * Represents the response side of the HTTP communication.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
@ContractDslMarker
class ResponseDsl : CommonDsl() {

    private val delegate = Response()

    /**
     * The HTTP response status.
     */
    var status: DslProperty<Any>? = null

    /**
     * The HTTP response delay in milliseconds.
     */
    var delay: DslProperty<Any>? = null

    /**
     * The HTTP response headers.
     */
    var headers: Headers? = null

    /**
     * The HTTP response cookies.
     */
    var cookies: Cookies? = null

    /**
     * The HTTP response body.
     */
    var body: Body? = null

    /**
     * Indicates asynchronous communication.
     */
    var async: Boolean = false

    /**
     * The HTTP response body matchers.
     */
    var bodyMatchers: ResponseBodyMatchers? = null

    fun code(code: Int): DslProperty<Any> = code.toDslProperty()

    fun fixedMilliseconds(delay: Long): DslProperty<Any> = delay.toDslProperty()

    fun headers(headers: HeadersDsl.() -> Unit) {
        this.headers = ResponseHeadersDsl().apply(headers).get()
    }

    fun cookies(cookies: CookiesDsl.() -> Unit) {
        this.cookies = ResponseCookiesDsl().apply(cookies).get()
    }

    fun body(body: Map<String, Any>) = Body(body.toDslProperties())

    fun body(vararg body: Pair<String, Any>) = Body(body.toMap().toDslProperties())

    fun body(body: Pair<String, Any>) = Body(mapOf(body).toDslProperties())

    fun body(body: List<Any>) = Body(body.toDslProperties())

    fun body(body: Any) = Body(body)

    fun bodyMatchers(configurer: ResponseBodyMatchersDsl.() -> Unit) {
        bodyMatchers = ResponseBodyMatchersDsl().apply(configurer).get()
    }

    /* HELPER VARIABLES */

    /* HTTP STATUS CODES */

    val CONTINUE
        get() = code(HttpStatus.CONTINUE)

    val SWITCHING_PROTOCOLS
        get() = code(HttpStatus.SWITCHING_PROTOCOLS)

    val PROCESSING
        get() = code(HttpStatus.PROCESSING)

    val CHECKPOINT
        get() = code(HttpStatus.CHECKPOINT)

    val OK
        get() = code(HttpStatus.OK)

    val CREATED
        get() = code(HttpStatus.CREATED)

    val ACCEPTED
        get() = code(HttpStatus.ACCEPTED)

    val NON_AUTHORITATIVE_INFORMATION
        get() = code(HttpStatus.NON_AUTHORITATIVE_INFORMATION)

    val NO_CONTENT
        get() = code(HttpStatus.NO_CONTENT)

    val RESET_CONTENT
        get() = code(HttpStatus.RESET_CONTENT)

    val PARTIAL_CONTENT
        get() = code(HttpStatus.PARTIAL_CONTENT)

    val MULTI_STATUS
        get() = code(HttpStatus.MULTI_STATUS)

    val ALREADY_REPORTED
        get() = code(HttpStatus.ALREADY_REPORTED)

    val IM_USED
        get() = code(HttpStatus.IM_USED)

    val MULTIPLE_CHOICES
        get() = code(HttpStatus.MULTIPLE_CHOICES)

    val MOVED_PERMANENTLY
        get() = code(HttpStatus.MOVED_PERMANENTLY)

    val FOUND
        get() = code(HttpStatus.FOUND)

    val SEE_OTHER
        get() = code(HttpStatus.SEE_OTHER)

    val NOT_MODIFIED
        get() = code(HttpStatus.NOT_MODIFIED)

    val TEMPORARY_REDIRECT
        get() = code(HttpStatus.TEMPORARY_REDIRECT)

    val PERMANENT_REDIRECT
        get() = code(HttpStatus.PERMANENT_REDIRECT)

    val BAD_REQUEST
        get() = code(HttpStatus.BAD_REQUEST)

    val UNAUTHORIZED
        get() = code(HttpStatus.UNAUTHORIZED)

    val PAYMENT_REQUIRED
        get() = code(HttpStatus.PAYMENT_REQUIRED)

    val FORBIDDEN
        get() = code(HttpStatus.FORBIDDEN)

    val NOT_FOUND
        get() = code(HttpStatus.NOT_FOUND)

    val METHOD_NOT_ALLOWED
        get() = code(HttpStatus.METHOD_NOT_ALLOWED)

    val NOT_ACCEPTABLE
        get() = code(HttpStatus.NOT_ACCEPTABLE)

    val PROXY_AUTHENTICATION_REQUIRED
        get() = code(HttpStatus.PROXY_AUTHENTICATION_REQUIRED)

    val REQUEST_TIMEOUT
        get() = code(HttpStatus.REQUEST_TIMEOUT)

    val CONFLICT
        get() = code(HttpStatus.CONFLICT)

    val GONE
        get() = code(HttpStatus.GONE)

    val LENGTH_REQUIRED
        get() = code(HttpStatus.LENGTH_REQUIRED)

    val PRECONDITION_FAILED
        get() = code(HttpStatus.PRECONDITION_FAILED)

    val PAYLOAD_TOO_LARGE
        get() = code(HttpStatus.PAYLOAD_TOO_LARGE)

    val UNSUPPORTED_MEDIA_TYPE
        get() = code(HttpStatus.UNSUPPORTED_MEDIA_TYPE)

    val REQUESTED_RANGE_NOT_SATISFIABLE
        get() = code(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)

    val EXPECTATION_FAILED
        get() = code(HttpStatus.EXPECTATION_FAILED)

    val I_AM_A_TEAPOT
        get() = code(HttpStatus.I_AM_A_TEAPOT)

    val UNPROCESSABLE_ENTITY
        get() = code(HttpStatus.UNPROCESSABLE_ENTITY)

    val LOCKED
        get() = code(HttpStatus.LOCKED)

    val FAILED_DEPENDENCY
        get() = code(HttpStatus.FAILED_DEPENDENCY)

    val UPGRADE_REQUIRED
        get() = code(HttpStatus.UPGRADE_REQUIRED)

    val PRECONDITION_REQUIRED
        get() = code(HttpStatus.PRECONDITION_REQUIRED)

    val TOO_MANY_REQUESTS
        get() = code(HttpStatus.TOO_MANY_REQUESTS)

    val REQUEST_HEADER_FIELDS_TOO_LARGE
        get() = code(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)

    val UNAVAILABLE_FOR_LEGAL_REASONS
        get() = code(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)

    val INTERNAL_SERVER_ERROR
        get() = code(HttpStatus.INTERNAL_SERVER_ERROR)

    val NOT_IMPLEMENTED
        get() = code(HttpStatus.NOT_IMPLEMENTED)

    val BAD_GATEWAY
        get() = code(HttpStatus.BAD_GATEWAY)

    val SERVICE_UNAVAILABLE
        get() = code(HttpStatus.SERVICE_UNAVAILABLE)

    val GATEWAY_TIMEOUT
        get() = code(HttpStatus.GATEWAY_TIMEOUT)

    val HTTP_VERSION_NOT_SUPPORTED
        get() = code(HttpStatus.HTTP_VERSION_NOT_SUPPORTED)

    val VARIANT_ALSO_NEGOTIATES
        get() = code(HttpStatus.VARIANT_ALSO_NEGOTIATES)

    val INSUFFICIENT_STORAGE
        get() = code(HttpStatus.INSUFFICIENT_STORAGE)

    val LOOP_DETECTED
        get() = code(HttpStatus.LOOP_DETECTED)

    val BANDWIDTH_LIMIT_EXCEEDED
        get() = code(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)

    val NOT_EXTENDED
        get() = code(HttpStatus.NOT_EXTENDED)

    val NETWORK_AUTHENTICATION_REQUIRED
        get() = code(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED)

    /* REGEX */

    val anyAlphaUnicode
        get() = delegate.anyAlphaUnicode()

    val anyAlphaNumeric
        get() = delegate.anyAlphaNumeric()

    val anyNumber
        get() = delegate.anyNumber()

    val anyInteger
        get() = delegate.anyInteger()

    val anyPositiveInt
        get() = delegate.anyPositiveInt()

    val anyDouble
        get() = delegate.anyDouble()

    val anyHex
        get() = delegate.anyHex()

    val aBoolean
        get() = delegate.aBoolean()

    val anyIpAddress
        get() = delegate.anyIpAddress()

    val anyHostname
        get() = delegate.anyHostname()

    val anyEmail
        get() = delegate.anyEmail()

    val anyUrl
        get() = delegate.anyUrl()

    val anyHttpsUrl
        get() = delegate.anyHttpsUrl()

    val anyUuid
        get() = delegate.anyUuid()

    val anyDate
        get() = delegate.anyDate()

    val anyDateTime
        get() = delegate.anyDateTime()

    val anyTime
        get() = delegate.anyTime()

    val anyIso8601WithOffset
        get() = delegate.anyIso8601WithOffset()

    val anyNonBlankString
        get() = delegate.anyNonBlankString()

    val anyNonEmptyString
        get() = delegate.anyNonEmptyString()

    /* HELPER FUNCTIONS */

    fun value(value: ClientDslProperty) = delegate.value(value)

    fun v(value: ClientDslProperty) = delegate.value(value)

    fun value(value: DslProperty<Any>) = delegate.value(value)

    fun v(value: DslProperty<Any>) = delegate.value(value)

    fun value(value: Pattern) = delegate.value(value)

    fun v(value: Pattern) = delegate.value(value)

    fun value(value: RegexProperty) = delegate.value(value)

    fun v(value: RegexProperty) = delegate.value(value)

    fun value(value: Any?) = delegate.value(value)

    fun v(value: Any?) = delegate.value(value)

    fun value(client: ClientDslProperty, server: ServerDslProperty) = delegate.value(client, server)

    fun v(client: ClientDslProperty, server: ServerDslProperty) = delegate.value(client, server)

    fun value(server: ServerDslProperty, client: ClientDslProperty) = delegate.value(client, server)

    fun v(server: ServerDslProperty, client: ClientDslProperty) = delegate.value(client, server)

    fun fromRequest() = FromRequestDsl()

    fun anyOf(vararg values: String?) = delegate.anyOf(*values)

    internal fun get(): Response {
        val response = Response()
        status?.also { response.status = status }
        delay?.also { response.delay = delay }
        headers?.also { response.headers = headers }
        cookies?.also { response.cookies = cookies }
        body?.also { response.body = body }
        response.async = async
        bodyMatchers?.also { response.bodyMatchers = bodyMatchers }
        return response
    }

    private class ResponseHeadersDsl : HeadersDsl() {

        private val common = Common()

        override fun matching(value: Any?): Any? {
            return value?.also {
                return when (value) {
                    is String -> return this.common.value(
                            c(value),
                            p(NotToEscapePattern(Pattern.compile(RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*")))
                    )
                    else -> value
                }
            }
        }

    }

    private class ResponseCookiesDsl : CookiesDsl() {

        private val common = Common()

        override fun matching(value: Any?): Any? {
            return value?.also {
                return when (value) {
                    is String -> return this.common.value(
                            c(value),
                            p(regex(RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*"))
                    )
                    else -> value
                }
            }
        }

    }
}
