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
 * @since 1.0.2
 */
public class HttpHeaders {

	/**
	 * The HTTP {@code Accept} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.3.2" > Section 5.3.2 of
	 * RFC 7231</a>
	 */
	public String accept() {
		return "Accept";
	}

	/**
	 * The HTTP {@code Accept-Charset} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.3.3" > Section 5.3.3 of
	 * RFC 7231</a>
	 */
	public String acceptCharset() {
		return "Accept-Charset";
	}

	/**
	 * The HTTP {@code Accept-Encoding} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.3.4" > Section 5.3.4 of
	 * RFC 7231</a>
	 */
	public String acceptEncoding() {
		return "Accept-Encoding";
	}

	/**
	 * The HTTP {@code Accept-Language} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.3.5" > Section 5.3.5 of
	 * RFC 7231</a>
	 */
	public String acceptLanguage() {
		return "Accept-Language";
	}

	/**
	 * The HTTP {@code Accept-Ranges} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7233#section-2.3" > Section 5.3.5 of
	 * RFC 7233</a>
	 */
	public String acceptRanges() {
		return "Accept-Ranges";
	}

	/**
	 * The CORS {@code Access-Control-Allow-Credentials} response header field name.
	 * @see <ahref="https://www.w3.org/TR/cors/" > CORS W3C recommendation</a>
	 */
	public String accessControlAllowCredentials() {
		return "Access-Control-Allow-Credentials";
	}

	/**
	 * The CORS {@code Access-Control-Allow-Headers} response header field name.
	 * @see <ahref="https://www.w3.org/TR/cors/" > CORS W3C recommendation</a>
	 */
	public String accessControlAllowHeaders() {
		return "Access-Control-Allow-Headers";
	}

	/**
	 * The CORS {@code Access-Control-Allow-Methods} response header field name.
	 * @see <ahref="https://www.w3.org/TR/cors/" > CORS W3C recommendation</a>
	 */
	public String accessControlAllowMethods() {
		return "Access-Control-Allow-Methods";
	}

	/**
	 * The CORS {@code Access-Control-Allow-Origin} response header field name.
	 * @see <ahref="https://www.w3.org/TR/cors/" > CORS W3C recommendation</a>
	 */
	public String accessControlAllowOrigin() {
		return "Access-Control-Allow-Origin";
	}

	/**
	 * The CORS {@code Access-Control-Expose-Headers} response header field name.
	 * @see <ahref="https://www.w3.org/TR/cors/" > CORS W3C recommendation</a>
	 */
	public String accessControlExposeHeaders() {
		return "Access-Control-Expose-Headers";
	}

	/**
	 * The CORS {@code Access-Control-Max-Age} response header field name.
	 * @see <ahref="https://www.w3.org/TR/cors/" > CORS W3C recommendation</a>
	 */
	public String accessControlMaxAge() {
		return "Access-Control-Max-Age";
	}

	/**
	 * The CORS {@code Access-Control-Request-Headers} request header field name.
	 * @see <ahref="https://www.w3.org/TR/cors/" > CORS W3C recommendation</a>
	 */
	public String accessControlRequestHeaders() {
		return "Access-Control-Request-Headers";
	}

	/**
	 * The CORS {@code Access-Control-Request-Method} request header field name.
	 * @see <ahref="https://www.w3.org/TR/cors/" > CORS W3C recommendation</a>
	 */
	public String accessControlRequestMethod() {
		return "Access-Control-Request-Method";
	}

	/**
	 * The HTTP {@code Age} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7234#section-5.1" > Section 5.1 of RFC
	 * 7234</a>
	 */
	public String age() {
		return "Age";
	}

	/**
	 * The HTTP {@code Allow} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-7.4.1" > Section 7.4.1 of
	 * RFC 7231</a>
	 */
	public String allow() {
		return "Allow";
	}

	/**
	 * The HTTP {@code Authorization} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7235#section-4.2" > Section 4.2 of RFC
	 * 7235</a>
	 */
	public String authorization() {
		return "Authorization";
	}

	/**
	 * The HTTP {@code Cache-Control} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7234#section-5.2" > Section 5.2 of RFC
	 * 7234</a>
	 */
	public String cacheControl() {
		return "Cache-Control";
	}

	/**
	 * The HTTP {@code Connection} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7230#section-6.1" > Section 6.1 of RFC
	 * 7230</a>
	 */
	public String connection() {
		return "Connection";
	}

	/**
	 * The HTTP {@code Content-Encoding} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-3.1.2.2" > Section 3.1.2.2
	 * of RFC 7231</a>
	 */
	public String contentEncoding() {
		return "Content-Encoding";
	}

	/**
	 * The HTTP {@code Content-Disposition} header field name
	 * @see <ahref="https://tools.ietf.org/html/rfc6266" > RFC 6266</a>
	 */
	public String contentDisposition() {
		return "Content-Disposition";
	}

	/**
	 * The HTTP {@code Content-Language} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-3.1.3.2" > Section 3.1.3.2
	 * of RFC 7231</a>
	 */
	public String contentLanguage() {
		return "Content-Language";
	}

	/**
	 * The HTTP {@code Content-Length} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7230#section-3.3.2" > Section 3.3.2 of
	 * RFC 7230</a>
	 */
	public String contentLength() {
		return "Content-Length";
	}

	/**
	 * The HTTP {@code Content-Location} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-3.1.4.2" > Section 3.1.4.2
	 * of RFC 7231</a>
	 */
	public String contentLocation() {
		return "Content-Location";
	}

	/**
	 * The HTTP {@code Content-Range} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7233#section-4.2" > Section 4.2 of RFC
	 * 7233</a>
	 */
	public String contentRange() {
		return "Content-Range";
	}

	/**
	 * The HTTP {@code Content-Type} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-3.1.1.5" > Section 3.1.1.5
	 * of RFC 7231</a>
	 */
	public String contentType() {
		return "Content-Type";
	}

	/**
	 * The HTTP {@code Cookie} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc2109#section-4.3.4" > Section 4.3.4 of
	 * RFC 2109</a>
	 */
	public String cookie() {
		return "Cookie";
	}

	/**
	 * The HTTP {@code Date} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-7.1.1.2" > Section 7.1.1.2
	 * of RFC 7231</a>
	 */
	public String date() {
		return "Date";
	}

	/**
	 * The HTTP {@code ETag} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7232#section-2.3" > Section 2.3 of RFC
	 * 7232</a>
	 */
	public String etag() {
		return "ETag";
	}

	/**
	 * The HTTP {@code Expect} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.1.1" > Section 5.1.1 of
	 * RFC 7231</a>
	 */
	public String expect() {
		return "Expect";
	}

	/**
	 * The HTTP {@code Expires} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7234#section-5.3" > Section 5.3 of RFC
	 * 7234</a>
	 */
	public String expires() {
		return "Expires";
	}

	/**
	 * The HTTP {@code From} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.5.1" > Section 5.5.1 of
	 * RFC 7231</a>
	 */
	public String from() {
		return "From";
	}

	/**
	 * The HTTP {@code Host} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7230#section-5.4" > Section 5.4 of RFC
	 * 7230</a>
	 */
	public String host() {
		return "Host";
	}

	/**
	 * The HTTP {@code If-Match} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7232#section-3.1" > Section 3.1 of RFC
	 * 7232</a>
	 */
	public String ifMatch() {
		return "If-Match";
	}

	/**
	 * The HTTP {@code If-Modified-Since} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7232#section-3.3" > Section 3.3 of RFC
	 * 7232</a>
	 */
	public String ifModifiedSince() {
		return "If-Modified-Since";
	}

	/**
	 * The HTTP {@code If-None-Match} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7232#section-3.2" > Section 3.2 of RFC
	 * 7232</a>
	 */
	public String ifNoneMatch() {
		return "If-None-Match";
	}

	/**
	 * The HTTP {@code If-Range} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7233#section-3.2" > Section 3.2 of RFC
	 * 7233</a>
	 */
	public String ifRange() {
		return "If-Range";
	}

	/**
	 * The HTTP {@code If-Unmodified-Since} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7232#section-3.4" > Section 3.4 of RFC
	 * 7232</a>
	 */
	public String ifUnmodifiedSince() {
		return "If-Unmodified-Since";
	}

	/**
	 * The HTTP {@code Last-Modified} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7232#section-2.2" > Section 2.2 of RFC
	 * 7232</a>
	 */
	public String lastModified() {
		return "Last-Modified";
	}

	/**
	 * The HTTP {@code Link} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc5988" > RFC 5988</a>
	 */
	public String link() {
		return "Link";
	}

	/**
	 * The HTTP {@code Location} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-7.1.2" > Section 7.1.2 of
	 * RFC 7231</a>
	 */
	public String location() {
		return "Location";
	}

	/**
	 * The HTTP {@code Max-Forwards} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.1.2" > Section 5.1.2 of
	 * RFC 7231</a>
	 */
	public String max_forwards() {
		return "Max-Forwards";
	}

	/**
	 * The HTTP {@code Origin} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc6454" > RFC 6454</a>
	 */
	public String origin() {
		return "Origin";
	}

	/**
	 * The HTTP {@code Pragma} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7234#section-5.4" > Section 5.4 of RFC
	 * 7234</a>
	 */
	public String pragma() {
		return "Pragma";
	}

	/**
	 * The HTTP {@code Proxy-Authenticate} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7235#section-4.3" > Section 4.3 of RFC
	 * 7235</a>
	 */
	public String proxyAuthenticate() {
		return "Proxy-Authenticate";
	}

	/**
	 * The HTTP {@code Proxy-Authorization} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7235#section-4.4" > Section 4.4 of RFC
	 * 7235</a>
	 */
	public String proxyAuthorization() {
		return "Proxy-Authorization";
	}

	/**
	 * The HTTP {@code Range} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7233#section-3.1" > Section 3.1 of RFC
	 * 7233</a>
	 */
	public String range() {
		return "Range";
	}

	/**
	 * The HTTP {@code Referer} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.5.2" > Section 5.5.2 of
	 * RFC 7231</a>
	 */
	public String referer() {
		return "Referer";
	}

	/**
	 * The HTTP {@code Retry-After} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-7.1.3" > Section 7.1.3 of
	 * RFC 7231</a>
	 */
	public String retryAfter() {
		return "Retry-After";
	}

	/**
	 * The HTTP {@code Server} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-7.4.2" > Section 7.4.2 of
	 * RFC 7231</a>
	 */
	public String server() {
		return "Server";
	}

	/**
	 * The HTTP {@code Set-Cookie} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc2109#section-4.2.2" > Section 4.2.2 of
	 * RFC 2109</a>
	 */
	public String setCookie() {
		return "Set-Cookie";
	}

	/**
	 * The HTTP {@code Set-Cookie2} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc2965" > RFC 2965</a>
	 */
	public String setCookie2() {
		return "Set-Cookie2";
	}

	/**
	 * The HTTP {@code TE} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7230#section-4.3" > Section 4.3 of RFC
	 * 7230</a>
	 */
	public String te() {
		return "TE";
	}

	/**
	 * The HTTP {@code Trailer} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7230#section-4.4" > Section 4.4 of RFC
	 * 7230</a>
	 */
	public String trailer() {
		return "Trailer";
	}

	/**
	 * The HTTP {@code Transfer-Encoding} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7230#section-3.3.1" > Section 3.3.1 of
	 * RFC 7230</a>
	 */
	public String transferEncoding() {
		return "Transfer-Encoding";
	}

	/**
	 * The HTTP {@code Upgrade} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7230#section-6.7" > Section 6.7 of RFC
	 * 7230</a>
	 */
	public String upgrade() {
		return "Upgrade";
	}

	/**
	 * The HTTP {@code User-Agent} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-5.5.3" > Section 5.5.3 of
	 * RFC 7231</a>
	 */
	public String user_agent() {
		return "User-Agent";
	}

	/**
	 * The HTTP {@code Vary} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7231#section-7.1.4" > Section 7.1.4 of
	 * RFC 7231</a>
	 */
	public String vary() {
		return "Vary";
	}

	/**
	 * The HTTP {@code Via} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7230#section-5.7.1" > Section 5.7.1 of
	 * RFC 7230</a>
	 */
	public String via() {
		return "Via";
	}

	/**
	 * The HTTP {@code Warning} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7234#section-5.5" > Section 5.5 of RFC
	 * 7234</a>
	 */
	public String warning() {
		return "Warning";
	}

	/**
	 * The HTTP {@code WWW-Authenticate} header field name.
	 * @see <ahref="https://tools.ietf.org/html/rfc7235#section-4.1" > Section 4.1 of RFC
	 * 7235</a>
	 */
	public String wwwAuthenticate() {
		return "WWW-Authenticate";
	}

}
