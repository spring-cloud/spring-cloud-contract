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
 * Helper functions for HTTP statuses.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.0.0
 */
public final class HttpStatus {

	public HttpStatus() {
		System.out.println("WARNING: HttpStatus shouldn't be instantiated");
	}

	/**
	 * @return {@code 100 Continue}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.1">HTTP/1.1:
	 * Semantics and Content, section 6.2.1</a>
	 */
	public static int CONTINUE() {
		return 100;
	}

	/**
	 * @return {@code 101 Switching Protocols}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.2">HTTP/1.1:
	 * Semantics and Content, section 6.2.2</a>
	 */
	public static int SWITCHING_PROTOCOLS() {
		return 101;
	}

	/**
	 * @return {@code 102 Processing}.
	 * @see <a href="https://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
	 */
	public static int PROCESSING() {
		return 102;
	}

	/**
	 * @return {@code 103 Checkpoint}.
	 * @see <a href="https://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal" >
	 * A proposal for supporting resumable POST/PUT HTTP requests in HTTP/1.0</a>
	 */
	public static int CHECKPOINT() {
		return 103;
	}

	/**
	 * @return {@code 200 OK}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">HTTP/1.1:
	 * Semantics and Content, section 6.3.1</a>
	 */
	public static int OK() {
		return 200;
	}

	/**
	 * @return {@code 201 Created}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.2">HTTP/1.1:
	 * Semantics and Content, section 6.3.2</a>
	 */
	public static int CREATED() {
		return 201;
	}

	/**
	 * @return {@code 202 Accepted}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.3">HTTP/1.1:
	 * Semantics and Content, section 6.3.3</a>
	 */
	public static int ACCEPTED() {
		return 202;
	}

	/**
	 * @return {@code 203 Non-Authoritative Information}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.4">HTTP/1.1:
	 * Semantics and Content, section 6.3.4</a>
	 */
	public static int NON_AUTHORITATIVE_INFORMATION() {
		return 203;
	}

	/**
	 * @return {@code 204 No Content}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.5">HTTP/1.1:
	 * Semantics and Content, section 6.3.5</a>
	 */
	public static int NO_CONTENT() {
		return 204;
	}

	/**
	 * @return {@code 205 Reset Content}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.6">HTTP/1.1:
	 * Semantics and Content, section 6.3.6</a>
	 */
	public static int RESET_CONTENT() {
		return 205;
	}

	/**
	 * @return {@code 206 Partial Content}.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.1">HTTP/1.1: Range
	 * Requests, section 4.1</a>
	 */
	public static int PARTIAL_CONTENT() {
		return 206;
	}

	/**
	 * @return {@code 207 Multi-Status}.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
	 */
	public static int MULTI_STATUS() {
		return 207;
	}

	/**
	 * @return {@code 208 Already Reported}.
	 * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding
	 * Extensions</a>
	 */
	public static int ALREADY_REPORTED() {
		return 208;
	}

	/**
	 * @return {@code 226 IM Used}.
	 * @see <a href="https://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in
	 * HTTP</a>
	 */
	public static int IM_USED() {
		return 226;
	}

	/**
	 * @return {@code 300 Multiple Choices}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.1">HTTP/1.1:
	 * Semantics and Content, section 6.4.1</a>
	 */
	public static int MULTIPLE_CHOICES() {
		return 300;
	}

	/**
	 * @return {@code 301 Moved Permanently}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.2">HTTP/1.1:
	 * Semantics and Content, section 6.4.2</a>
	 */
	public static int MOVED_PERMANENTLY() {
		return 301;
	}

	/**
	 * @return {@code 302 Found}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.3">HTTP/1.1:
	 * Semantics and Content, section 6.4.3</a>
	 */
	public static int FOUND() {
		return 302;
	}

	/**
	 * @return {@code 302 Moved Temporarily}.
	 * @see <a href="https://tools.ietf.org/html/rfc1945#section-9.3">HTTP/1.0, section
	 * 9.3</a>
	 * @deprecated in favor of {@link #FOUND} which will be returned from
	 */
	@Deprecated
	public static int MOVED_TEMPORARILY() {
		return 302;
	}

	/**
	 * @return {@code 303 See Other}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.4">HTTP/1.1:
	 * Semantics and Content, section 6.4.4</a>
	 */
	public static int SEE_OTHER() {
		return 303;
	}

	/**
	 * @return {@code 304 Not Modified}.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.1">HTTP/1.1:
	 * Conditional Requests, section 4.1</a>
	 */
	public static int NOT_MODIFIED() {
		return 304;
	}

	/**
	 * @return {@code 305 Use Proxy}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.5">HTTP/1.1:
	 * Semantics and Content, section 6.4.5</a>
	 * @deprecated due to security concerns regarding in-band configuration of a proxy
	 */
	@Deprecated
	public static int USE_PROXY() {
		return 305;
	}

	/**
	 * @return {@code 307 Temporary Redirect}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.7">HTTP/1.1:
	 * Semantics and Content, section 6.4.7</a>
	 */
	public static int TEMPORARY_REDIRECT() {
		return 307;
	}

	/**
	 * @return {@code 308 Permanent Redirect}.
	 * @see <a href="https://tools.ietf.org/html/rfc7238">RFC 7238</a>
	 */
	public static int PERMANENT_REDIRECT() {
		return 308;
	}

	/**
	 * @return {@code 400 Bad Request}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">HTTP/1.1:
	 * Semantics and Content, section 6.5.1</a>
	 */
	public static int BAD_REQUEST() {
		return 400;
	}

	/**
	 * @return {@code 401 Unauthorized}.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1:
	 * Authentication, section 3.1</a>
	 */
	public static int UNAUTHORIZED() {
		return 401;
	}

	/**
	 * @return {@code 402 Payment Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.2">HTTP/1.1:
	 * Semantics and Content, section 6.5.2</a>
	 */
	public static int PAYMENT_REQUIRED() {
		return 402;
	}

	/**
	 * @return {@code 403 Forbidden}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1:
	 * Semantics and Content, section 6.5.3</a>
	 */
	public static int FORBIDDEN() {
		return 403;
	}

	/**
	 * @return {@code 404 Not Found}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1:
	 * Semantics and Content, section 6.5.4</a>
	 */
	public static int NOT_FOUND() {
		return 404;
	}

	/**
	 * @return {@code 405 Method Not Allowed}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">HTTP/1.1:
	 * Semantics and Content, section 6.5.5</a>
	 */
	public static int METHOD_NOT_ALLOWED() {
		return 405;
	}

	/**
	 * @return {@code 406 Not Acceptable}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.6">HTTP/1.1:
	 * Semantics and Content, section 6.5.6</a>
	 */
	public static int NOT_ACCEPTABLE() {
		return 406;
	}

	/**
	 * @return {@code 407 Proxy Authentication Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.2">HTTP/1.1:
	 * Authentication, section 3.2</a>
	 */
	public static int PROXY_AUTHENTICATION_REQUIRED() {
		return 407;
	}

	/**
	 * @return {@code 408 Request Timeout}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.7">HTTP/1.1:
	 * Semantics and Content, section 6.5.7</a>
	 */
	public static int REQUEST_TIMEOUT() {
		return 408;
	}

	/**
	 * @return {@code 409 Conflict}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">HTTP/1.1:
	 * Semantics and Content, section 6.5.8</a>
	 */
	public static int CONFLICT() {
		return 409;
	}

	/**
	 * @return {@code 410 Gone}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.9">HTTP/1.1:
	 * Semantics and Content, section 6.5.9</a>
	 */
	public static int GONE() {
		return 410;
	}

	/**
	 * @return {@code 411 Length Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.10">HTTP/1.1:
	 * Semantics and Content, section 6.5.10</a>
	 */
	public static int LENGTH_REQUIRED() {
		return 411;
	}

	/**
	 * @return {@code 412 Precondition failed}.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.2">HTTP/1.1:
	 * Conditional Requests, section 4.2</a>
	 */
	public static int PRECONDITION_FAILED() {
		return 412;
	}

	/**
	 * @return {@code 413 Payload Too Large}.
	 * @since 4.1* @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.11" >
	 * HTTP/1.1: Semantics and Content, section 6.5.11</a>
	 */
	public static int PAYLOAD_TOO_LARGE() {
		return 413;
	}

	/**
	 * @return {@code 413 Request Entity Too Large}.
	 * @see <a href="https://tools.ietf.org/html/rfc2616#section-10.4.14">HTTP/1.1,
	 * section 10.4.14</a>
	 * @deprecated in favor of {@link #PAYLOAD_TOO_LARGE} which will be returned from
	 */
	@Deprecated
	public static int REQUEST_ENTITY_TOO_LARGE() {
		return 413;
	}

	/**
	 * @return {@code 414 URI Too Long}.
	 * @since 4.1* @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12" >
	 * HTTP/1.1: Semantics and Content, section 6.5.12</a>
	 */
	public static int URI_TOO_LONG() {
		return 414;
	}

	/**
	 * @return {@code 414 Request-URI Too Long}.
	 * @see <a href="https://tools.ietf.org/html/rfc2616#section-10.4.15">HTTP/1.1,
	 * section 10.4.15</a>
	 * @deprecated in favor of {@link #URI_TOO_LONG} which will be returned from
	 */
	@Deprecated
	public static int REQUEST_URI_TOO_LONG() {
		return 414;
	}

	/**
	 * @return {@code 415 Unsupported Media Type}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.13">HTTP/1.1:
	 * Semantics and Content, section 6.5.13</a>
	 */
	public static int UNSUPPORTED_MEDIA_TYPE() {
		return 415;
	}

	/**
	 * @return {@code 416 Requested Range Not Satisfiable}.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.4">HTTP/1.1: Range
	 * Requests, section 4.4</a>
	 */
	public static int REQUESTED_RANGE_NOT_SATISFIABLE() {
		return 416;
	}

	/**
	 * @return {@code 417 Expectation Failed}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.14">HTTP/1.1:
	 * Semantics and Content, section 6.5.14</a>
	 */
	public static int EXPECTATION_FAILED() {
		return 417;
	}

	/**
	 * @return {@code 418 I'm a teapot}.
	 * @see <a href="https://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
	 */
	public static int I_AM_A_TEAPOT() {
		return 418;
	}

	/**
	 * @return {@code 419 Insufficient Space}.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public static int INSUFFICIENT_SPACE_ON_RESOURCE() {
		return 419;
	}

	/**
	 * @return {@code 420 Method Failure}.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public static int METHOD_FAILURE() {
		return 420;
	}

	/**
	 * @return {@code 421 Destination Locked}.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public static int DESTINATION_LOCKED() {
		return 421;
	}

	/**
	 * @return {@code 422 Unprocessable Entity}.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
	 */
	public static int UNPROCESSABLE_ENTITY() {
		return 422;
	}

	/**
	 * @return {@code 423 Locked}.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
	 */
	public static int LOCKED() {
		return 423;
	}

	/**
	 * @return {@code 424 Failed Dependency}.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
	 */
	public static int FAILED_DEPENDENCY() {
		return 424;
	}

	/**
	 * @return {@code 426 Upgrade Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS
	 * Within HTTP/1.1</a>
	 */
	public static int UPGRADE_REQUIRED() {
		return 426;
	}

	/**
	 * @return {@code 428 Precondition Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status
	 * Codes</a>
	 */
	public static int PRECONDITION_REQUIRED() {
		return 428;
	}

	/**
	 * @return {@code 429 Too Many Requests}.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status
	 * Codes</a>
	 */
	public static int TOO_MANY_REQUESTS() {
		return 429;
	}

	/**
	 * @return {@code 431 Request Header Fields Too Large}.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status
	 * Codes</a>
	 */
	public static int REQUEST_HEADER_FIELDS_TOO_LARGE() {
		return 431;
	}

	/**
	 * @return {@code 451 Unavailable For Legal Reasons}.
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-httpbis-legally-restricted-status-04" > An
	 * HTTP Status Code to Report Legal Obstacles</a>
	 */
	public static int UNAVAILABLE_FOR_LEGAL_REASONS() {
		return 451;
	}

	/**
	 * @return {@code 500 Internal Server Error}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">HTTP/1.1:
	 * Semantics and Content, section 6.6.1</a>
	 */
	public static int INTERNAL_SERVER_ERROR() {
		return 500;
	}

	/**
	 * @return {@code 501 Not Implemented}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">HTTP/1.1:
	 * Semantics and Content, section 6.6.2</a>
	 */
	public static int NOT_IMPLEMENTED() {
		return 501;
	}

	/**
	 * @return {@code 502 Bad Gateway}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">HTTP/1.1:
	 * Semantics and Content, section 6.6.3</a>
	 */
	public static int BAD_GATEWAY() {
		return 502;
	}

	/**
	 * @return {@code 503 Service Unavailable}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.4">HTTP/1.1:
	 * Semantics and Content, section 6.6.4</a>
	 */
	public static int SERVICE_UNAVAILABLE() {
		return 503;
	}

	/**
	 * @return {@code 504 Gateway Timeout}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.5">HTTP/1.1:
	 * Semantics and Content, section 6.6.5</a>
	 */
	public static int GATEWAY_TIMEOUT() {
		return 504;
	}

	/**
	 * @return {@code 505 HTTP Version Not Supported}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.6">HTTP/1.1:
	 * Semantics and Content, section 6.6.6</a>
	 */
	public static int HTTP_VERSION_NOT_SUPPORTED() {
		return 505;
	}

	/**
	 * @return {@code 506 Variant Also Negotiates}
	 * @see <a href="https://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content
	 * Negotiation</a>
	 */
	public static int VARIANT_ALSO_NEGOTIATES() {
		return 506;
	}

	/**
	 * @return {@code 507 Insufficient Storage}
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
	 */
	public static int INSUFFICIENT_STORAGE() {
		return 507;
	}

	/**
	 * @return {@code 508 Loop Detected}
	 * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding
	 * Extensions</a>
	 */
	public static int LOOP_DETECTED() {
		return 508;
	}

	/**
	 * @return {@code 509 Bandwidth Limit Exceeded}
	 */
	public static int BANDWIDTH_LIMIT_EXCEEDED() {
		return 509;
	}

	/**
	 * @return {@code 510 Not Extended}
	 * @see <a href="https://tools.ietf.org/html/rfc2774#section-7">HTTP Extension
	 * Framework</a>
	 */
	public static int NOT_EXTENDED() {
		return 510;
	}

	/**
	 * @return {@code 511 Network Authentication Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status
	 * Codes</a>
	 */
	public static int NETWORK_AUTHENTICATION_REQUIRED() {
		return 511;
	}

}
