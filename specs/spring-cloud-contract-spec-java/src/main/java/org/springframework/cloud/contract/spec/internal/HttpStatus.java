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
		log.warn(
				"WARNING: HttpStatus shouldn't be instantiated. Use its static methods instead.");
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
	 * The HTTP {@code 302 Moved Temporarily} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc1945#section-9.3">HTTP/1.0, section
	 * 9.3</a>
	 * @deprecated in favor of {@link #FOUND} which will be returned from
	 */
	@Deprecated
	public static final int MOVED_TEMPORARILY = 302;

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
	 * The HTTP {@code 305 Use Proxy} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.5">HTTP/1.1:
	 * Semantics and Content, section 6.4.5</a>
	 * @deprecated due to security concerns regarding in-band configuration of a proxy
	 */
	@Deprecated
	public static final int USE_PROXY = 305;

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
	 * The HTTP {@code 413 Request Entity Too Large} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc2616#section-10.4.14">HTTP/1.1,
	 * section 10.4.14</a>
	 * @deprecated in favor of {@link #PAYLOAD_TOO_LARGE} which will be returned from
	 */
	@Deprecated
	public static final int REQUEST_ENTITY_TOO_LARGE = 413;

	/**
	 * The HTTP {@code 414 URI Too Long} status code.
	 * @since 4.1* @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12" >
	 * HTTP/1.1: Semantics and Content, section 6.5.12</a>
	 */
	public static final int URI_TOO_LONG = 414;

	/**
	 * The HTTP {@code 414 Request-URI Too Long} status code.
	 * @see <a href="https://tools.ietf.org/html/rfc2616#section-10.4.15">HTTP/1.1,
	 * section 10.4.15</a>
	 * @deprecated in favor of {@link #URI_TOO_LONG} which will be returned from
	 */
	@Deprecated
	public static final int REQUEST_URI_TOO_LONG = 414;

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
	 * The HTTP {@code 419 Insufficient Space} status code.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public static final int INSUFFICIENT_SPACE_ON_RESOURCE = 419;

	/**
	 * The HTTP {@code 420 Method Failure} status code.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public static final int METHOD_FAILURE = 420;

	/**
	 * The HTTP {@code 421 Destination Locked} status code.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public static final int DESTINATION_LOCKED = 421;

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

	/**
	 * @return {@code 100 Continue}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.1">HTTP/1.1:
	 * Semantics and Content, section 6.2.1</a>
	 * @deprecated Replaced by {@code HttpStatus.CONTINUE}.
	 */
	@Deprecated
	public int CONTINUE() {
		return HttpStatus.CONTINUE;
	}

	/**
	 * @return {@code 101 Switching Protocols}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.2">HTTP/1.1:
	 * Semantics and Content, section 6.2.2</a>
	 * @deprecated Replaced by {@code HttpStatus.SWITCHING_PROTOCOLS}.
	 */
	@Deprecated
	public int SWITCHING_PROTOCOLS() {
		return HttpStatus.SWITCHING_PROTOCOLS;
	}

	/**
	 * @return {@code 102 Processing}.
	 * @see <a href="https://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
	 * @deprecated Replaced by {@code HttpStatus.PROCESSING}.
	 */
	@Deprecated
	public int PROCESSING() {
		return HttpStatus.PROCESSING;
	}

	/**
	 * @return {@code 103 Checkpoint}.
	 * @see <a href="https://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal" >
	 * A proposal for supporting resumable POST/PUT HTTP requests in HTTP/1.0</a>
	 * @deprecated Replaced by {@code HttpStatus.CHECKPOINT}.
	 */
	@Deprecated
	public int CHECKPOINT() {
		return HttpStatus.CHECKPOINT;
	}

	/**
	 * @return {@code 200 OK}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">HTTP/1.1:
	 * Semantics and Content, section 6.3.1</a>
	 * @deprecated Replaced by {@code HttpStatus.OK}.
	 */
	@Deprecated
	public int OK() {
		return HttpStatus.OK;
	}

	/**
	 * @return {@code 201 Created}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.2">HTTP/1.1:
	 * Semantics and Content, section 6.3.2</a>
	 * @deprecated Replaced by {@code HttpStatus.CREATED}.
	 */
	@Deprecated
	public int CREATED() {
		return HttpStatus.CREATED;
	}

	/**
	 * @return {@code 202 Accepted}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.3">HTTP/1.1:
	 * Semantics and Content, section 6.3.3</a>
	 * @deprecated Replaced by {@code HttpStatus.ACCEPTED}.
	 */
	@Deprecated
	public int ACCEPTED() {
		return HttpStatus.ACCEPTED;
	}

	/**
	 * @return {@code 203 Non-Authoritative Information}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.4">HTTP/1.1:
	 * Semantics and Content, section 6.3.4</a>
	 * @deprecated Replaced by {@code HttpStatus.NON_AUTHORITATIVE_INFORMATION}.
	 */
	@Deprecated
	public int NON_AUTHORITATIVE_INFORMATION() {
		return HttpStatus.NON_AUTHORITATIVE_INFORMATION;
	}

	/**
	 * @return {@code 204 No Content}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.5">HTTP/1.1:
	 * Semantics and Content, section 6.3.5</a>
	 * @deprecated Replaced by {@code HttpStatus.NO_CONTENT}.
	 */
	@Deprecated
	public int NO_CONTENT() {
		return HttpStatus.NO_CONTENT;
	}

	/**
	 * @return {@code 205 Reset Content}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.6">HTTP/1.1:
	 * Semantics and Content, section 6.3.6</a>
	 * @deprecated Replaced by {@code HttpStatus.RESET_CONTENT}.
	 */
	@Deprecated
	public int RESET_CONTENT() {
		return HttpStatus.RESET_CONTENT;
	}

	/**
	 * @return {@code 206 Partial Content}.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.1">HTTP/1.1: Range
	 * Requests, section 4.1</a>
	 * @deprecated Replaced by {@code HttpStatus.PARTIAL_CONTENT}.
	 */
	@Deprecated
	public int PARTIAL_CONTENT() {
		return HttpStatus.PARTIAL_CONTENT;
	}

	/**
	 * @return {@code 207 Multi-Status}.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
	 * @deprecated Replaced by {@code HttpStatus.MULTI_STATUS}.
	 */
	@Deprecated
	public int MULTI_STATUS() {
		return HttpStatus.MULTI_STATUS;
	}

	/**
	 * @return {@code 208 Already Reported}.
	 * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding
	 * Extensions</a>
	 * @deprecated Replaced by {@code HttpStatus.ALREADY_REPORTED}.
	 */
	@Deprecated
	public int ALREADY_REPORTED() {
		return HttpStatus.ALREADY_REPORTED;
	}

	/**
	 * @return {@code 226 IM Used}.
	 * @see <a href="https://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in
	 * HTTP</a>
	 * @deprecated Replaced by {@code HttpStatus.IM_USED}.
	 */
	@Deprecated
	public int IM_USED() {
		return HttpStatus.IM_USED;
	}

	/**
	 * @return {@code 300 Multiple Choices}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.1">HTTP/1.1:
	 * Semantics and Content, section 6.4.1</a>
	 * @deprecated Replaced by {@code HttpStatus.MULTIPLE_CHOICES}.
	 */
	@Deprecated
	public int MULTIPLE_CHOICES() {
		return HttpStatus.MULTIPLE_CHOICES;
	}

	/**
	 * @return {@code 301 Moved Permanently}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.2">HTTP/1.1:
	 * Semantics and Content, section 6.4.2</a>
	 * @deprecated Replaced by {@code HttpStatus.MOVED_PERMANENTLY}.
	 */
	@Deprecated
	public int MOVED_PERMANENTLY() {
		return HttpStatus.MOVED_PERMANENTLY;
	}

	/**
	 * @return {@code 302 Found}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.3">HTTP/1.1:
	 * Semantics and Content, section 6.4.3</a>
	 * @deprecated Replaced by {@code HttpStatus.FOUND}.
	 */
	@Deprecated
	public int FOUND() {
		return HttpStatus.FOUND;
	}

	/**
	 * @return {@code 302 Moved Temporarily}.
	 * @see <a href="https://tools.ietf.org/html/rfc1945#section-9.3">HTTP/1.0, section
	 * 9.3</a>
	 * @deprecated in favor of {@link #FOUND} which will be returned from
	 */
	@Deprecated
	public int MOVED_TEMPORARILY() {
		return HttpStatus.MOVED_TEMPORARILY;
	}

	/**
	 * @return {@code 303 See Other}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.4">HTTP/1.1:
	 * Semantics and Content, section 6.4.4</a>
	 * @deprecated Replaced by {@code HttpStatus.SEE_OTHER}.
	 */
	@Deprecated
	public int SEE_OTHER() {
		return HttpStatus.SEE_OTHER;
	}

	/**
	 * @return {@code 304 Not Modified}.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.1">HTTP/1.1:
	 * Conditional Requests, section 4.1</a>
	 * @deprecated Replaced by {@code HttpStatus.NOT_MODIFIED}.
	 */
	@Deprecated
	public int NOT_MODIFIED() {
		return HttpStatus.NOT_MODIFIED;
	}

	/**
	 * @return {@code 305 Use Proxy}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.5">HTTP/1.1:
	 * Semantics and Content, section 6.4.5</a>
	 * @deprecated due to security concerns regarding in-band configuration of a proxy
	 */
	@Deprecated
	public int USE_PROXY() {
		return HttpStatus.USE_PROXY;
	}

	/**
	 * @return {@code 307 Temporary Redirect}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.7">HTTP/1.1:
	 * Semantics and Content, section 6.4.7</a>
	 * @deprecated Replaced by {@code HttpStatus.TEMPORARY_REDIRECT}.
	 */
	@Deprecated
	public int TEMPORARY_REDIRECT() {
		return HttpStatus.TEMPORARY_REDIRECT;
	}

	/**
	 * @return {@code 308 Permanent Redirect}.
	 * @see <a href="https://tools.ietf.org/html/rfc7238">RFC 7238</a>
	 * @deprecated Replaced by {@code HttpStatus.PERMANENT_REDIRECT}.
	 */
	@Deprecated
	public int PERMANENT_REDIRECT() {
		return HttpStatus.PERMANENT_REDIRECT;
	}

	/**
	 * @return {@code 400 Bad Request}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">HTTP/1.1:
	 * Semantics and Content, section 6.5.1</a>
	 * @deprecated Replaced by {@code HttpStatus.BAD_REQUEST}.
	 */
	@Deprecated
	public int BAD_REQUEST() {
		return HttpStatus.BAD_REQUEST;
	}

	/**
	 * @return {@code 401 Unauthorized}.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1:
	 * Authentication, section 3.1</a>
	 * @deprecated Replaced by {@code HttpStatus.UNAUTHORIZED}.
	 */
	@Deprecated
	public int UNAUTHORIZED() {
		return HttpStatus.UNAUTHORIZED;
	}

	/**
	 * @return {@code 402 Payment Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.2">HTTP/1.1:
	 * Semantics and Content, section 6.5.2</a>
	 * @deprecated Replaced by {@code HttpStatus.PAYMENT_REQUIRED}.
	 */
	@Deprecated
	public int PAYMENT_REQUIRED() {
		return HttpStatus.PAYMENT_REQUIRED;
	}

	/**
	 * @return {@code 403 Forbidden}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1:
	 * Semantics and Content, section 6.5.3</a>
	 * @deprecated Replaced by {@code HttpStatus.FORBIDDEN}.
	 */
	@Deprecated
	public int FORBIDDEN() {
		return HttpStatus.FORBIDDEN;
	}

	/**
	 * @return {@code 404 Not Found}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1:
	 * Semantics and Content, section 6.5.4</a>
	 * @deprecated Replaced by {@code HttpStatus.NOT_FOUND}.
	 */
	@Deprecated
	public int NOT_FOUND() {
		return HttpStatus.NOT_FOUND;
	}

	/**
	 * @return {@code 405 Method Not Allowed}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">HTTP/1.1:
	 * Semantics and Content, section 6.5.5</a>
	 * @deprecated Replaced by {@code HttpStatus.METHOD_NOT_ALLOWED}.
	 */
	@Deprecated
	public int METHOD_NOT_ALLOWED() {
		return HttpStatus.METHOD_NOT_ALLOWED;
	}

	/**
	 * @return {@code 406 Not Acceptable}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.6">HTTP/1.1:
	 * Semantics and Content, section 6.5.6</a>
	 * @deprecated Replaced by {@code HttpStatus.NOT_ACCEPTABLE}.
	 */
	@Deprecated
	public int NOT_ACCEPTABLE() {
		return HttpStatus.NOT_ACCEPTABLE;
	}

	/**
	 * @return {@code 407 Proxy Authentication Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.2">HTTP/1.1:
	 * Authentication, section 3.2</a>
	 * @deprecated Replaced by {@code HttpStatus.PROXY_AUTHENTICATION_REQUIRED}.
	 */
	@Deprecated
	public int PROXY_AUTHENTICATION_REQUIRED() {
		return HttpStatus.PROXY_AUTHENTICATION_REQUIRED;
	}

	/**
	 * @return {@code 408 Request Timeout}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.7">HTTP/1.1:
	 * Semantics and Content, section 6.5.7</a>
	 * @deprecated Replaced by {@code HttpStatus.REQUEST_TIMEOUT}.
	 */
	@Deprecated
	public int REQUEST_TIMEOUT() {
		return HttpStatus.REQUEST_TIMEOUT;
	}

	/**
	 * @return {@code 409 Conflict}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">HTTP/1.1:
	 * Semantics and Content, section 6.5.8</a>
	 * @deprecated Replaced by {@code HttpStatus.CONFLICT}.
	 */
	@Deprecated
	public int CONFLICT() {
		return HttpStatus.CONFLICT;
	}

	/**
	 * @return {@code 410 Gone}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.9">HTTP/1.1:
	 * Semantics and Content, section 6.5.9</a>
	 * @deprecated Replaced by {@code HttpStatus.GONE}.
	 */
	@Deprecated
	public int GONE() {
		return HttpStatus.GONE;
	}

	/**
	 * @return {@code 411 Length Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.10">HTTP/1.1:
	 * Semantics and Content, section 6.5.10</a>
	 * @deprecated Replaced by {@code HttpStatus.LENGTH_REQUIRED}.
	 */
	@Deprecated
	public int LENGTH_REQUIRED() {
		return HttpStatus.LENGTH_REQUIRED;
	}

	/**
	 * @return {@code 412 Precondition failed}.
	 * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.2">HTTP/1.1:
	 * Conditional Requests, section 4.2</a>
	 * @deprecated Replaced by {@code HttpStatus.PRECONDITION_FAILED}.
	 */
	@Deprecated
	public int PRECONDITION_FAILED() {
		return HttpStatus.PRECONDITION_FAILED;
	}

	/**
	 * @return {@code 413 Payload Too Large}.
	 * @since 4.1* @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.11" >
	 * HTTP/1.1: Semantics and Content, section 6.5.11</a>
	 * @deprecated Replaced by {@code HttpStatus.PAYLOAD_TOO_LARGE}.
	 */
	@Deprecated
	public int PAYLOAD_TOO_LARGE() {
		return HttpStatus.PAYLOAD_TOO_LARGE;
	}

	/**
	 * @return {@code 413 Request Entity Too Large}.
	 * @see <a href="https://tools.ietf.org/html/rfc2616#section-10.4.14">HTTP/1.1,
	 * section 10.4.14</a>
	 * @deprecated in favor of {@link #PAYLOAD_TOO_LARGE} which will be returned from
	 */
	@Deprecated
	public int REQUEST_ENTITY_TOO_LARGE() {
		return HttpStatus.REQUEST_ENTITY_TOO_LARGE;
	}

	/**
	 * @return {@code 414 URI Too Long}.
	 * @since 4.1* @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12" >
	 * HTTP/1.1: Semantics and Content, section 6.5.12</a>
	 * @deprecated Replaced by {@code HttpStatus.URI_TOO_LONG}.
	 */
	@Deprecated
	public int URI_TOO_LONG() {
		return HttpStatus.URI_TOO_LONG;
	}

	/**
	 * @return {@code 414 Request-URI Too Long}.
	 * @see <a href="https://tools.ietf.org/html/rfc2616#section-10.4.15">HTTP/1.1,
	 * section 10.4.15</a>
	 * @deprecated in favor of {@link #URI_TOO_LONG} which will be returned from
	 */
	@Deprecated
	public int REQUEST_URI_TOO_LONG() {
		return HttpStatus.REQUEST_URI_TOO_LONG;
	}

	/**
	 * @return {@code 415 Unsupported Media Type}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.13">HTTP/1.1:
	 * Semantics and Content, section 6.5.13</a>
	 * @deprecated Replaced by {@code HttpStatus.UNSUPPORTED_MEDIA_TYPE}.
	 */
	@Deprecated
	public int UNSUPPORTED_MEDIA_TYPE() {
		return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
	}

	/**
	 * @return {@code 416 Requested Range Not Satisfiable}.
	 * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.4">HTTP/1.1: Range
	 * Requests, section 4.4</a>
	 * @deprecated Replaced by {@code HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE}.
	 */
	@Deprecated
	public int REQUESTED_RANGE_NOT_SATISFIABLE() {
		return HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
	}

	/**
	 * @return {@code 417 Expectation Failed}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.14">HTTP/1.1:
	 * Semantics and Content, section 6.5.14</a>
	 * @deprecated Replaced by {@code HttpStatus.EXPECTATION_FAILED}.
	 */
	@Deprecated
	public int EXPECTATION_FAILED() {
		return HttpStatus.EXPECTATION_FAILED;
	}

	/**
	 * @return {@code 418 I'm a teapot}.
	 * @see <a href="https://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
	 * @deprecated Replaced by {@code HttpStatus.I_AM_A_TEAPOT}.
	 */
	@Deprecated
	public int I_AM_A_TEAPOT() {
		return HttpStatus.I_AM_A_TEAPOT;
	}

	/**
	 * @return {@code 419 Insufficient Space}.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public int INSUFFICIENT_SPACE_ON_RESOURCE() {
		return HttpStatus.INSUFFICIENT_SPACE_ON_RESOURCE;
	}

	/**
	 * @return {@code 420 Method Failure}.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public int METHOD_FAILURE() {
		return HttpStatus.METHOD_FAILURE;
	}

	/**
	 * @return {@code 421 Destination Locked}.
	 * @deprecated See <a href=
	 * "https://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV
	 * Draft Changes</a>
	 */
	@Deprecated
	public int DESTINATION_LOCKED() {
		return HttpStatus.DESTINATION_LOCKED;
	}

	/**
	 * @return {@code 422 Unprocessable Entity}.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
	 * @deprecated Replaced by {@code HttpStatus.UNPROCESSABLE_ENTITY}.
	 */
	@Deprecated
	public int UNPROCESSABLE_ENTITY() {
		return HttpStatus.UNPROCESSABLE_ENTITY;
	}

	/**
	 * @return {@code 423 Locked}.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
	 * @deprecated Replaced by {@code HttpStatus.LOCKED}.
	 */
	@Deprecated
	public int LOCKED() {
		return HttpStatus.LOCKED;
	}

	/**
	 * @return {@code 424 Failed Dependency}.
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
	 * @deprecated Replaced by {@code HttpStatus.FAILED_DEPENDENCY}.
	 */
	@Deprecated
	public int FAILED_DEPENDENCY() {
		return HttpStatus.FAILED_DEPENDENCY;
	}

	/**
	 * @return {@code 426 Upgrade Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS
	 * Within HTTP/1.1</a>
	 * @deprecated Replaced by {@code HttpStatus.UPGRADE_REQUIRED}.
	 */
	@Deprecated
	public int UPGRADE_REQUIRED() {
		return HttpStatus.UPGRADE_REQUIRED;
	}

	/**
	 * @return {@code 428 Precondition Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status
	 * Codes</a>
	 * @deprecated Replaced by {@code HttpStatus.PRECONDITION_REQUIRED}.
	 */
	@Deprecated
	public int PRECONDITION_REQUIRED() {
		return HttpStatus.PRECONDITION_REQUIRED;
	}

	/**
	 * @return {@code 429 Too Many Requests}.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status
	 * Codes</a>
	 * @deprecated Replaced by {@code HttpStatus.TOO_MANY_REQUESTS}.
	 */
	@Deprecated
	public int TOO_MANY_REQUESTS() {
		return HttpStatus.TOO_MANY_REQUESTS;
	}

	/**
	 * @return {@code 431 Request Header Fields Too Large}.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status
	 * Codes</a>
	 * @deprecated Replaced by {@code HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE}.
	 */
	@Deprecated
	public int REQUEST_HEADER_FIELDS_TOO_LARGE() {
		return HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE;
	}

	/**
	 * @return {@code 451 Unavailable For Legal Reasons}.
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-httpbis-legally-restricted-status-04" > An
	 * HTTP Status Code to Report Legal Obstacles</a>
	 * @deprecated Replaced by {@code HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS}.
	 */
	@Deprecated
	public int UNAVAILABLE_FOR_LEGAL_REASONS() {
		return HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS;
	}

	/**
	 * @return {@code 500 Internal Server Error}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">HTTP/1.1:
	 * Semantics and Content, section 6.6.1</a>
	 * @deprecated Replaced by {@code HttpStatus.INTERNAL_SERVER_ERROR}.
	 */
	@Deprecated
	public int INTERNAL_SERVER_ERROR() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/**
	 * @return {@code 501 Not Implemented}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">HTTP/1.1:
	 * Semantics and Content, section 6.6.2</a>
	 * @deprecated Replaced by {@code HttpStatus.NOT_IMPLEMENTED}.
	 */
	@Deprecated
	public int NOT_IMPLEMENTED() {
		return HttpStatus.NOT_IMPLEMENTED;
	}

	/**
	 * @return {@code 502 Bad Gateway}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">HTTP/1.1:
	 * Semantics and Content, section 6.6.3</a>
	 * @deprecated Replaced by {@code HttpStatus.BAD_GATEWAY}.
	 */
	@Deprecated
	public int BAD_GATEWAY() {
		return HttpStatus.BAD_GATEWAY;
	}

	/**
	 * @return {@code 503 Service Unavailable}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.4">HTTP/1.1:
	 * Semantics and Content, section 6.6.4</a>
	 * @deprecated Replaced by {@code HttpStatus.SERVICE_UNAVAILABLE}.
	 */
	@Deprecated
	public int SERVICE_UNAVAILABLE() {
		return HttpStatus.SERVICE_UNAVAILABLE;
	}

	/**
	 * @return {@code 504 Gateway Timeout}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.5">HTTP/1.1:
	 * Semantics and Content, section 6.6.5</a>
	 * @deprecated Replaced by {@code HttpStatus.GATEWAY_TIMEOUT}.
	 */
	@Deprecated
	public int GATEWAY_TIMEOUT() {
		return HttpStatus.GATEWAY_TIMEOUT;
	}

	/**
	 * @return {@code 505 HTTP Version Not Supported}.
	 * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.6">HTTP/1.1:
	 * Semantics and Content, section 6.6.6</a>
	 * @deprecated Replaced by {@code HttpStatus.HTTP_VERSION_NOT_SUPPORTED}.
	 */
	@Deprecated
	public int HTTP_VERSION_NOT_SUPPORTED() {
		return HttpStatus.HTTP_VERSION_NOT_SUPPORTED;
	}

	/**
	 * @return {@code 506 Variant Also Negotiates}
	 * @see <a href="https://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content
	 * Negotiation</a>
	 * @deprecated Replaced by {@code HttpStatus.VARIANT_ALSO_NEGOTIATES}.
	 */
	@Deprecated
	public int VARIANT_ALSO_NEGOTIATES() {
		return HttpStatus.VARIANT_ALSO_NEGOTIATES;
	}

	/**
	 * @return {@code 507 Insufficient Storage}
	 * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
	 * @deprecated Replaced by {@code HttpStatus.INSUFFICIENT_STORAGE}.
	 */
	@Deprecated
	public int INSUFFICIENT_STORAGE() {
		return HttpStatus.INSUFFICIENT_STORAGE;
	}

	/**
	 * @return {@code 508 Loop Detected}
	 * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding
	 * Extensions</a>
	 * @deprecated Replaced by {@code HttpStatus.LOOP_DETECTED}.
	 */
	@Deprecated
	public int LOOP_DETECTED() {
		return HttpStatus.LOOP_DETECTED;
	}

	/**
	 * @return {@code 509 Bandwidth Limit Exceeded}
	 * @deprecated Replaced by {@code HttpStatus.BANDWIDTH_LIMIT_EXCEEDED}.
	 */
	@Deprecated
	public int BANDWIDTH_LIMIT_EXCEEDED() {
		return HttpStatus.BANDWIDTH_LIMIT_EXCEEDED;
	}

	/**
	 * @return {@code 510 Not Extended}
	 * @see <a href="https://tools.ietf.org/html/rfc2774#section-7">HTTP Extension
	 * Framework</a>
	 * @deprecated Replaced by {@code HttpStatus.NOT_EXTENDED}.
	 */
	@Deprecated
	public int NOT_EXTENDED() {
		return HttpStatus.NOT_EXTENDED;
	}

	/**
	 * @return {@code 511 Network Authentication Required}.
	 * @see <a href="https://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status
	 * Codes</a>
	 * @deprecated Replaced by {@code HttpStatus.NETWORK_AUTHENTICATION_REQUIRED}.
	 */
	@Deprecated
	public int NETWORK_AUTHENTICATION_REQUIRED() {
		return HttpStatus.NETWORK_AUTHENTICATION_REQUIRED;
	}

}
