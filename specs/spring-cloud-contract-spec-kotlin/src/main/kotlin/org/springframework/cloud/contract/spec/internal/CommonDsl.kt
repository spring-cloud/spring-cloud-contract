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

import java.io.File
import java.net.URISyntaxException
import java.nio.charset.Charset
import java.util.regex.Pattern

/**
 * @author Tim Ysewyn
 */
open class CommonDsl {

    /* HELPER FUNCTIONS */
    
    /**
     * Helper method to provide a better name for the consumer side.
     * @param clientValue client value
     * @return client dsl property
     */
    fun client(clientValue: Any?) = ClientDslProperty(clientValue)

    /**
     * Helper method to provide a better name for the consumer side.
     * @param clientValue client value
     * @return client dsl property
     */
    fun c(clientValue: Any?) = ClientDslProperty(clientValue)

    /**
     * Helper method to provide a better name for the consumer side.
     * @param clientValue client value
     * @return client dsl property
     */
    fun consumer(clientValue: Any?) = ClientDslProperty(clientValue)

    /**
     * Helper method to provide a better name for the consumer side.
     * @param clientValue client value
     * @return client dsl property
     */
    fun stub(clientValue: Any?) = ClientDslProperty(clientValue)

    /**
     * Helper method to provide a better name for the server side.
     * @param serverValue server value
     * @return server dsl property
     */
    fun server(serverValue: Any?) = ServerDslProperty(serverValue)

    /**
     * Helper method to provide a better name for the server side.
     * @param serverValue server value
     * @return server dsl property
     */
    fun p(serverValue: Any?) = ServerDslProperty(serverValue)

    /**
     * Helper method to provide a better name for the server side.
     * @param serverValue server value
     * @return server dsl property
     */
    fun producer(serverValue: Any?) = ServerDslProperty(serverValue)

    /**
     * Helper method to provide a better name for the server side.
     * @param serverValue server value
     * @return server dsl property
     */
    fun test(serverValue: Any?) = ServerDslProperty(serverValue)

    fun optional(value: Any?) = OptionalProperty(value)

    fun execute(commandToExecute: String) = ExecutionProperty(commandToExecute)

    /**
     * Read file contents as String.
     * @param relativePath of the file to read
     * @return String file contents
     */
    fun file(relativePath: String) = file(relativePath, Charset.defaultCharset())

    /**
     * Read file contents as String with the given Charset.
     * @param relativePath of the file to read
     * @param charset to use for converting the bytes to String
     * @return String file contents
     */
    fun file(relativePath: String, charset: Charset) = Body(FromFileProperty(fileLocation(relativePath), String::class.java, charset))

    /**
     * Read file contents as bytes[].
     * @param relativePath of the file to read
     * @return String file contents
     */
    fun fileAsBytes(relativePath: String) = Body(FromFileProperty(fileLocation(relativePath), ByteArray::class.java))

    /**
     * Read file contents as array of bytes.
     * @param relativePath of the file to read
     * @return file contents as an array of bytes
     */
    private fun fileLocation(relativePath: String): File {
        val resource = Thread.currentThread().contextClassLoader
                .getResource(relativePath) ?: throw IllegalStateException("File [$relativePath] is not present")
        try {
            return File(resource.toURI())
        } catch (ex: URISyntaxException) {
            throw IllegalStateException(ex)
        }
    }

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