package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.ContractTemplate
/**
 * Helper class to reference the request body parameters
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */

@CompileStatic
class FromRequest {

	private final ContractTemplate template

	FromRequest() {
		this.template = template()
	}

	private ContractTemplate template() {
		return new HandlebarsContractTemplate()
	}

	/**
	 * URL path and query
	 */
	DslProperty url() {
		return new DslProperty(template.url())
	}

	/**
	 * First value of a query parameter e.g. request.query.search
	 * @param key
	 */
	DslProperty query(String key) {
		return new DslProperty(template.query(key))
	}

	/**
	 * nth value of a query parameter (zero indexed) e.g. request.query.search.[5]
	 * @param key
	 * @param index
	 */
	DslProperty query(String key, int index) {
		return new DslProperty(template.query(key, index))
	}

	/**
	 * First value of a request header e.g. request.headers.X-Request-Id
	 * @param key
	 */
	DslProperty header(String key) {
		return new DslProperty(template.header(key))
	}

	/**
	 * nth value of a request header (zero indexed) e.g. request.headers.X-Request-Id
	 * @param key
	 */
	DslProperty header(String key, int index) {
		return new DslProperty(template.header(key, index))
	}

	/**
	 * Request body text (avoid for non-text bodies)
	 */
	DslProperty body() {
		return new DslProperty(template.body())
	}

	/**
	 * Request body text for the given JsonPath
	 */
	DslProperty body(String jsonPath) {
		return new DslProperty(template.body(jsonPath))
	}

}


