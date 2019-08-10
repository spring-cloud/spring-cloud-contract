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

    private val delegate = OutputMessage()

    var sentTo: DslProperty<String>? = null
    var headers: Headers? = null
    var body: DslProperty<Any>? = null
    var assertThat: String? = null
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

    fun bodyMatchers(configurer: ResponseBodyMatchersDsl.() -> Unit) {
        this.bodyMatchers = ResponseBodyMatchersDsl().apply(configurer).get()
    }

    override fun anyAlphaUnicode(): ServerDslProperty = delegate.anyAlphaUnicode()

    override fun anyAlphaNumeric(): ServerDslProperty = delegate.anyAlphaNumeric()

    override fun anyNumber(): ServerDslProperty = delegate.anyNumber()

    override fun anyInteger(): ServerDslProperty = delegate.anyInteger()

    override fun anyPositiveInt(): ServerDslProperty = delegate.anyPositiveInt()

    override fun anyDouble(): ServerDslProperty = delegate.anyDouble()

    override fun anyHex(): ServerDslProperty = delegate.anyHex()

    override fun aBoolean(): ServerDslProperty = delegate.aBoolean()

    override fun anyIpAddress(): ServerDslProperty = delegate.anyIpAddress()

    override fun anyHostname(): ServerDslProperty = delegate.anyHostname()

    override fun anyEmail(): ServerDslProperty = delegate.anyEmail()

    override fun anyUrl(): ServerDslProperty = delegate.anyUrl()

    override fun anyHttpsUrl(): ServerDslProperty = delegate.anyHttpsUrl()

    override fun anyUuid(): ServerDslProperty = delegate.anyUuid()

    override fun anyDate(): ServerDslProperty = delegate.anyDate()

    override fun anyDateTime(): ServerDslProperty = delegate.anyDateTime()

    override fun anyTime(): ServerDslProperty = delegate.anyTime()

    override fun anyIso8601WithOffset(): ServerDslProperty = delegate.anyIso8601WithOffset()

    override fun anyNonBlankString(): ServerDslProperty = delegate.anyNonBlankString()

    override fun anyNonEmptyString(): ServerDslProperty = delegate.anyNonEmptyString()

    override fun anyOf(vararg values: String?): ServerDslProperty = delegate.anyOf(*values)

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
