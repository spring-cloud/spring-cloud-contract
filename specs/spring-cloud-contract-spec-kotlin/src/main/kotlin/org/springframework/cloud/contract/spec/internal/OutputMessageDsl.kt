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
 * Represents an output for messaging.
 * Used for verifying the body and headers that are sent.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
@ContractDslMarker
class OutputMessageDsl : CommonDsl() {

    private val delegate = OutputMessage()

    /**
     * Name of a destination to which a message should be sent.
     */
    var sentTo: DslProperty<String>? = null

    /**
     * The message headers part of the contract.
     */
    var headers: Headers? = null

    /**
     * Name of a destination from which message would come to trigger action in the
     * system.
     */
    var body: DslProperty<Any>? = null

    /**
     * Function that needs to be executed after the message has been sent out of the system.
     */
    var assertThat: String? = null

    /**
     * The body matchers part of the contract.
     */
    var bodyMatchers: ResponseBodyMatchers? = null

    fun sentTo(sentTo: String) = sentTo.toDslProperty()

    fun headers(headers: HeadersDsl.() -> Unit) {
        this.headers = HeadersDsl().apply(headers).get()
    }

    fun body(body: Any) = body.toDslProperty()

	fun body(vararg body: Pair<String, Any>) = Body(body.toMap().toDslProperties())

    fun bodyMatchers(configurer: ResponseBodyMatchersDsl.() -> Unit) {
        this.bodyMatchers = ResponseBodyMatchersDsl().apply(configurer).get()
    }

    /* HELPER VARIABLES */

    val anyAlphaUnicode: ServerDslProperty
        get() = delegate.anyAlphaUnicode()

    val anyAlphaNumeric: ServerDslProperty
        get() = delegate.anyAlphaNumeric()

    val anyNumber: ServerDslProperty
        get() = delegate.anyNumber()

    val anyInteger: ServerDslProperty
        get() = delegate.anyInteger()

    val anyPositiveInt: ServerDslProperty
        get() = delegate.anyPositiveInt()

    val anyDouble: ServerDslProperty
        get() = delegate.anyDouble()

    val anyHex: ServerDslProperty
        get() = delegate.anyHex()

    val aBoolean: ServerDslProperty
        get() = delegate.aBoolean()

    val anyIpAddress: ServerDslProperty
        get() = delegate.anyIpAddress()

    val anyHostname: ServerDslProperty
        get() = delegate.anyHostname()

    val anyEmail: ServerDslProperty
        get() = delegate.anyEmail()

    val anyUrl: ServerDslProperty
        get() = delegate.anyUrl()

    val anyHttpsUrl: ServerDslProperty
        get() = delegate.anyHttpsUrl()

    val anyUuid: ServerDslProperty
        get() = delegate.anyUuid()

    val anyDate: ServerDslProperty
        get() = delegate.anyDate()

    val anyDateTime: ServerDslProperty
        get() = delegate.anyDateTime()

    val anyTime: ServerDslProperty
        get() = delegate.anyTime()

    val anyIso8601WithOffset: ServerDslProperty
        get() = delegate.anyIso8601WithOffset()

    val anyNonBlankString: ServerDslProperty
        get() = delegate.anyNonBlankString()

    val anyNonEmptyString: ServerDslProperty
        get() = delegate.anyNonEmptyString()

    /* HELPER FUNCTIONS */

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

    fun anyOf(vararg values: String?) = delegate.anyOf(*values)

    internal fun get(): OutputMessage {
        val outputMessage = OutputMessage()
        sentTo?.also { outputMessage.sentTo = sentTo }
        body?.also { outputMessage.body = body }
        headers?.also { outputMessage.headers = headers }
        assertThat?.also { outputMessage.assertThat(assertThat) }
        bodyMatchers?.also { outputMessage.bodyMatchers = bodyMatchers }
        return outputMessage
    }
}
