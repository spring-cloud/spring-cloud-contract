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

package org.springframework.cloud.contract.spec.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper functions for HTTP statuses.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.0.0
 */
public final class HttpStatus {

	private static final Log log = LogFactory.getLog(HttpStatus.class);

	public HttpStatus() {
		log.warn("WARNING: HttpStatus shouldn't be instantiated. Use its static methods instead.");
	}

	/**
	 * The HTTP {@code 100 Continue} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.1">HTTP/1.1:
	 * Semantics and Content, section 6.2.1</a>
	 */
	public static final int CONTINUE = 100;

	/**
	 * The HTTP {@code 101 Switching Protocols} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.2">HTTP/1.1:
	 * Semantics and Content, section 6.2.2</a>
	 */
	public static final int SWITCHING_PROTOCOLS = 101;

	/**
	 * The HTTP {@code 102 Processing} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
	 */
	public static final int PROCESSING = 102;

	/**
	 * The HTTP {@code 103 Checkpoint} status code.
	 * @see <a href="https://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal" >
	 * A proposal for supporting resumable POST/PUT HTTP requests in HTTP/1.0</a>
	 */
	public static final int CHECKPOINT = 103;

	/**
	 * The HTTP {@code 200 OK} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">HTTP/1.1:
	 * Semantics and Content, section 6.3.1</a>
	 */
	public static final int OK = 200;

	/**
	 * The HTTP {@code 201 Created} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.2">HTTP/1.1:
	 * Semantics and Content, section 6.3.2</a>
	 */
	public static final int CREATED = 201;

	/**
	 * The HTTP {@code 202 Accepted} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.3">HTTP/1.1:
	 * Semantics and Content, section 6.3.3</a>
	 */
	public static final int ACCEPTED = 202;

	/**
	 * The HTTP {@code 203 Non-Authoritative Information} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.4">HTTP/1.1:
	 * Semantics and Content, section 6.3.4</a>
	 */
	public static final int NON_AUTHORITATIVE_INFORMATION = 203;

	/**
	 * The HTTP {@code 204 No Content} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.5">HTTP/1.1:
	 * Semantics and Content, section 6.3.5</a>
	 */
	public static final int NO_CONTENT = 204;

	/**
	 * The HTTP {@code 205 Reset Content} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.6">HTTP/1.1:
	 * Semantics and Content, section 6.3.6</a>
	 */
	public static final int RESET_CONTENT = 205;

	/**
	 * The HTTP {@code 206 Partial Content} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.1">HTTP/1.1: Range
	 * Requests, section 4.1</a>
	 */
	public static final int PARTIAL_CONTENT = 206;

	/**
	 * The HTTP {@code 207 Multi-Status} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
	 */
	public static final int MULTI_STATUS = 207;

	/**
	 * The HTTP {@code 208 Already Reported} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding
	 * Extensions</a>
	 */
	public static final int ALREADY_REPORTED = 208;

	/**
	 * The HTTP {@code 226 IM Used} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in
	 * HTTP</a>
	 */
	public static final int IM_USED = 226;

	/**
	 * The HTTP {@code 300 Multiple Choices} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.1">HTTP/1.1:
	 * Semantics and Content, section 6.4.1</a>
	 */
	public static final int MULTIPLE_CHOICES = 300;

	/**
	 * The HTTP {@code 301 Moved Permanently} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.2">HTTP/1.1:
	 * Semantics and Content, section 6.4.2</a>
	 */
	public static final int MOVED_PERMANENTLY = 301;

	/**
	 * The HTTP {@code 302 Found} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.3">HTTP/1.1:
	 * Semantics and Content, section 6.4.3</a>
	 */
	public static final int FOUND = 302;

	/**
	 * The HTTP {@code 303 See Other} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.4">HTTP/1.1:
	 * Semantics and Content, section 6.4.4</a>
	 */
	public static final int SEE_OTHER = 303;

	/**
	 * The HTTP {@code 304 Not Modified} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.1">HTTP/1.1:
	 * Conditional Requests, section 4.1</a>
	 */
	public static final int NOT_MODIFIED = 304;

	/**
	 * The HTTP {@code 307 Temporary Redirect} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.7">HTTP/1.1:
	 * Semantics and Content, section 6.4.7</a>
	 */
	public static final int TEMPORARY_REDIRECT = 307;

	/**
	 * The HTTP {@code 308 Permanent Redirect} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7238">RFC 7238</a>
	 */
	public static final int PERMANENT_REDIRECT = 308;

	/**
	 * The HTTP {@code 400 Bad Request} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">HTTP/1.1:
	 * Semantics and Content, section 6.5.1</a>
	 */
	public static final int BAD_REQUEST = 400;

	/**
	 * The HTTP {@code 401 Unauthorized} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1:
	 * Authentication, section 3.1</a>
	 */
	public static final int UNAUTHORIZED = 401;

	/**
	 * The HTTP {@code 402 Payment Required} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.2">HTTP/1.1:
	 * Semantics and Content, section 6.5.2</a>
	 */
	public static final int PAYMENT_REQUIRED = 402;

	/**
	 * The HTTP {@code 403 Forbidden} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1:
	 * Semantics and Content, section 6.5.3</a>
	 */
	public static final int FORBIDDEN = 403;

	/**
	 * The HTTP {@code 404 Not Found} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1:
	 * Semantics and Content, section 6.5.4</a>
	 */
	public static final int NOT_FOUND = 404;

	/**
	 * The HTTP {@code 405 Method Not Allowed} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">HTTP/1.1:
	 * Semantics and Content, section 6.5.5</a>
	 */
	public static final int METHOD_NOT_ALLOWED = 405;

	/**
	 * The HTTP {@code 406 Not Acceptable} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.6">HTTP/1.1:
	 * Semantics and Content, section 6.5.6</a>
	 */
	public static final int NOT_ACCEPTABLE = 406;

	/**
	 * The HTTP {@code 407 Proxy Authentication Required} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.2">HTTP/1.1:
	 * Authentication, section 3.2</a>
	 */
	public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

	/**
	 * The HTTP {@code 408 Request Timeout} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.7">HTTP/1.1:
	 * Semantics and Content, section 6.5.7</a>
	 */
	public static final int REQUEST_TIMEOUT = 408;

	/**
	 * The HTTP {@code 409 Conflict} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">HTTP/1.1:
	 * Semantics and Content, section 6.5.8</a>
	 */
	public static final int CONFLICT = 409;

	/**
	 * The HTTP {@code 410 Gone} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.9">HTTP/1.1:
	 * Semantics and Content, section 6.5.9</a>
	 */
	public static final int GONE = 410;

	/**
	 * The HTTP {@code 411 Length Required} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.10">HTTP/1.1:
	 * Semantics and Content, section 6.5.10</a>
	 */
	public static final int LENGTH_REQUIRED = 411;

	/**
	 * The HTTP {@code 412 Precondition failed} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.2">HTTP/1.1:
	 * Conditional Requests, section 4.2</a>
	 */
	public static final int PRECONDITION_FAILED = 412;

	/**
	 * The HTTP {@code 413 Payload Too Large} status code.
	 * @since 4.1* @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.11" >
	 * HTTP/1.1: Semantics and Content, section 6.5.11</a>
	 */
	public static final int PAYLOAD_TOO_LARGE = 413;

	/**
	 * The HTTP {@code 414 URI Too Long} status code.
	 * @since 4.1* @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12" >
	 * HTTP/1.1: Semantics and Content, section 6.5.12</a>
	 */
	public static final int URI_TOO_LONG = 414;

	/**
	 * The HTTP {@code 415 Unsupported Media Type} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.13">HTTP/1.1:
	 * Semantics and Content, section 6.5.13</a>
	 */
	public static final int UNSUPPORTED_MEDIA_TYPE = 415;

	/**
	 * The HTTP {@code 416 Requested Range Not Satisfiable} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.4">HTTP/1.1: Range
	 * Requests, section 4.4</a>
	 */
	public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;

	/**
	 * The HTTP {@code 417 Expectation Failed} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.14">HTTP/1.1:
	 * Semantics and Content, section 6.5.14</a>
	 */
	public static final int EXPECTATION_FAILED = 417;

	/**
	 * The HTTP {@code 418 I'm a teapot} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
	 */
	public static final int I_AM_A_TEAPOT = 418;

	/**
	 * The HTTP {@code 422 Unprocessable Entity} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
	 */
	public static final int UNPROCESSABLE_ENTITY = 422;

	/**
	 * The HTTP {@code 423 Locked} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
	 */
	public static final int LOCKED = 423;

	/**
	 * The HTTP {@code 424 Failed Dependency} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
	 */
	public static final int FAILED_DEPENDENCY = 424;

	/**
	 * The HTTP {@code 426 Upgrade Required} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS
	 * Within HTTP/1.1</a>
	 */
	public static final int UPGRADE_REQUIRED = 426;

	/**
	 * The HTTP {@code 428 Precondition Required} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status
	 * Codes</a>
	 */
	public static final int PRECONDITION_REQUIRED = 428;

	/**
	 * The HTTP {@code 429 Too Many Requests} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status
	 * Codes</a>
	 */
	public static final int TOO_MANY_REQUESTS = 429;

	/**
	 * The HTTP {@code 431 Request Header Fields Too Large} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status
	 * Codes</a>
	 */
	public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

	/**
	 * The HTTP {@code 451 Unavailable For Legal Reasons} status code.
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-httpbis-legally-restricted-status-04" > An
	 * HTTP Status Code to Report Legal Obstacles</a>
	 */
	public static final int UNAVAILABLE_FOR_LEGAL_REASONS = 451;

	/**
	 * The HTTP {@code 500 Internal Server Error} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">HTTP/1.1:
	 * Semantics and Content, section 6.6.1</a>
	 */
	public static final int INTERNAL_SERVER_ERROR = 500;

	/**
	 * The HTTP {@code 501 Not Implemented} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">HTTP/1.1:
	 * Semantics and Content, section 6.6.2</a>
	 */
	public static final int NOT_IMPLEMENTED = 501;

	/**
	 * The HTTP {@code 502 Bad Gateway} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">HTTP/1.1:
	 * Semantics and Content, section 6.6.3</a>
	 */
	public static final int BAD_GATEWAY = 502;

	/**
	 * The HTTP {@code 503 Service Unavailable} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.4">HTTP/1.1:
	 * Semantics and Content, section 6.6.4</a>
	 */
	public static final int SERVICE_UNAVAILABLE = 503;

	/**
	 * The HTTP {@code 504 Gateway Timeout} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.5">HTTP/1.1:
	 * Semantics and Content, section 6.6.5</a>
	 */
	public static final int GATEWAY_TIMEOUT = 504;

	/**
	 * The HTTP {@code 505 HTTP Version Not Supported} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.6">HTTP/1.1:
	 * Semantics and Content, section 6.6.6</a>
	 */
	public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

	/**
	 * The HTTP {@code 506 Variant Also Negotiates} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content
	 * Negotiation</a>
	 */
	public static final int VARIANT_ALSO_NEGOTIATES = 506;

	/**
	 * The HTTP {@code 507 Insufficient Storage} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
	 */
	public static final int INSUFFICIENT_STORAGE = 507;

	/**
	 * The HTTP {@code 508 Loop Detected} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding
	 * Extensions</a>
	 */
	public static final int LOOP_DETECTED = 508;

	/**
	 * The HTTP {@code 509 Bandwidth Limit Exceeded} status code.
	 */
	public static final int BANDWIDTH_LIMIT_EXCEEDED = 509;

	/**
	 * The HTTP {@code 510 Not Extended} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc2774#section-7">HTTP Extension
	 * Framework</a>
	 */
	public static final int NOT_EXTENDED = 510;

	/**
	 * The HTTP {@code 511 Network Authentication Required} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status
	 * Codes</a>
	 */
	public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

}
