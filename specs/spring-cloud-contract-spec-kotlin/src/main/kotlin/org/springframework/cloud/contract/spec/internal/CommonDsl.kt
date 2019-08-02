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

    fun CONTINUE() = HttpStatus.CONTINUE()

    fun SWITCHING_PROTOCOLS() = HttpStatus.SWITCHING_PROTOCOLS()

    fun PROCESSING() = HttpStatus.PROCESSING()

    fun CHECKPOINT() = HttpStatus.CHECKPOINT()

    fun OK() = HttpStatus.OK()

    fun CREATED() = HttpStatus.CREATED()

    fun ACCEPTED() = HttpStatus.ACCEPTED()

    fun NON_AUTHORITATIVE_INFORMATION() = HttpStatus.NON_AUTHORITATIVE_INFORMATION()

    fun NO_CONTENT() = HttpStatus.NO_CONTENT()

    fun RESET_CONTENT() = HttpStatus.RESET_CONTENT()

    fun PARTIAL_CONTENT() = HttpStatus.PARTIAL_CONTENT()

    fun MULTI_STATUS() = HttpStatus.MULTI_STATUS()

    fun ALREADY_REPORTED() = HttpStatus.ALREADY_REPORTED()

    fun IM_USED() = HttpStatus.IM_USED()

    fun MULTIPLE_CHOICES() = HttpStatus.MULTIPLE_CHOICES()

    fun MOVED_PERMANENTLY() = HttpStatus.MOVED_PERMANENTLY()

    fun FOUND() = HttpStatus.FOUND()

    fun SEE_OTHER() = HttpStatus.SEE_OTHER()

    fun NOT_MODIFIED() = HttpStatus.NOT_MODIFIED()

    fun TEMPORARY_REDIRECT() = HttpStatus.TEMPORARY_REDIRECT()

    fun PERMANENT_REDIRECT() = HttpStatus.PERMANENT_REDIRECT()

    fun BAD_REQUEST() = HttpStatus.BAD_REQUEST()

    fun UNAUTHORIZED() = HttpStatus.UNAUTHORIZED()

    fun PAYMENT_REQUIRED() = HttpStatus.PAYMENT_REQUIRED()

    fun FORBIDDEN() = HttpStatus.FORBIDDEN()

    fun NOT_FOUND() = HttpStatus.NOT_FOUND()

    fun METHOD_NOT_ALLOWED() = HttpStatus.METHOD_NOT_ALLOWED()

    fun NOT_ACCEPTABLE() = HttpStatus.NOT_ACCEPTABLE()

    fun PROXY_AUTHENTICATION_REQUIRED() = HttpStatus.PROXY_AUTHENTICATION_REQUIRED()

    fun REQUEST_TIMEOUT() = HttpStatus.REQUEST_TIMEOUT()

    fun CONFLICT() = HttpStatus.CONFLICT()

    fun GONE() = HttpStatus.GONE()

    fun LENGTH_REQUIRED() = HttpStatus.LENGTH_REQUIRED()

    fun PRECONDITION_FAILED() = HttpStatus.PRECONDITION_FAILED()

    fun PAYLOAD_TOO_LARGE() = HttpStatus.PAYLOAD_TOO_LARGE()

    fun URI_TOO_LONG() = HttpStatus.URI_TOO_LONG()

    fun UNSUPPORTED_MEDIA_TYPE() = HttpStatus.UNSUPPORTED_MEDIA_TYPE()

    fun REQUESTED_RANGE_NOT_SATISFIABLE() = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE()

    fun EXPECTATION_FAILED() = HttpStatus.EXPECTATION_FAILED()

    fun I_AM_A_TEAPOT() = HttpStatus.I_AM_A_TEAPOT()

    fun UNPROCESSABLE_ENTITY() = HttpStatus.UNPROCESSABLE_ENTITY()

    fun LOCKED() = HttpStatus.LOCKED()

    fun FAILED_DEPENDENCY() = HttpStatus.FAILED_DEPENDENCY()

    fun UPGRADE_REQUIRED() = HttpStatus.UPGRADE_REQUIRED()

    fun PRECONDITION_REQUIRED() = HttpStatus.PRECONDITION_REQUIRED()

    fun TOO_MANY_REQUESTS() = HttpStatus.TOO_MANY_REQUESTS()

    fun REQUEST_HEADER_FIELDS_TOO_LARGE() = HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE()

    fun UNAVAILABLE_FOR_LEGAL_REASONS() = HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS()

    fun INTERNAL_SERVER_ERROR() = HttpStatus.INTERNAL_SERVER_ERROR()

    fun NOT_IMPLEMENTED() = HttpStatus.NOT_IMPLEMENTED()

    fun BAD_GATEWAY() = HttpStatus.BAD_GATEWAY()

    fun SERVICE_UNAVAILABLE() = HttpStatus.SERVICE_UNAVAILABLE()

    fun GATEWAY_TIMEOUT() = HttpStatus.GATEWAY_TIMEOUT()

    fun HTTP_VERSION_NOT_SUPPORTED() = HttpStatus.HTTP_VERSION_NOT_SUPPORTED()

    fun VARIANT_ALSO_NEGOTIATES() = HttpStatus.VARIANT_ALSO_NEGOTIATES()

    fun INSUFFICIENT_STORAGE() = HttpStatus.INSUFFICIENT_STORAGE()

    fun LOOP_DETECTED() = HttpStatus.LOOP_DETECTED()

    fun BANDWIDTH_LIMIT_EXCEEDED() = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED()

    fun NOT_EXTENDED() = HttpStatus.NOT_EXTENDED()

    fun NETWORK_AUTHENTICATION_REQUIRED() = HttpStatus.NETWORK_AUTHENTICATION_REQUIRED()

    /* REGEX */

    fun regexProperty(value: Any) = RegexProperty(value)

    fun regex(regex: String) = regexProperty(Pattern.compile(regex))

    fun regex(regex: Pattern) = regexProperty(regex)

    fun regex(regex: RegexProperty) = regex

    fun onlyAlphaUnicode(): RegexProperty = RegexPatterns.onlyAlphaUnicode()

    fun alphaNumeric(): RegexProperty = RegexPatterns.alphaNumeric()

    fun number(): RegexProperty = RegexPatterns.number()

    fun positiveInt(): RegexProperty = RegexPatterns.positiveInt()

    fun anyBoolean(): RegexProperty = RegexPatterns.anyBoolean()

    fun anInteger(): RegexProperty = RegexPatterns.anInteger()

    fun aDouble(): RegexProperty = RegexPatterns.aDouble()

    fun ipAddress(): RegexProperty = RegexPatterns.ipAddress()

    fun hostname(): RegexProperty = RegexPatterns.hostname()

    fun email(): RegexProperty = RegexPatterns.email()

    fun url(): RegexProperty = RegexPatterns.url()

    fun httpsUrl(): RegexProperty = RegexPatterns.httpsUrl()

    fun uuid(): RegexProperty = RegexPatterns.uuid()

    fun isoDate(): RegexProperty = RegexPatterns.isoDate()

    fun isoDateTime(): RegexProperty = RegexPatterns.isoDateTime()

    fun isoTime(): RegexProperty = RegexPatterns.isoTime()

    fun iso8601WithOffset(): RegexProperty = RegexPatterns.iso8601WithOffset()

    fun nonEmpty(): RegexProperty = RegexPatterns.nonEmpty()

    fun nonBlank(): RegexProperty = RegexPatterns.nonBlank()
}