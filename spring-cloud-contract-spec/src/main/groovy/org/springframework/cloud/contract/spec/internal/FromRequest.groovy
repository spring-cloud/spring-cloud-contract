package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic

/**
 * Helper class to reference the request body parameters
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class FromRequest {

	/**
	 * URL path and query
	 */
	DslProperty url() {
		return new DslProperty("{{request.url}}".toString())
	}

	/**
	 * First value of a query parameter e.g. request.query.search
	 * @param key
	 */
	DslProperty query(String key) {
		return new DslProperty("{{request.query.${key}}}".toString())
	}

	/**
	 * nth value of a query parameter (zero indexed) e.g. request.query.search.[5]
	 * @param key
	 * @param index
	 */
	DslProperty query(String key, int index) {
		return new DslProperty("{{request.query.${key}.[${index}]}}".toString())
	}

	/**
	 * First value of a request header e.g. request.headers.X-Request-Id
	 * @param key
	 */
	DslProperty headers(String key) {
		return new DslProperty("{{request.headers.${key}}}".toString())
	}

	/**
	 * nth value of a header (zero indexed) e.g. request.headers.ManyThings.[1]
	 * @param key
	 * @param index
	 */
	DslProperty headers(String key, int index) {
		return new DslProperty("{{request.headers.${key}.[${index}]}}".toString())
	}

	/**
	 * Request body text (avoid for non-text bodies)
	 */
	DslProperty body() {
		return new DslProperty("{{request.body}}".toString())
	}
}
