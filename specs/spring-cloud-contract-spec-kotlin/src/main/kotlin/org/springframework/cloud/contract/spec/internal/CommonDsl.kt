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

import org.springframework.cloud.contract.spec.internal.HttpMethods.HttpMethod
import java.util.regex.Pattern

/**
 * @author Tim Ysewyn
 */
open class CommonDsl {
    
    var httpStatus = HttpStatus()
    var regexPatterns = RegexPatterns()

    /**
     * Helper method to provide a better name for the consumer side.
     * @param clientValue client value
     * @return client dsl property
     */
    fun client(clientValue: Any) = ClientDslProperty(clientValue)

    /**
     * Helper method to provide a better name for the consumer side.
     * @param clientValue client value
     * @return client dsl property
     */
    fun c(clientValue: Any) = ClientDslProperty(clientValue)

    /**
     * Helper method to provide a better name for the consumer side.
     * @param clientValue client value
     * @return client dsl property
     */
    fun consumer(clientValue: Any) = ClientDslProperty(clientValue)

    /**
     * Helper method to provide a better name for the consumer side.
     * @param clientValue client value
     * @return client dsl property
     */
    fun stub(clientValue: Any) = ClientDslProperty(clientValue)

    /**
     * Helper method to provide a better name for the server side.
     * @param serverValue server value
     * @return server dsl property
     */
    fun server(serverValue: Any) = ServerDslProperty(serverValue)

    /**
     * Helper method to provide a better name for the server side.
     * @param serverValue server value
     * @return server dsl property
     */
    fun p(serverValue: Any) = ServerDslProperty(serverValue)

    /**
     * Helper method to provide a better name for the server side.
     * @param serverValue server value
     * @return server dsl property
     */
    fun producer(serverValue: Any) = ServerDslProperty(serverValue)

    /**
     * Helper method to provide a better name for the server side.
     * @param serverValue server value
     * @return server dsl property
     */
    fun test(serverValue: Any) = ServerDslProperty(serverValue)

    /* HTTP METHODS */

    fun GET() = HttpMethod.GET

    fun HEAD() = HttpMethod.HEAD

    fun POST() = HttpMethod.POST

    fun PUT() = HttpMethod.PUT

    fun PATCH() = HttpMethod.PATCH

    fun DELETE() = HttpMethod.DELETE

    fun OPTIONS() = HttpMethod.OPTIONS

    fun TRACE() = HttpMethod.TRACE

    /* HTTP STATUS CODES */

    fun CONTINUE() = httpStatus.CONTINUE()

    fun SWITCHING_PROTOCOLS() = httpStatus.SWITCHING_PROTOCOLS()

    fun PROCESSING() = httpStatus.PROCESSING()

    fun CHECKPOINT() = httpStatus.CHECKPOINT()

    fun OK() = httpStatus.OK()

    fun CREATED() = httpStatus.CREATED()

    fun ACCEPTED() = httpStatus.ACCEPTED()

    fun NON_AUTHORITATIVE_INFORMATION() = httpStatus.NON_AUTHORITATIVE_INFORMATION()

    fun NO_CONTENT() = httpStatus.NO_CONTENT()

    fun RESET_CONTENT() = httpStatus.RESET_CONTENT()

    fun PARTIAL_CONTENT() = httpStatus.PARTIAL_CONTENT()

    fun MULTI_STATUS() = httpStatus.MULTI_STATUS()

    fun ALREADY_REPORTED() = httpStatus.ALREADY_REPORTED()

    fun IM_USED() = httpStatus.IM_USED()

    fun MULTIPLE_CHOICES() = httpStatus.MULTIPLE_CHOICES()

    fun MOVED_PERMANENTLY() = httpStatus.MOVED_PERMANENTLY()

    fun FOUND() = httpStatus.FOUND()

    fun SEE_OTHER() = httpStatus.SEE_OTHER()

    fun NOT_MODIFIED() = httpStatus.NOT_MODIFIED()

    fun TEMPORARY_REDIRECT() = httpStatus.TEMPORARY_REDIRECT()

    fun PERMANENT_REDIRECT() = httpStatus.PERMANENT_REDIRECT()

    fun BAD_REQUEST() = httpStatus.BAD_REQUEST()

    fun UNAUTHORIZED() = httpStatus.UNAUTHORIZED()

    fun PAYMENT_REQUIRED() = httpStatus.PAYMENT_REQUIRED()

    fun FORBIDDEN() = httpStatus.FORBIDDEN()

    fun NOT_FOUND() = httpStatus.NOT_FOUND()

    fun METHOD_NOT_ALLOWED() = httpStatus.METHOD_NOT_ALLOWED()

    fun NOT_ACCEPTABLE() = httpStatus.NOT_ACCEPTABLE()

    fun PROXY_AUTHENTICATION_REQUIRED() = httpStatus.PROXY_AUTHENTICATION_REQUIRED()

    fun REQUEST_TIMEOUT() = httpStatus.REQUEST_TIMEOUT()

    fun CONFLICT() = httpStatus.CONFLICT()

    fun GONE() = httpStatus.GONE()

    fun LENGTH_REQUIRED() = httpStatus.LENGTH_REQUIRED()

    fun PRECONDITION_FAILED() = httpStatus.PRECONDITION_FAILED()

    fun PAYLOAD_TOO_LARGE() = httpStatus.PAYLOAD_TOO_LARGE()

    fun URI_TOO_LONG() = httpStatus.URI_TOO_LONG()

    fun UNSUPPORTED_MEDIA_TYPE() = httpStatus.UNSUPPORTED_MEDIA_TYPE()

    fun REQUESTED_RANGE_NOT_SATISFIABLE() = httpStatus.REQUESTED_RANGE_NOT_SATISFIABLE()

    fun EXPECTATION_FAILED() = httpStatus.EXPECTATION_FAILED()

    fun I_AM_A_TEAPOT() = httpStatus.I_AM_A_TEAPOT()

    fun UNPROCESSABLE_ENTITY() = httpStatus.UNPROCESSABLE_ENTITY()

    fun LOCKED() = httpStatus.LOCKED()

    fun FAILED_DEPENDENCY() = httpStatus.FAILED_DEPENDENCY()

    fun UPGRADE_REQUIRED() = httpStatus.UPGRADE_REQUIRED()

    fun PRECONDITION_REQUIRED() = httpStatus.PRECONDITION_REQUIRED()

    fun TOO_MANY_REQUESTS() = httpStatus.TOO_MANY_REQUESTS()

    fun REQUEST_HEADER_FIELDS_TOO_LARGE() = httpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE()

    fun UNAVAILABLE_FOR_LEGAL_REASONS() = httpStatus.UNAVAILABLE_FOR_LEGAL_REASONS()

    fun INTERNAL_SERVER_ERROR() = httpStatus.INTERNAL_SERVER_ERROR()

    fun NOT_IMPLEMENTED() = httpStatus.NOT_IMPLEMENTED()

    fun BAD_GATEWAY() = httpStatus.BAD_GATEWAY()

    fun SERVICE_UNAVAILABLE() = httpStatus.SERVICE_UNAVAILABLE()

    fun GATEWAY_TIMEOUT() = httpStatus.GATEWAY_TIMEOUT()

    fun HTTP_VERSION_NOT_SUPPORTED() = httpStatus.HTTP_VERSION_NOT_SUPPORTED()

    fun VARIANT_ALSO_NEGOTIATES() = httpStatus.VARIANT_ALSO_NEGOTIATES()

    fun INSUFFICIENT_STORAGE() = httpStatus.INSUFFICIENT_STORAGE()

    fun LOOP_DETECTED() = httpStatus.LOOP_DETECTED()

    fun BANDWIDTH_LIMIT_EXCEEDED() = httpStatus.BANDWIDTH_LIMIT_EXCEEDED()

    fun NOT_EXTENDED() = httpStatus.NOT_EXTENDED()

    fun NETWORK_AUTHENTICATION_REQUIRED() = httpStatus.NETWORK_AUTHENTICATION_REQUIRED()

    /* REGEX */

    fun regexProperty(value: Any) = RegexProperty(value)

    fun regex(regex: String) = regexProperty(Pattern.compile(regex))

    fun regex(regex: Pattern) = regexProperty(regex)

    fun regex(regex: RegexProperty) = regex

    fun onlyAlphaUnicode(): RegexProperty = regexPatterns.onlyAlphaUnicode()

    fun alphaNumeric(): RegexProperty = regexPatterns.alphaNumeric()

    fun number(): RegexProperty = regexPatterns.number()

    fun positiveInt(): RegexProperty = regexPatterns.positiveInt()

    fun anyBoolean(): RegexProperty = regexPatterns.anyBoolean()

    fun anInteger(): RegexProperty = regexPatterns.anInteger()

    fun aDouble(): RegexProperty = regexPatterns.aDouble()

    fun ipAddress(): RegexProperty = regexPatterns.ipAddress()

    fun hostname(): RegexProperty = regexPatterns.hostname()

    fun email(): RegexProperty = regexPatterns.email()

    fun url(): RegexProperty = regexPatterns.url()

    fun httpsUrl(): RegexProperty = regexPatterns.httpsUrl()

    fun uuid(): RegexProperty = regexPatterns.uuid()

    fun isoDate(): RegexProperty = regexPatterns.isoDate()

    fun isoDateTime(): RegexProperty = regexPatterns.isoDateTime()

    fun isoTime(): RegexProperty = regexPatterns.isoTime()

    fun iso8601WithOffset(): RegexProperty = regexPatterns.iso8601WithOffset()

    fun nonEmpty(): RegexProperty = regexPatterns.nonEmpty()

    fun nonBlank(): RegexProperty = regexPatterns.nonBlank()
}