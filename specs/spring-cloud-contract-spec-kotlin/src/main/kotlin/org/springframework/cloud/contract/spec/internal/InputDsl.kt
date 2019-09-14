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

import org.springframework.cloud.contract.spec.toDslProperty

/**
 * Represents an input for messaging.
 * The input can be a message or some action inside the application.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
@ContractDslMarker
class InputDsl : CommonDsl() {

    private val delegate = Input()

    /**
     * Name of a destination from which message would come to trigger action in the
     * system.
     */
    var messageFrom: DslProperty<String>? = null

    /**
     * Function that needs to be executed to trigger action in the system.
     */
    var triggeredBy: String? = null

    /**
     * The message headers part of the contract.
     */
    var headers: Headers? = null

    /**
     * The contents of the incoming message.
     */
    var messageBody: Input.BodyType? = null

    /**
     * Function that needs to be executed after the message has been received/processed by the system.
     */
    var assertThat: String? = null

    /**
     * The body matchers part of the contract.
     */
    var bodyMatchers: BodyMatchers? = null

    fun messageFrom(messageFrom: String) = messageFrom.toDslProperty()

    fun headers(headers: HeadersDsl.() -> Unit) {
        this.headers = HeadersDsl().apply(headers).get()
    }

    fun messageBody(vararg pairs: Pair<String, Any>) = Input.BodyType(pairs.toMap())

    fun messageBody(pair: Pair<String, Any>) = Input.BodyType(mapOf(pair))

    fun messageBody(value: String) = Input.BodyType(value)

    fun bodyMatchers(configurer: BodyMatchersDsl.() -> Unit) {
        this.bodyMatchers = BodyMatchersDsl().apply(configurer).get()
    }

    /* HELPER VARIABLES */

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

    fun anyOf(vararg values: String?) = delegate.anyOf(*values)

    internal fun get(): Input {
        val input = Input()
        messageFrom?.also { input.messageFrom = messageFrom }
        triggeredBy?.also { input.triggeredBy(triggeredBy) }
        headers?.also { input.messageHeaders = headers }
        messageBody?.also { input.messageBody = messageBody }
        assertThat?.also { input.assertThat(assertThat) }
        bodyMatchers?.also { input.bodyMatchers = bodyMatchers }
        return input
    }
}
