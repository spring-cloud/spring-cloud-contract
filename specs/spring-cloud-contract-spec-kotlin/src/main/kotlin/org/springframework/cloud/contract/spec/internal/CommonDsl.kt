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

import java.io.File
import java.net.URISyntaxException
import java.nio.charset.Charset
import java.util.regex.Pattern

/**
 * Composed out of common variables and funtions.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
open class CommonDsl {

    /* HELPER VARIABLES */

    /* REGEX */

    val onlyAlphaUnicode: RegexProperty
        get() = RegexPatterns.onlyAlphaUnicode()

    val alphaNumeric: RegexProperty
        get() = RegexPatterns.alphaNumeric()

    val number: RegexProperty
        get() = RegexPatterns.number()

    val positiveInt: RegexProperty
        get() = RegexPatterns.positiveInt()

    val anyBoolean: RegexProperty
        get() = RegexPatterns.anyBoolean()

    val anInteger: RegexProperty
        get() = RegexPatterns.anInteger()

    val aDouble: RegexProperty
        get() = RegexPatterns.aDouble()

    val ipAddress: RegexProperty
        get() = RegexPatterns.ipAddress()

    val hostname: RegexProperty
        get() = RegexPatterns.hostname()

    val email: RegexProperty
        get() = RegexPatterns.email()

    val anUrl: RegexProperty
        get() = RegexPatterns.url()

    val anHttpsUrl: RegexProperty
        get() = RegexPatterns.httpsUrl()

    val uuid: RegexProperty
        get() = RegexPatterns.uuid()

    val isoDate: RegexProperty
        get() = RegexPatterns.isoDate()

    val isoDateTime: RegexProperty
        get() = RegexPatterns.isoDateTime()

    val isoTime: RegexProperty
        get() = RegexPatterns.isoTime()

    val iso8601WithOffset: RegexProperty
        get() = RegexPatterns.iso8601WithOffset()

    val nonEmpty: RegexProperty
        get() = RegexPatterns.nonEmpty()

    val nonBlank: RegexProperty
        get() = RegexPatterns.nonBlank()

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

    /**
     * Helper method to indicate an optional property.
     * @param value the optional property value
     * @return optional dsl property
     */
    fun optional(value: Any?) = OptionalProperty(value)

    /**
     * Helper method to indicate that a method needs to be executed during testing.
     * @param commandToExecute the name of the method to execute
     * @return execution dsl property
     */
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
    fun file(relativePath: String, charset: Charset) = FromFileProperty(fileLocation(relativePath), String::class.java, charset)

    /**
     * Read file contents as String with the given Charset.
     * @param relativePath of the file to read
     * @param charset to use for converting the bytes to String
     * @return String file contents
     */
    fun fileAsBytes(relativePath: String) = FromFileProperty(fileLocation(relativePath), ByteArray::class.java)

    /**
     * Read file contents as String using a default charset (mostly UTF-8).
     * @param relativePath of the file to read
     * @return Body DSL property with the content of the file as String
     */
    fun bodyFromFile(relativePath: String) = bodyFromFile(relativePath, Charset.defaultCharset())

    /**
     * Read file contents as String using the provided file encoding.
     * @param relativePath of the file to read
     * @param charset of the file's encoding
     * @return Body the body with the String as its content
     */
    fun bodyFromFile(relativePath: String, charset: Charset) = Body(FromFileProperty(fileLocation(relativePath), String::class.java, charset))

    /**
     * Read file contents as bytes[].
     * @param relativePath of the file to read
     * @return Body the body with the byte array as its content
     */
    fun bodyFromFileAsBytes(relativePath: String) = Body(FromFileProperty(fileLocation(relativePath), ByteArray::class.java))

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

	fun part(value: Any) = Part(value)

	fun part(value: Any, contentType: Any) = Part(value, contentType)

	fun part(filename: Any, value: Any, contentType: Any) = Part(filename, value, contentType)

	fun part(filename: Any, value: Any, contentType: Any, contentTransferEncoding: Any) = Part(
		filename, value, contentType, contentTransferEncoding
	)

	fun part(properties: Map<String, Any>) = Part(properties)

    fun named(name: DslProperty<Any>, value: DslProperty<Any>) = NamedProperty(name, value)

    fun named(name: DslProperty<Any>, value: DslProperty<Any>,
              contentType: DslProperty<Any>) = NamedProperty(name, value, contentType)

    fun named(namedMap: Map<String, DslProperty<Any>>) = NamedProperty(namedMap)

    /* REGEX FUNCTIONS */

    fun regexProperty(value: Any) = RegexProperty(value)

    fun regex(regex: String) = regexProperty(Pattern.compile(regex))

    fun regex(regex: Pattern) = regexProperty(regex)

    fun regex(regex: RegexProperty) = regex
}