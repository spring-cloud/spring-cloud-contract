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
 * @author Tim Ysewyn
 */
@ContractDslMarker
class InputDsl : CommonDsl(), RegexCreatingProperty<ClientDslProperty> {

    private val delegate = Input()

    var messageFrom: DslProperty<String>? = null
    var triggeredBy: String? = null
    var headers: Headers? = null
    var messageBody: Input.BodyType? = null
    var assertThat: String? = null
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

    override fun anyAlphaUnicode(): ClientDslProperty = delegate.anyAlphaUnicode()

    override fun anyAlphaNumeric(): ClientDslProperty = delegate.anyAlphaNumeric()

    override fun anyNumber(): ClientDslProperty = delegate.anyNumber()

    override fun anyInteger(): ClientDslProperty = delegate.anyInteger()

    override fun anyPositiveInt(): ClientDslProperty = delegate.anyPositiveInt()

    override fun anyDouble(): ClientDslProperty = delegate.anyDouble()

    override fun anyHex(): ClientDslProperty = delegate.anyHex()

    override fun aBoolean(): ClientDslProperty = delegate.aBoolean()

    override fun anyIpAddress(): ClientDslProperty = delegate.anyIpAddress()

    override fun anyHostname(): ClientDslProperty = delegate.anyHostname()

    override fun anyEmail(): ClientDslProperty = delegate.anyEmail()

    override fun anyUrl(): ClientDslProperty = delegate.anyUrl()

    override fun anyHttpsUrl(): ClientDslProperty = delegate.anyHttpsUrl()

    override fun anyUuid(): ClientDslProperty = delegate.anyUuid()

    override fun anyDate(): ClientDslProperty = delegate.anyDate()

    override fun anyDateTime(): ClientDslProperty = delegate.anyDateTime()

    override fun anyTime(): ClientDslProperty = delegate.anyTime()

    override fun anyIso8601WithOffset(): ClientDslProperty = delegate.anyIso8601WithOffset()

    override fun anyNonBlankString(): ClientDslProperty = delegate.anyNonBlankString()

    override fun anyNonEmptyString(): ClientDslProperty = delegate.anyNonEmptyString()

    override fun anyOf(vararg values: String?): ClientDslProperty = delegate.anyOf(*values)

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
