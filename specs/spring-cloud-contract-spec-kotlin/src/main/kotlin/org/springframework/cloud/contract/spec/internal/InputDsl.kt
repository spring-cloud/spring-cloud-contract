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

/**
 * @author Tim Ysewyn
 */
@ContractDslMarker
class InputDsl : CommonDsl(), RegexCreatingProperty<ClientDslProperty> {

    var messageFrom: DslProperty<String>? = null
    var triggeredBy: ExecutionProperty? = null
    var headers: Headers? = null
    var messageBody: Input.BodyType? = null
    var assertThat: ExecutionProperty? = null
    var bodyMatchers: BodyMatchers? = null

    fun messageFrom(messageFrom: String) {
        this.messageFrom = DslProperty(messageFrom)
    }

    fun headers(headers: HeadersDsl.() -> Unit) {
        this.headers = HeadersDsl().apply(headers).get()
    }

    fun triggeredBy(triggeredBy: String) {
        this.triggeredBy = ExecutionProperty(triggeredBy)
    }

    fun messageBody(vararg pairs: Pair<String, Any>) {
        this.messageBody = Input.BodyType(pairs.toMap())
    }

    fun messageBody(pair: Pair<String, Any>) {
        this.messageBody = Input.BodyType(mapOf(pair))
    }

    fun messageBody(value: String) {
        this.messageBody = Input.BodyType(value)
    }

    fun assertThat(assertThat: String) {
        this.assertThat = ExecutionProperty(assertThat)
    }

    fun bodyMatchers(configurer: BodyMatchersDsl.() -> Unit) {
        bodyMatchers = BodyMatchersDsl().apply(configurer).get()
    }

    override fun anyAlphaUnicode(): ClientDslProperty = Input().anyAlphaUnicode()

    override fun anyAlphaNumeric(): ClientDslProperty = Input().anyAlphaNumeric()

    override fun anyNumber(): ClientDslProperty = Input().anyNumber()

    override fun anyInteger(): ClientDslProperty = Input().anyInteger()

    override fun anyPositiveInt(): ClientDslProperty = Input().anyPositiveInt()

    override fun anyDouble(): ClientDslProperty = Input().anyDouble()

    override fun anyHex(): ClientDslProperty = Input().anyHex()

    override fun aBoolean(): ClientDslProperty = Input().aBoolean()

    override fun anyIpAddress(): ClientDslProperty = Input().anyIpAddress()

    override fun anyHostname(): ClientDslProperty = Input().anyHostname()

    override fun anyEmail(): ClientDslProperty = Input().anyEmail()

    override fun anyUrl(): ClientDslProperty = Input().anyUrl()

    override fun anyHttpsUrl(): ClientDslProperty = Input().anyHttpsUrl()

    override fun anyUuid(): ClientDslProperty = Input().anyUuid()

    override fun anyDate(): ClientDslProperty = Input().anyDate()

    override fun anyDateTime(): ClientDslProperty = Input().anyDateTime()

    override fun anyTime(): ClientDslProperty = Input().anyTime()

    override fun anyIso8601WithOffset(): ClientDslProperty = Input().anyIso8601WithOffset()

    override fun anyNonBlankString(): ClientDslProperty = Input().anyNonBlankString()

    override fun anyNonEmptyString(): ClientDslProperty = Input().anyNonEmptyString()

    override fun anyOf(vararg values: String?): ClientDslProperty = Input().anyOf(*values)

    internal fun get(): Input {
        val input = Input()
        messageFrom?.also { input.messageFrom = messageFrom!! }
        triggeredBy?.also { input.triggeredBy = triggeredBy!! }
        headers?.also { input.messageHeaders = headers!! }
        messageBody?.also { input.messageBody = messageBody!! }
        assertThat?.also { input.assertThat = assertThat!! }
        bodyMatchers?.also { input.bodyMatchers = bodyMatchers!! }
        return input
    }
}
