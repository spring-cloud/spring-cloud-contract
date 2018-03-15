package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

/**
 * Helper functions for HTTP statuses
 * 
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
@CompileStatic
@EqualsAndHashCode
class HttpStatus {

	private HttpStatus() {}

	// 1xx Informational

	/**
	 * {@code 100 Continue}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.2.1">HTTP/1.1: Semantics and Content, section 6.2.1</a>
	 */
	int CONTINUE() {
		return 100
	}
	
	/**
	 * {@code 101 Switching Protocols}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.2.2">HTTP/1.1: Semantics and Content, section 6.2.2</a>
	 */
	int SWITCHING_PROTOCOLS() {
		return 101
	}
	/**
	 * {@code 102 Processing}.
	 * @see <a href="http://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
	 */
	int PROCESSING() { return 102 }
	/**
	 * {@code 103 Checkpoint}.
	 * @see <a href="http://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
	 * resumable POST/PUT HTTP requests in HTTP/1.0</a>
	 */
	int CHECKPOINT() { return 103 }

	// 2xx Success

	/**
	 * {@code 200 OK}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.3.1">HTTP/1.1: Semantics and Content, section 6.3.1</a>
	 */
	int OK() { return 200 }
	/**
	 * {@code 201 Created}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.3.2">HTTP/1.1: Semantics and Content, section 6.3.2</a>
	 */
	int CREATED() { return 201 }
	/**
	 * {@code 202 Accepted}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.3.3">HTTP/1.1: Semantics and Content, section 6.3.3</a>
	 */
	int ACCEPTED() { return 202 }
	/**
	 * {@code 203 Non-Authoritative Information}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.3.4">HTTP/1.1: Semantics and Content, section 6.3.4</a>
	 */
	int NON_AUTHORITATIVE_INFORMATION() { return 203 }
	/**
	 * {@code 204 No Content}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.3.5">HTTP/1.1: Semantics and Content, section 6.3.5</a>
	 */
	int NO_CONTENT() { return 204 }
	/**
	 * {@code 205 Reset Content}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.3.6">HTTP/1.1: Semantics and Content, section 6.3.6</a>
	 */
	int RESET_CONTENT() { return 205 }
	/**
	 * {@code 206 Partial Content}.
	 * @see <a href="http://tools.ietf.org/html/rfc7233#section-4.1">HTTP/1.1: Range Requests, section 4.1</a>
	 */
	int PARTIAL_CONTENT() { return 206 }
	/**
	 * {@code 207 Multi-Status}.
	 * @see <a href="http://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
	 */
	int MULTI_STATUS() { return 207 }
	/**
	 * {@code 208 Already Reported}.
	 * @see <a href="http://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding Extensions</a>
	 */
	int ALREADY_REPORTED() { return 208 }
	/**
	 * {@code 226 IM Used}.
	 * @see <a href="http://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
	 */
	int IM_USED() { return 226 }

	// 3xx Redirection

	/**
	 * {@code 300 Multiple Choices}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.4.1">HTTP/1.1: Semantics and Content, section 6.4.1</a>
	 */
	int MULTIPLE_CHOICES() { return 300 }
	/**
	 * {@code 301 Moved Permanently}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.4.2">HTTP/1.1: Semantics and Content, section 6.4.2</a>
	 */
	int MOVED_PERMANENTLY() { return 301 }
	/**
	 * {@code 302 Found}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.4.3">HTTP/1.1: Semantics and Content, section 6.4.3</a>
	 */
	int FOUND() { return 302 }
	/**
	 * {@code 302 Moved Temporarily}.
	 * @see <a href="http://tools.ietf.org/html/rfc1945#section-9.3">HTTP/1.0, section 9.3</a>
	 * @deprecated in favor of {@link #FOUND} which will be returned from {@code HttpStatus.valueOf(302)}
	 */
	@Deprecated
	int MOVED_TEMPORARILY() { return 302 }
	/**
	 * {@code 303 See Other}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.4.4">HTTP/1.1: Semantics and Content, section 6.4.4</a>
	 */
	int SEE_OTHER() { return 303 }
	/**
	 * {@code 304 Not Modified}.
	 * @see <a href="http://tools.ietf.org/html/rfc7232#section-4.1">HTTP/1.1: Conditional Requests, section 4.1</a>
	 */
	int NOT_MODIFIED() { return 304 }
	/**
	 * {@code 305 Use Proxy}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.4.5">HTTP/1.1: Semantics and Content, section 6.4.5</a>
	 * @deprecated due to security concerns regarding in-band configuration of a proxy
	 */
	@Deprecated
	int USE_PROXY() { return 305 }
	/**
	 * {@code 307 Temporary Redirect}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.4.7">HTTP/1.1: Semantics and Content, section 6.4.7</a>
	 */
	int TEMPORARY_REDIRECT() { return 307 }
	/**
	 * {@code 308 Permanent Redirect}.
	 * @see <a href="http://tools.ietf.org/html/rfc7238">RFC 7238</a>
	 */
	int PERMANENT_REDIRECT() { return 308 }

	// --- 4xx Client Error ---

	/**
	 * {@code 400 Bad Request}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.1">HTTP/1.1: Semantics and Content, section 6.5.1</a>
	 */
	int BAD_REQUEST() { return 400 }
	/**
	 * {@code 401 Unauthorized}.
	 * @see <a href="http://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1: Authentication, section 3.1</a>
	 */
	int UNAUTHORIZED() { return 401 }
	/**
	 * {@code 402 Payment Required}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.2">HTTP/1.1: Semantics and Content, section 6.5.2</a>
	 */
	int PAYMENT_REQUIRED() { return 402 }
	/**
	 * {@code 403 Forbidden}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1: Semantics and Content, section 6.5.3</a>
	 */
	int FORBIDDEN() { return 403 }
	/**
	 * {@code 404 Not Found}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1: Semantics and Content, section 6.5.4</a>
	 */
	int NOT_FOUND() { return 404 }
	/**
	 * {@code 405 Method Not Allowed}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.5">HTTP/1.1: Semantics and Content, section 6.5.5</a>
	 */
	int METHOD_NOT_ALLOWED() { return 405 }
	/**
	 * {@code 406 Not Acceptable}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.6">HTTP/1.1: Semantics and Content, section 6.5.6</a>
	 */
	int NOT_ACCEPTABLE() { return 406 }
	/**
	 * {@code 407 Proxy Authentication Required}.
	 * @see <a href="http://tools.ietf.org/html/rfc7235#section-3.2">HTTP/1.1: Authentication, section 3.2</a>
	 */
	int PROXY_AUTHENTICATION_REQUIRED() { return 407 }
	/**
	 * {@code 408 Request Timeout}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.7">HTTP/1.1: Semantics and Content, section 6.5.7</a>
	 */
	int REQUEST_TIMEOUT() { return 408 }
	/**
	 * {@code 409 Conflict}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.8">HTTP/1.1: Semantics and Content, section 6.5.8</a>
	 */
	int CONFLICT() { return 409 }
	/**
	 * {@code 410 Gone}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.9">HTTP/1.1: Semantics and Content, section 6.5.9</a>
	 */
	int GONE() { return 410 }
	/**
	 * {@code 411 Length Required}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.10">HTTP/1.1: Semantics and Content, section 6.5.10</a>
	 */
	int LENGTH_REQUIRED() { return 411 }
	/**
	 * {@code 412 Precondition failed}.
	 * @see <a href="http://tools.ietf.org/html/rfc7232#section-4.2">HTTP/1.1: Conditional Requests, section 4.2</a>
	 */
	int PRECONDITION_FAILED() { return 412 }
	/**
	 * {@code 413 Payload Too Large}.
	 * @since 4.1
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.11">HTTP/1.1: Semantics and Content, section 6.5.11</a>
	 */
	int PAYLOAD_TOO_LARGE() { return 413 }
	/**
	 * {@code 413 Request Entity Too Large}.
	 * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.14">HTTP/1.1, section 10.4.14</a>
	 * @deprecated in favor of {@link #PAYLOAD_TOO_LARGE} which will be returned from {@code HttpStatus.valueOf(413)}
	 */
	@Deprecated
	int REQUEST_ENTITY_TOO_LARGE() { return 413 }
	/**
	 * {@code 414 URI Too Long}.
	 * @since 4.1
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.12">HTTP/1.1: Semantics and Content, section 6.5.12</a>
	 */
	int URI_TOO_LONG() { return 414 }
	/**
	 * {@code 414 Request-URI Too Long}.
	 * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.15">HTTP/1.1, section 10.4.15</a>
	 * @deprecated in favor of {@link #URI_TOO_LONG} which will be returned from {@code HttpStatus.valueOf(414)}
	 */
	@Deprecated
	int REQUEST_URI_TOO_LONG() { return 414 }
	/**
	 * {@code 415 Unsupported Media Type}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.13">HTTP/1.1: Semantics and Content, section 6.5.13</a>
	 */
	int UNSUPPORTED_MEDIA_TYPE() { return 415 }
	/**
	 * {@code 416 Requested Range Not Satisfiable}.
	 * @see <a href="http://tools.ietf.org/html/rfc7233#section-4.4">HTTP/1.1: Range Requests, section 4.4</a>
	 */
	int REQUESTED_RANGE_NOT_SATISFIABLE() { return 416 }
	/**
	 * {@code 417 Expectation Failed}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.14">HTTP/1.1: Semantics and Content, section 6.5.14</a>
	 */
	int EXPECTATION_FAILED() { return 417 }
	/**
	 * {@code 418 I'm a teapot}.
	 * @see <a href="http://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
	 */
	int I_AM_A_TEAPOT() { return 418 }
	/**
	 * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
	 */
	@Deprecated
	int INSUFFICIENT_SPACE_ON_RESOURCE() { return 419 }
	/**
	 * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
	 */
	@Deprecated
	int METHOD_FAILURE() { return 420 }
	/**
	 * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
	 */
	@Deprecated
	int DESTINATION_LOCKED() { return 421 }
	/**
	 * {@code 422 Unprocessable Entity}.
	 * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
	 */
	int UNPROCESSABLE_ENTITY() { return 422 }
	/**
	 * {@code 423 Locked}.
	 * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
	 */
	int LOCKED() { return 423 }
	/**
	 * {@code 424 Failed Dependency}.
	 * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
	 */
	int FAILED_DEPENDENCY() { return 424 }
	/**
	 * {@code 426 Upgrade Required}.
	 * @see <a href="http://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS Within HTTP/1.1</a>
	 */
	int UPGRADE_REQUIRED() { return 426 }
	/**
	 * {@code 428 Precondition Required}.
	 * @see <a href="http://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status Codes</a>
	 */
	int PRECONDITION_REQUIRED() { return 428 }
	/**
	 * {@code 429 Too Many Requests}.
	 * @see <a href="http://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status Codes</a>
	 */
	int TOO_MANY_REQUESTS() { return 429 }
	/**
	 * {@code 431 Request Header Fields Too Large}.
	 * @see <a href="http://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status Codes</a>
	 */
	int REQUEST_HEADER_FIELDS_TOO_LARGE() { return 431 }
	/**
	 * {@code 451 Unavailable For Legal Reasons}.
	 * @see <a href="https://tools.ietf.org/html/draft-ietf-httpbis-legally-restricted-status-04">
	 * An HTTP Status Code to Report Legal Obstacles</a>
	 * @since 4.3
	 */
	int UNAVAILABLE_FOR_LEGAL_REASONS() { return 451 }

	// --- 5xx Server Error ---

	/**
	 * {@code 500 Internal Server Error}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.6.1">HTTP/1.1: Semantics and Content, section 6.6.1</a>
	 */
	int INTERNAL_SERVER_ERROR() { return 500 }
	/**
	 * {@code 501 Not Implemented}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.6.2">HTTP/1.1: Semantics and Content, section 6.6.2</a>
	 */
	int NOT_IMPLEMENTED() { return 501 }
	/**
	 * {@code 502 Bad Gateway}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.6.3">HTTP/1.1: Semantics and Content, section 6.6.3</a>
	 */
	int BAD_GATEWAY() { return 502 }
	/**
	 * {@code 503 Service Unavailable}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.6.4">HTTP/1.1: Semantics and Content, section 6.6.4</a>
	 */
	int SERVICE_UNAVAILABLE() { return 503 }
	/**
	 * {@code 504 Gateway Timeout}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.6.5">HTTP/1.1: Semantics and Content, section 6.6.5</a>
	 */
	int GATEWAY_TIMEOUT() { return 504 }
	/**
	 * {@code 505 HTTP Version Not Supported}.
	 * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.6.6">HTTP/1.1: Semantics and Content, section 6.6.6</a>
	 */
	int HTTP_VERSION_NOT_SUPPORTED() { return 505 }
	/**
	 * {@code 506 Variant Also Negotiates}
	 * @see <a href="http://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content Negotiation</a>
	 */
	int VARIANT_ALSO_NEGOTIATES() { return 506 }
	/**
	 * {@code 507 Insufficient Storage}
	 * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
	 */
	int INSUFFICIENT_STORAGE() { return 507 }
	/**
	 * {@code 508 Loop Detected}
	 * @see <a href="http://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding Extensions</a>
	 */
	int LOOP_DETECTED() { return 508 }
	/**
	 * {@code 509 Bandwidth Limit Exceeded}
	 */
	int BANDWIDTH_LIMIT_EXCEEDED() { return 509 }
	/**
	 * {@code 510 Not Extended}
	 * @see <a href="http://tools.ietf.org/html/rfc2774#section-7">HTTP Extension Framework</a>
	 */
	int NOT_EXTENDED() { return 510 }
	/**
	 * {@code 511 Network Authentication Required}.
	 * @see <a href="http://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status Codes</a>
	 */
	int NETWORK_AUTHENTICATION_REQUIRED() { return 511 }
}
