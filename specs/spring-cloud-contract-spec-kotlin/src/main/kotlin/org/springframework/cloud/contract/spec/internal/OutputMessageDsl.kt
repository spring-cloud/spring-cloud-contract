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
class OutputMessageDsl : CommonDsl(), RegexCreatingProperty<ServerDslProperty> {

    var sentTo: DslProperty<String>? = null
    var headers: Headers? = null
    var body: DslProperty<Any>? = null
    var assertThat: ExecutionProperty? = null
    var bodyMatchers: ResponseBodyMatchers? = null

    fun sentTo(sentTo: String) {
        this.sentTo = DslProperty(sentTo)
    }

    fun headers(headers: HeadersDsl.() -> Unit) {
        this.headers = HeadersDsl().apply(headers).get()
    }

    fun body(body: Any) {
        this.body = DslProperty(body)
    }

    fun assertThat(assertThat: String) {
        this.assertThat = ExecutionProperty(assertThat)
    }

    fun bodyMatchers(configurer: ResponseBodyMatchersDsl.() -> Unit) {
        bodyMatchers = ResponseBodyMatchersDsl().apply(configurer).get()
    }

    override fun anyAlphaUnicode(): ServerDslProperty = OutputMessage().anyAlphaUnicode()

    override fun anyAlphaNumeric(): ServerDslProperty = OutputMessage().anyAlphaNumeric()

    override fun anyNumber(): ServerDslProperty = OutputMessage().anyNumber()

    override fun anyInteger(): ServerDslProperty = OutputMessage().anyInteger()

    override fun anyPositiveInt(): ServerDslProperty = OutputMessage().anyPositiveInt()

    override fun anyDouble(): ServerDslProperty = OutputMessage().anyDouble()

    override fun anyHex(): ServerDslProperty = OutputMessage().anyHex()

    override fun aBoolean(): ServerDslProperty = OutputMessage().aBoolean()

    override fun anyIpAddress(): ServerDslProperty = OutputMessage().anyIpAddress()

    override fun anyHostname(): ServerDslProperty = OutputMessage().anyHostname()

    override fun anyEmail(): ServerDslProperty = OutputMessage().anyEmail()

    override fun anyUrl(): ServerDslProperty = OutputMessage().anyUrl()

    override fun anyHttpsUrl(): ServerDslProperty = OutputMessage().anyHttpsUrl()

    override fun anyUuid(): ServerDslProperty = OutputMessage().anyUuid()

    override fun anyDate(): ServerDslProperty = OutputMessage().anyDate()

    override fun anyDateTime(): ServerDslProperty = OutputMessage().anyDateTime()

    override fun anyTime(): ServerDslProperty = OutputMessage().anyTime()

    override fun anyIso8601WithOffset(): ServerDslProperty = OutputMessage().anyIso8601WithOffset()

    override fun anyNonBlankString(): ServerDslProperty = OutputMessage().anyNonBlankString()

    override fun anyNonEmptyString(): ServerDslProperty = OutputMessage().anyNonEmptyString()

    override fun anyOf(vararg values: String?): ServerDslProperty = OutputMessage().anyOf(*values)

    internal fun get(): OutputMessage {
        val outputMessage = OutputMessage()
        sentTo?.also { outputMessage.sentTo = sentTo }
        body?.also { outputMessage.body = body }
        headers?.also { outputMessage.headers = headers }
        assertThat?.also { outputMessage.assertThat = assertThat }
        bodyMatchers?.also { outputMessage.bodyMatchers = bodyMatchers }
        return outputMessage
    }
}
