/*
 * Copyright 2013-2020 the original author or authors.
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
import java.util.regex.Pattern

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
     * Function that needs to be executed to trigger action in the system.
     */
    var triggeredBy: String? = null

    /**
     * Function that needs to be executed after the message has been received/processed by the system.
     */
    var assertThat: String? = null

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

    internal fun get(): Input {
        val input = Input()
        triggeredBy?.also { input.triggeredBy(triggeredBy) }
        assertThat?.also { input.assertThat(assertThat) }
        return input
    }
}
