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

/**
 * Represents the headers when sending/receiving HTTP traffic.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
open class HeadersDsl : CommonDsl() {

    internal val headers = LinkedHashMap<String, Any>()

    var accept: Any?
        get() = headers[HttpHeaders.ACCEPT]
        set(accept) = storeHeaderValue(HttpHeaders.ACCEPT, matching(accept))

    var acceptCharset: Any?
        get() = headers[HttpHeaders.ACCEPT_CHARSET]
        set(acceptCharset) = storeHeaderValue(HttpHeaders.ACCEPT_CHARSET, acceptCharset)

    var acceptEncoding: Any?
        get() = headers[HttpHeaders.ACCEPT_ENCODING]
        set(acceptEncoding) = storeHeaderValue(HttpHeaders.ACCEPT_ENCODING, acceptEncoding)

    var acceptLanguage: Any?
        get() = headers[HttpHeaders.ACCEPT_LANGUAGE]
        set(acceptLanguage) = storeHeaderValue(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage)

    var acceptRanges: Any?
        get() = headers[HttpHeaders.ACCEPT_RANGES]
        set(acceptRanges) = storeHeaderValue(HttpHeaders.ACCEPT_RANGES, acceptRanges)

    var accessControlAllowCredentials: Any?
        get() = headers[HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS]
        set(accessControlAllowCredentials) = storeHeaderValue(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, accessControlAllowCredentials)

    var accessControlAllowHeaders: Any?
        get() = headers[HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS]
        set(accessControlAllowHeaders) = storeHeaderValue(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, accessControlAllowHeaders)

    var accessControlAllowMethods: Any?
        get() = headers[HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS]
        set(accessControlAllowMethods) = storeHeaderValue(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, accessControlAllowMethods)

    var accessControlAllowOrigin: Any?
        get() = headers[HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN]
        set(accessControlAllowOrigin) = storeHeaderValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, accessControlAllowOrigin)

    var accessControlExposeHeaders: Any?
        get() = headers[HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS]
        set(accessControlExposeHeaders) = storeHeaderValue(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, accessControlExposeHeaders)

    var accessControlMaxAge: Any?
        get() = headers[HttpHeaders.ACCESS_CONTROL_MAX_AGE]
        set(accessControlMaxAge) = storeHeaderValue(HttpHeaders.ACCESS_CONTROL_MAX_AGE, accessControlMaxAge)

    var accessControlRequestHeaders: Any?
        get() = headers[HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS]
        set(accessControlRequestHeaders) = storeHeaderValue(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, accessControlRequestHeaders)

    var accessControlRequestMethod: Any?
        get() = headers[HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD]
        set(accessControlRequestMethod) = storeHeaderValue(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, accessControlRequestMethod)

    var age: Any?
        get() = headers[HttpHeaders.AGE]
        set(age) = storeHeaderValue(HttpHeaders.AGE, age)

    var allow: Any?
        get() = headers[HttpHeaders.ALLOW]
        set(allow) = storeHeaderValue(HttpHeaders.ALLOW, allow)

    var authorization: Any?
        get() = headers[HttpHeaders.AUTHORIZATION]
        set(authorization) = storeHeaderValue(HttpHeaders.AUTHORIZATION, authorization)

    var cacheControl: Any?
        get() = headers[HttpHeaders.CACHE_CONTROL]
        set(cacheControl) = storeHeaderValue(HttpHeaders.CACHE_CONTROL, cacheControl)

    var connection: Any?
        get() = headers[HttpHeaders.CONNECTION]
        set(connection) = storeHeaderValue(HttpHeaders.CONNECTION, connection)

    var contentEncoding: Any?
        get() = headers[HttpHeaders.CONTENT_ENCODING]
        set(contentEncoding) = storeHeaderValue(HttpHeaders.CONTENT_ENCODING, contentEncoding)

    var contentDisposition: Any?
        get() = headers[HttpHeaders.CONTENT_DISPOSITION]
        set(contentDisposition) = storeHeaderValue(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)

    var contentLanguage: Any?
        get() = headers[HttpHeaders.CONTENT_LANGUAGE]
        set(contentLanguage) = storeHeaderValue(HttpHeaders.CONTENT_LANGUAGE, contentLanguage)

    var contentLength: Any?
        get() = headers[HttpHeaders.CONTENT_LENGTH]
        set(contentLength) = storeHeaderValue(HttpHeaders.CONTENT_LENGTH, contentLength)

    var contentLocation: Any?
        get() = headers[HttpHeaders.CONTENT_LOCATION]
        set(contentLocation) = storeHeaderValue(HttpHeaders.CONTENT_LOCATION, contentLocation)

    var contentRange: Any?
        get() = headers[HttpHeaders.CONTENT_RANGE]
        set(contentRange) = storeHeaderValue(HttpHeaders.CONTENT_RANGE, contentRange)

    var contentType: Any?
        get() = headers[HttpHeaders.CONTENT_TYPE]
        set(contentType) = storeHeaderValue(HttpHeaders.CONTENT_TYPE, matching(contentType))

    var cookie: Any?
        get() = headers[HttpHeaders.COOKIE]
        set(cookie) = storeHeaderValue(HttpHeaders.COOKIE, cookie)

    var date: Any?
        get() = headers[HttpHeaders.DATE]
        set(date) = storeHeaderValue(HttpHeaders.DATE, date)

    var etag: Any?
        get() = headers[HttpHeaders.ETAG]
        set(etag) = storeHeaderValue(HttpHeaders.ETAG, etag)

    var expect: Any?
        get() = headers[HttpHeaders.EXPECT]
        set(expect) = storeHeaderValue(HttpHeaders.EXPECT, expect)

    var expires: Any?
        get() = headers[HttpHeaders.EXPIRES]
        set(expires) = storeHeaderValue(HttpHeaders.EXPIRES, expires)

    var from: Any?
        get() = headers[HttpHeaders.FROM]
        set(from) = storeHeaderValue(HttpHeaders.FROM, from)

    var host: Any?
        get() = headers[HttpHeaders.HOST]
        set(host) = storeHeaderValue(HttpHeaders.HOST, host)

    var ifMatch: Any?
        get() = headers[HttpHeaders.IF_MATCH]
        set(ifMatch) = storeHeaderValue(HttpHeaders.IF_MATCH, ifMatch)

    var ifModifiedSince: Any?
        get() = headers[HttpHeaders.IF_MODIFIED_SINCE]
        set(ifModifiedSince) = storeHeaderValue(HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince)

    var ifNoneMatch: Any?
        get() = headers[HttpHeaders.IF_NONE_MATCH]
        set(ifNoneMatch) = storeHeaderValue(HttpHeaders.IF_NONE_MATCH, ifNoneMatch)

    var ifRange: Any?
        get() = headers[HttpHeaders.IF_RANGE]
        set(ifRange) = storeHeaderValue(HttpHeaders.IF_RANGE, ifRange)

    var ifUnmodifiedSince: Any?
        get() = headers[HttpHeaders.IF_UNMODIFIED_SINCE]
        set(ifUnmodifiedSince) = storeHeaderValue(HttpHeaders.IF_UNMODIFIED_SINCE, ifUnmodifiedSince)

    var lastModified: Any?
        get() = headers[HttpHeaders.LAST_MODIFIED]
        set(lastModified) = storeHeaderValue(HttpHeaders.LAST_MODIFIED, lastModified)

    var link: Any?
        get() = headers[HttpHeaders.LINK]
        set(link) = storeHeaderValue(HttpHeaders.LINK, link)

    var location: Any?
        get() = headers[HttpHeaders.LOCATION]
        set(location) = storeHeaderValue(HttpHeaders.LOCATION, location)

    var max_forwards: Any?
        get() = headers[HttpHeaders.MAX_FORWARDS]
        set(max_forwards) = storeHeaderValue(HttpHeaders.MAX_FORWARDS, max_forwards)

    var origin: Any?
        get() = headers[HttpHeaders.ORIGIN]
        set(origin) = storeHeaderValue(HttpHeaders.ORIGIN, origin)

    var pragma: Any?
        get() = headers[HttpHeaders.PRAGMA]
        set(pragma) = storeHeaderValue(HttpHeaders.PRAGMA, pragma)

    var proxyAuthenticate: Any?
        get() = headers[HttpHeaders.PROXY_AUTHENTICATE]
        set(proxyAuthenticate) = storeHeaderValue(HttpHeaders.PROXY_AUTHENTICATE, proxyAuthenticate)

    var proxyAuthorization: Any?
        get() = headers[HttpHeaders.PROXY_AUTHORIZATION]
        set(proxyAuthorization) = storeHeaderValue(HttpHeaders.PROXY_AUTHORIZATION, proxyAuthorization)

    var range: Any?
        get() = headers[HttpHeaders.RANGE]
        set(range) = storeHeaderValue(HttpHeaders.RANGE, range)

    var referer: Any?
        get() = headers[HttpHeaders.REFERER]
        set(referer) = storeHeaderValue(HttpHeaders.REFERER, referer)

    var retryAfter: Any?
        get() = headers[HttpHeaders.RETRY_AFTER]
        set(retryAfter) = storeHeaderValue(HttpHeaders.RETRY_AFTER, retryAfter)

    var server: Any?
        get() = headers[HttpHeaders.SERVER]
        set(server) = storeHeaderValue(HttpHeaders.SERVER, server)

    var setCookie: Any?
        get() = headers[HttpHeaders.SET_COOKIE]
        set(setCookie) = storeHeaderValue(HttpHeaders.SET_COOKIE, setCookie)

    var setCookie2: Any?
        get() = headers[HttpHeaders.SET_COOKIE_2]
        set(setCookie2) = storeHeaderValue(HttpHeaders.SET_COOKIE_2, setCookie2)

    var te: Any?
        get() = headers[HttpHeaders.TE]
        set(te) = storeHeaderValue(HttpHeaders.TE, te)

    var trailer: Any?
        get() = headers[HttpHeaders.TRAILER]
        set(trailer) = storeHeaderValue(HttpHeaders.TRAILER, trailer)

    var transferEncoding: Any?
        get() = headers[HttpHeaders.TRANSFER_ENCODING]
        set(transferEncoding) = storeHeaderValue(HttpHeaders.TRANSFER_ENCODING, transferEncoding)

    var upgrade: Any?
        get() = headers[HttpHeaders.UPGRADE]
        set(upgrade) = storeHeaderValue(HttpHeaders.UPGRADE, upgrade)

    var user_agent: Any?
        get() = headers[HttpHeaders.USER_AGENT]
        set(user_agent) = storeHeaderValue(HttpHeaders.USER_AGENT, user_agent)

    var vary: Any?
        get() = headers[HttpHeaders.VARY]
        set(vary) = storeHeaderValue(HttpHeaders.VARY, vary)

    var via: Any?
        get() = headers[HttpHeaders.VIA]
        set(via) = storeHeaderValue(HttpHeaders.VIA, via)

    var warning: Any?
        get() = headers[HttpHeaders.WARNING]
        set(warning) = storeHeaderValue(HttpHeaders.WARNING, warning)

    var wwwAuthenticate: Any?
        get() = headers[HttpHeaders.WWW_AUTHENTICATE]
        set(wwwAuthenticate) = storeHeaderValue(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate)

    var messagingContentType: Any?
        get() = headers[MessagingHeaders.MESSAGING_CONTENT_TYPE]
        set(messagingContentType) = storeHeaderValue(MessagingHeaders.MESSAGING_CONTENT_TYPE, matching(messagingContentType))

    open fun matching(value: Any?): Any? = value

    private fun storeHeaderValue(header: String, value: Any?) {
        value?.also { headers[header] = value }
    }

    /* HELPER VARIABLES */

    /* HTTP HEADERS */

    val ACCEPT = HttpHeaders.ACCEPT

    val ACCEPT_CHARSET = HttpHeaders.ACCEPT_CHARSET

    val ACCEPT_ENCODING = HttpHeaders.ACCEPT_ENCODING

    val ACCEPT_LANGUAGE = HttpHeaders.ACCEPT_LANGUAGE

    val ACCEPT_RANGES = HttpHeaders.ACCEPT_RANGES

    val ACCESS_CONTROL_ALLOW_CREDENTIALS = HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS

    val ACCESS_CONTROL_ALLOW_HEADERS = HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS

    val ACCESS_CONTROL_ALLOW_METHODS = HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS

    val ACCESS_CONTROL_ALLOW_ORIGIN = HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN

    val ACCESS_CONTROL_EXPOSE_HEADERS = HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS

    val ACCESS_CONTROL_MAX_AGE = HttpHeaders.ACCESS_CONTROL_MAX_AGE

    val ACCESS_CONTROL_REQUEST_HEADERS = HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS

    val ACCESS_CONTROL_REQUEST_METHOD = HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD

    val AGE = HttpHeaders.AGE

    val ALLOW = HttpHeaders.ALLOW

    val AUTHORIZATION = HttpHeaders.AUTHORIZATION

    val CACHE_CONTROL = HttpHeaders.CACHE_CONTROL

    val CONNECTION = HttpHeaders.CONNECTION

    val CONTENT_ENCODING = HttpHeaders.CONTENT_ENCODING

    val CONTENT_DISPOSITION = HttpHeaders.CONTENT_DISPOSITION

    val CONTENT_LANGUAGE = HttpHeaders.CONTENT_LANGUAGE

    val CONTENT_LENGTH = HttpHeaders.CONTENT_LENGTH

    val CONTENT_LOCATION = HttpHeaders.CONTENT_LOCATION

    val CONTENT_RANGE = HttpHeaders.CONTENT_RANGE

    val CONTENT_TYPE = HttpHeaders.CONTENT_TYPE

    val COOKIE = HttpHeaders.COOKIE

    val DATE = HttpHeaders.DATE

    val ETAG = HttpHeaders.ETAG

    val EXPECT = HttpHeaders.EXPECT

    val EXPIRES = HttpHeaders.EXPIRES

    val FROM = HttpHeaders.FROM

    val HOST = HttpHeaders.HOST

    val IF_MATCH = HttpHeaders.IF_MATCH

    val IF_MODIFIED_SINCE = HttpHeaders.IF_MODIFIED_SINCE

    val IF_NONE_MATCH = HttpHeaders.IF_NONE_MATCH

    val IF_RANGE = HttpHeaders.IF_RANGE

    val IF_UNMODIFIED_SINCE = HttpHeaders.IF_UNMODIFIED_SINCE

    val LAST_MODIFIED = HttpHeaders.LAST_MODIFIED

    val LINK = HttpHeaders.LINK

    val LOCATION = HttpHeaders.LOCATION

    val MAX_FORWARDS = HttpHeaders.MAX_FORWARDS

    val ORIGIN = HttpHeaders.ORIGIN

    val PRAGMA = HttpHeaders.PRAGMA

    val PROXY_AUTHENTICATE = HttpHeaders.PROXY_AUTHENTICATE

    val PROXY_AUTHORIZATION = HttpHeaders.PROXY_AUTHORIZATION

    val RANGE = HttpHeaders.RANGE

    val REFERER = HttpHeaders.REFERER

    val RETRY_AFTER = HttpHeaders.RETRY_AFTER

    val SERVER = HttpHeaders.SERVER

    val SET_COOKIE = HttpHeaders.SET_COOKIE

    val SET_COOKIE_2 = HttpHeaders.SET_COOKIE_2

    val TE = HttpHeaders.TE

    val TRAILER = HttpHeaders.TRAILER

    val TRANSFER_ENCODING = HttpHeaders.TRANSFER_ENCODING

    val UPGRADE = HttpHeaders.UPGRADE

    val USER_AGENT = HttpHeaders.USER_AGENT

    val VARY = HttpHeaders.VARY

    val VIA = HttpHeaders.VIA

    val WARNING = HttpHeaders.WARNING

    val WWW_AUTHENTICATE = HttpHeaders.WWW_AUTHENTICATE

    /* MESSAGING HEADERS */

    val MESSAGING_CONTENT_TYPE = MessagingHeaders.MESSAGING_CONTENT_TYPE

    /* MEDIA TYPES */

    val ALL_VALUE = MediaTypes.ALL_VALUE

    val APPLICATION_ATOM_XML = MediaTypes.APPLICATION_ATOM_XML

    val APPLICATION_FORM_URLENCODED = MediaTypes.APPLICATION_FORM_URLENCODED

    val APPLICATION_JSON = MediaTypes.APPLICATION_JSON

    val APPLICATION_JSON_UTF8 = MediaTypes.APPLICATION_JSON_UTF8

    val APPLICATION_OCTET_STREAM = MediaTypes.APPLICATION_OCTET_STREAM

    val APPLICATION_PDF = MediaTypes.APPLICATION_PDF

    val APPLICATION_XHTML_XML = MediaTypes.APPLICATION_XHTML_XML

    val APPLICATION_XML = MediaTypes.APPLICATION_XML

    val IMAGE_GIF = MediaTypes.IMAGE_GIF

    val IMAGE_JPEG = MediaTypes.IMAGE_JPEG

    val IMAGE_PNG = MediaTypes.IMAGE_PNG

    val MULTIPART_FORM_DATA = MediaTypes.MULTIPART_FORM_DATA

    val TEXT_HTML = MediaTypes.TEXT_HTML

    val TEXT_MARKDOWN = MediaTypes.TEXT_MARKDOWN

    val TEXT_PLAIN = MediaTypes.TEXT_PLAIN

    val TEXT_XML = MediaTypes.TEXT_XML

    /**
     * Adds a header.
     *
     * @param name The name of the header.
     * @param value The value of the header.
     */
    fun header(name: String, value: Any) {
        this.headers[name] = value
    }

    internal fun get(): Headers {
        val headers = Headers()
        this.headers.forEach { (key, value) -> headers.header(key, value) }
        return headers
    }

}
