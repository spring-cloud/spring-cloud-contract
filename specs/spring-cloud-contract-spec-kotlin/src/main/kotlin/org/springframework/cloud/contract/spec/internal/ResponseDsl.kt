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

    fun code(code: Int): DslProperty<Any> = code.toDslProperty()

    fun fixedMilliseconds(delay: Long) = delay.toDslProperty()

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
    
    val CONTINUE = code(HttpStatus.CONTINUE)

	val SWITCHING_PROTOCOLS = code(HttpStatus.SWITCHING_PROTOCOLS)

	val PROCESSING = code(HttpStatus.PROCESSING)

	val CHECKPOINT = code(HttpStatus.CHECKPOINT)

    val OK = code(HttpStatus.OK)

	val CREATED = code(HttpStatus.CREATED)

	val ACCEPTED = code(HttpStatus.ACCEPTED)

	val NON_AUTHORITATIVE_INFORMATION = code(HttpStatus.NON_AUTHORITATIVE_INFORMATION)

	val NO_CONTENT = code(HttpStatus.NO_CONTENT)

	val RESET_CONTENT = code(HttpStatus.RESET_CONTENT)

	val PARTIAL_CONTENT = code(HttpStatus.PARTIAL_CONTENT)

	val MULTI_STATUS = code(HttpStatus.MULTI_STATUS)

	val ALREADY_REPORTED = code(HttpStatus.ALREADY_REPORTED)

	val IM_USED = code(HttpStatus.IM_USED)

	val MULTIPLE_CHOICES = code(HttpStatus.MULTIPLE_CHOICES)

	val MOVED_PERMANENTLY = code(HttpStatus.MOVED_PERMANENTLY)

	val FOUND = code(HttpStatus.FOUND)

	val SEE_OTHER = code(HttpStatus.SEE_OTHER)

	val NOT_MODIFIED = code(HttpStatus.NOT_MODIFIED)

	val TEMPORARY_REDIRECT = code(HttpStatus.TEMPORARY_REDIRECT)

	val PERMANENT_REDIRECT = code(HttpStatus.PERMANENT_REDIRECT)

	val BAD_REQUEST = code(HttpStatus.BAD_REQUEST)

	val UNAUTHORIZED = code(HttpStatus.UNAUTHORIZED)

	val PAYMENT_REQUIRED = code(HttpStatus.PAYMENT_REQUIRED)

	val FORBIDDEN = code(HttpStatus.FORBIDDEN)

	val NOT_FOUND = code(HttpStatus.NOT_FOUND)

	val METHOD_NOT_ALLOWED = code(HttpStatus.METHOD_NOT_ALLOWED)

	val NOT_ACCEPTABLE = code(HttpStatus.NOT_ACCEPTABLE)

	val PROXY_AUTHENTICATION_REQUIRED = code(HttpStatus.PROXY_AUTHENTICATION_REQUIRED)

	val REQUEST_TIMEOUT = code(HttpStatus.REQUEST_TIMEOUT)

	val CONFLICT = code(HttpStatus.CONFLICT)

	val GONE = code(HttpStatus.GONE)

	val LENGTH_REQUIRED = code(HttpStatus.LENGTH_REQUIRED)

	val PRECONDITION_FAILED = code(HttpStatus.PRECONDITION_FAILED)

	val PAYLOAD_TOO_LARGE = code(HttpStatus.PAYLOAD_TOO_LARGE)

	val UNSUPPORTED_MEDIA_TYPE = code(HttpStatus.UNSUPPORTED_MEDIA_TYPE)

	val REQUESTED_RANGE_NOT_SATISFIABLE = code(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)

	val EXPECTATION_FAILED = code(HttpStatus.EXPECTATION_FAILED)

	val I_AM_A_TEAPOT = code(HttpStatus.I_AM_A_TEAPOT)

	val UNPROCESSABLE_ENTITY = code(HttpStatus.UNPROCESSABLE_ENTITY)

	val LOCKED = code(HttpStatus.LOCKED)

	val FAILED_DEPENDENCY = code(HttpStatus.FAILED_DEPENDENCY)

	val UPGRADE_REQUIRED = code(HttpStatus.UPGRADE_REQUIRED)

	val PRECONDITION_REQUIRED = code(HttpStatus.PRECONDITION_REQUIRED)

	val TOO_MANY_REQUESTS = code(HttpStatus.TOO_MANY_REQUESTS)

	val REQUEST_HEADER_FIELDS_TOO_LARGE = code(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)

	val UNAVAILABLE_FOR_LEGAL_REASONS = code(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)

	val INTERNAL_SERVER_ERROR = code(HttpStatus.INTERNAL_SERVER_ERROR)

	val NOT_IMPLEMENTED = code(HttpStatus.NOT_IMPLEMENTED)

	val BAD_GATEWAY = code(HttpStatus.BAD_GATEWAY)

	val SERVICE_UNAVAILABLE = code(HttpStatus.SERVICE_UNAVAILABLE)

	val GATEWAY_TIMEOUT = code(HttpStatus.GATEWAY_TIMEOUT)

	val HTTP_VERSION_NOT_SUPPORTED = code(HttpStatus.HTTP_VERSION_NOT_SUPPORTED)

	val VARIANT_ALSO_NEGOTIATES = code(HttpStatus.VARIANT_ALSO_NEGOTIATES)

	val INSUFFICIENT_STORAGE = code(HttpStatus.INSUFFICIENT_STORAGE)

	val LOOP_DETECTED = code(HttpStatus.LOOP_DETECTED)

	val BANDWIDTH_LIMIT_EXCEEDED = code(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)

	val NOT_EXTENDED = code(HttpStatus.NOT_EXTENDED)

	val NETWORK_AUTHENTICATION_REQUIRED = code(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED)

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

//    TODO, needs to be reworked - no lazy string interpolation like in Groovy
//    fun fromRequest() = FromRequest()

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

	private class ResponseHeadersDsl: HeadersDsl() {

		private val common = Common()

		override fun matching(value: Any?): Any? {
			return value?.also {
				return when(value) {
					is String -> return this.common.value(
							c(value),
							p(NotToEscapePattern(Pattern.compile(RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*")))
					)
					else -> value
				}
			}
		}

	}

	private class ResponseCookiesDsl: CookiesDsl() {

		private val common = Common()

		override fun matching(value: Any?): Any? {
			return value?.also {
				return when(value) {
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
