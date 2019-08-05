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

package org.springframework.cloud.contract.spec.internal;

/**
 * Contains most commonly used http headers.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.2
 */
public final class HttpHeaders {

	public HttpHeaders() {
		System.out.println("WARNING: HttpHeaders shouldn't be instantiated");
	}

	/**
	 * @return The HTTP {@code Accept} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">Section 5.3.2 of
	 * RFC 7231</a>
	 */
	public static String accept() {
		return "Accept";
	}

	/**
	 * @return The HTTP {@code Accept-Charset} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.3">Section 5.3.3 of
	 * RFC 7231</a>
	 */
	public static String acceptCharset() {
		return "Accept-Charset";
	}

	/**
	 * @return The HTTP {@code Accept-Encoding} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.4">Section 5.3.4 of
	 * RFC 7231</a>
	 */
	public static String acceptEncoding() {
		return "Accept-Encoding";
	}

	/**
	 * @return The HTTP {@code Accept-Language} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">Section 5.3.5 of
	 * RFC 7231</a>
	 */
	public static String acceptLanguage() {
		return "Accept-Language";
	}

	/**
	 * @return The HTTP {@code Accept-Ranges} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-2.3">Section 5.3.5 of RFC
	 * 7233</a>
	 */
	public static String acceptRanges() {
		return "Accept-Ranges";
	}

	/**
	 * @return The CORS {@code Access-Control-Allow-Credentials} response header field
	 * name.
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static String accessControlAllowCredentials() {
		return "Access-Control-Allow-Credentials";
	}

	/**
	 * @return The CORS {@code Access-Control-Allow-Headers} response header field name.
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static String accessControlAllowHeaders() {
		return "Access-Control-Allow-Headers";
	}

	/**
	 * @return The CORS {@code Access-Control-Allow-Methods} response header field name.
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static String accessControlAllowMethods() {
		return "Access-Control-Allow-Methods";
	}

	/**
	 * @return The CORS {@code Access-Control-Allow-Origin} response header field name.
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static String accessControlAllowOrigin() {
		return "Access-Control-Allow-Origin";
	}

	/**
	 * @return The CORS {@code Access-Control-Expose-Headers} response header field name.
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static String accessControlExposeHeaders() {
		return "Access-Control-Expose-Headers";
	}

	/**
	 * @return The CORS {@code Access-Control-Max-Age} response header field name.
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static String accessControlMaxAge() {
		return "Access-Control-Max-Age";
	}

	/**
	 * @return The CORS {@code Access-Control-Request-Headers} request header field name.
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static String accessControlRequestHeaders() {
		return "Access-Control-Request-Headers";
	}

	/**
	 * @return The CORS {@code Access-Control-Request-Method} request header field name.
	 * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
	 */
	public static String accessControlRequestMethod() {
		return "Access-Control-Request-Method";
	}

	/**
	 * @return The HTTP {@code Age} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.1">Section 5.1 of RFC
	 * 7234</a>
	 */
	public static String age() {
		return "Age";
	}

	/**
	 * @return The HTTP {@code Allow} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.1">Section 7.4.1 of
	 * RFC 7231</a>
	 */
	public static String allow() {
		return "Allow";
	}

	/**
	 * @return The HTTP {@code Authorization} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.2">Section 4.2 of RFC
	 * 7235</a>
	 */
	public static String authorization() {
		return "Authorization";
	}

	/**
	 * @return The HTTP {@code Cache-Control} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.2">Section 5.2 of RFC
	 * 7234</a>
	 */
	public static String cacheControl() {
		return "Cache-Control";
	}

	/**
	 * @return The HTTP {@code Connection} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.1">Section 6.1 of RFC
	 * 7230</a>
	 */
	public static String connection() {
		return "Connection";
	}

	/**
	 * @return The HTTP {@code Content-Encoding} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.2.2">Section 3.1.2.2
	 * of RFC 7231</a>
	 */
	public static String contentEncoding() {
		return "Content-Encoding";
	}

	/**
	 * @return The HTTP {@code Content-Disposition} header field name
	 * @see <a href="https://tools.ietf.org/html/rfc6266">RFC 6266</a>
	 */
	public static String contentDisposition() {
		return "Content-Disposition";
	}

	/**
	 * @return The HTTP {@code Content-Language} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section 3.1.3.2
	 * of RFC 7231</a>
	 */
	public static String contentLanguage() {
		return "Content-Language";
	}

	/**
	 * @return The HTTP {@code Content-Length} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.2">Section 3.3.2 of
	 * RFC 7230</a>
	 */
	public static String contentLength() {
		return "Content-Length";
	}

	/**
	 * @return The HTTP {@code Content-Location} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.4.2">Section 3.1.4.2
	 * of RFC 7231</a>
	 */
	public static String contentLocation() {
		return "Content-Location";
	}

	/**
	 * @return The HTTP {@code Content-Range} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.2">Section 4.2 of RFC
	 * 7233</a>
	 */
	public static String contentRange() {
		return "Content-Range";
	}

	/**
	 * @return The HTTP {@code Content-Type} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section 3.1.1.5
	 * of RFC 7231</a>
	 */
	public static String contentType() {
		return "Content-Type";
	}

	/**
	 * @return The HTTP {@code Cookie} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.3.4">Section 4.3.4 of
	 * RFC 2109</a>
	 */
	public static String cookie() {
		return "Cookie";
	}

	/**
	 * @return The HTTP {@code Date} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.2">Section 7.1.1.2
	 * of RFC 7231</a>
	 */
	public static String date() {
		return "Date";
	}

	/**
	 * @return The HTTP {@code ETag} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC
	 * 7232</a>
	 */
	public static String etag() {
		return "ETag";
	}

	/**
	 * @return The HTTP {@code Expect} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.1">Section 5.1.1 of
	 * RFC 7231</a>
	 */
	public static String expect() {
		return "Expect";
	}

	/**
	 * @return The HTTP {@code Expires} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.3">Section 5.3 of RFC
	 * 7234</a>
	 */
	public static String expires() {
		return "Expires";
	}

	/**
	 * @return The HTTP {@code From} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.1">Section 5.5.1 of
	 * RFC 7231</a>
	 */
	public static String from() {
		return "From";
	}

	/**
	 * @return The HTTP {@code Host} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.4">Section 5.4 of RFC
	 * 7230</a>
	 */
	public static String host() {
		return "Host";
	}

	/**
	 * @return The HTTP {@code If-Match} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.1">Section 3.1 of RFC
	 * 7232</a>
	 */
	public static String ifMatch() {
		return "If-Match";
	}

	/**
	 * @return The HTTP {@code If-Modified-Since} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.3">Section 3.3 of RFC
	 * 7232</a>
	 */
	public static String ifModifiedSince() {
		return "If-Modified-Since";
	}

	/**
	 * @return The HTTP {@code If-None-Match} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.2">Section 3.2 of RFC
	 * 7232</a>
	 */
	public static String ifNoneMatch() {
		return "If-None-Match";
	}

	/**
	 * @return The HTTP {@code If-Range} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.2">Section 3.2 of RFC
	 * 7233</a>
	 */
	public static String ifRange() {
		return "If-Range";
	}

	/**
	 * @return The HTTP {@code If-Unmodified-Since} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.4">Section 3.4 of RFC
	 * 7232</a>
	 */
	public static String ifUnmodifiedSince() {
		return "If-Unmodified-Since";
	}

	/**
	 * @return The HTTP {@code Last-Modified} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.2">Section 2.2 of RFC
	 * 7232</a>
	 */
	public static String lastModified() {
		return "Last-Modified";
	}

	/**
	 * @return The HTTP {@code Link} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc5988">RFC 5988</a>
	 */
	public static String link() {
		return "Link";
	}

	/**
	 * @return The HTTP {@code Location} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.2">Section 7.1.2 of
	 * RFC 7231</a>
	 */
	public static String location() {
		return "Location";
	}

	/**
	 * @return The HTTP {@code Max-Forwards} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.2">Section 5.1.2 of
	 * RFC 7231</a>
	 */
	public static String max_forwards() {
		return "Max-Forwards";
	}

	/**
	 * @return The HTTP {@code Origin} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc6454">RFC 6454</a>
	 */
	public static String origin() {
		return "Origin";
	}

	/**
	 * @return The HTTP {@code Pragma} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.4">Section 5.4 of RFC
	 * 7234</a>
	 */
	public static String pragma() {
		return "Pragma";
	}

	/**
	 * @return The HTTP {@code Proxy-Authenticate} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.3">Section 4.3 of RFC
	 * 7235</a>
	 */
	public static String proxyAuthenticate() {
		return "Proxy-Authenticate";
	}

	/**
	 * @return The HTTP {@code Proxy-Authorization} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.4">Section 4.4 of RFC
	 * 7235</a>
	 */
	public static String proxyAuthorization() {
		return "Proxy-Authorization";
	}

	/**
	 * @return The HTTP {@code Range} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.1">Section 3.1 of RFC
	 * 7233</a>
	 */
	public static String range() {
		return "Range";
	}

	/**
	 * @return The HTTP {@code Referer} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.2">Section 5.5.2 of
	 * RFC 7231</a>
	 */
	public static String referer() {
		return "Referer";
	}

	/**
	 * @return The HTTP {@code Retry-After} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.3">Section 7.1.3 of
	 * RFC 7231</a>
	 */
	public static String retryAfter() {
		return "Retry-After";
	}

	/**
	 * @return The HTTP {@code Server} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.2">Section 7.4.2 of
	 * RFC 7231</a>
	 */
	public static String server() {
		return "Server";
	}

	/**
	 * @return The HTTP {@code Set-Cookie} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.2.2">Section 4.2.2 of
	 * RFC 2109</a>
	 */
	public static String setCookie() {
		return "Set-Cookie";
	}

	/**
	 * @return The HTTP {@code Set-Cookie2} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc2965">RFC 2965</a>
	 */
	public static String setCookie2() {
		return "Set-Cookie2";
	}

	/**
	 * @return The HTTP {@code TE} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.3">Section 4.3 of RFC
	 * 7230</a>
	 */
	public static String te() {
		return "TE";
	}

	/**
	 * @return The HTTP {@code Trailer} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.4">Section 4.4 of RFC
	 * 7230</a>
	 */
	public static String trailer() {
		return "Trailer";
	}

	/**
	 * @return The HTTP {@code Transfer-Encoding} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.1">Section 3.3.1 of
	 * RFC 7230</a>
	 */
	public static String transferEncoding() {
		return "Transfer-Encoding";
	}

	/**
	 * @return The HTTP {@code Upgrade} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.7">Section 6.7 of RFC
	 * 7230</a>
	 */
	public static String upgrade() {
		return "Upgrade";
	}

	/**
	 * @return The HTTP {@code User-Agent} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.3">Section 5.5.3 of
	 * RFC 7231</a>
	 */
	public static String user_agent() {
		return "User-Agent";
	}

	/**
	 * @return The HTTP {@code Vary} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.4">Section 7.1.4 of
	 * RFC 7231</a>
	 */
	public static String vary() {
		return "Vary";
	}

	/**
	 * @return The HTTP {@code Via} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.7.1">Section 5.7.1 of
	 * RFC 7230</a>
	 */
	public static String via() {
		return "Via";
	}

	/**
	 * @return The HTTP {@code Warning} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.5">Section 5.5 of RFC
	 * 7234</a>
	 */
	public static String warning() {
		return "Warning";
	}

	/**
	 * @return The HTTP {@code WWW-Authenticate} header field name.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.1">Section 4.1 of RFC
	 * 7235</a>
	 */
	public static String wwwAuthenticate() {
		return "WWW-Authenticate";
	}

}
