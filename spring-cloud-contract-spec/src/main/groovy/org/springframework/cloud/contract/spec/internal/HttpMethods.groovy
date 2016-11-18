package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Contains Http Methods
 *
 * @author Marcin Grzejszczak
 * @since 1.0.2
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class HttpMethods {

	HttpMethod GET() {
		return HttpMethod.GET
	}

	HttpMethod HEAD() {
		return HttpMethod.HEAD
	}

	HttpMethod POST() {
		return HttpMethod.POST
	}

	HttpMethod PUT() {
		return HttpMethod.PUT
	}

	HttpMethod PATCH() {
		return HttpMethod.PATCH
	}

	HttpMethod DELETE() {
		return HttpMethod.DELETE
	}

	HttpMethod OPTIONS() {
		return HttpMethod.OPTIONS
	}

	HttpMethod TRACE() {
		return HttpMethod.TRACE
	}

	enum HttpMethod {
		GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
	}
}
