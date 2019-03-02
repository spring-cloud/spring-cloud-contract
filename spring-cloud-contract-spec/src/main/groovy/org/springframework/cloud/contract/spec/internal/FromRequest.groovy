/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		return new CompositeContractTemplate()
	}

	/**
	 * URL path and query
	 */
	DslProperty url() {
		return new DslProperty(template.escapedUrl())
	}

	/**
	 * First value of a query parameter e.g. request.query.search
	 * @param key
	 */
	DslProperty query(String key) {
		return new DslProperty(template.escapedQuery(key))
	}

	/**
	 * nth value of a query parameter (zero indexed) e.g. request.query.search.[5]
	 * @param key
	 * @param index
	 */
	DslProperty query(String key, int index) {
		return new DslProperty(template.escapedQuery(key, index))
	}

	/**
	 * URL path
	 */
	DslProperty path() {
		return new DslProperty(template.escapedPath())
	}

	/**
	 * nth value of a URL path (zero indexed) e.g. {{{ request.path.[2] }}}* @param index
	 */
	DslProperty path(int index) {
		return new DslProperty(template.escapedPath(index))
	}

	/**
	 * First value of a request header e.g. request.headers.X-Request-Id
	 * @param key
	 */
	DslProperty header(String key) {
		return new DslProperty(template.escapedHeader(key))
	}

	/**
	 * nth value of a request header (zero indexed) e.g. request.headers.X-Request-Id
	 * @param key
	 */
	DslProperty header(String key, int index) {
		return new DslProperty(template.escapedHeader(key, index))
	}

	/**
	 * Retruns the tempalte for retrieving the first value of a cookie with certain key
	 * @param key
	 */
	DslProperty cookie(String key) {
		return new DslProperty(template.escapedCookie(key))
	}

	/**
	 * Request body text (avoid for non-text bodies)
	 */
	DslProperty body() {
		return new DslProperty(template.escapedBody())
	}

	/**
	 * Request body text for the given JsonPath
	 */
	DslProperty body(String jsonPath) {
		return new DslProperty(template.escapedBody(jsonPath))
	}

	/**
	 * Unescaped URL path and query
	 */
	DslProperty rawUrl() {
		return new DslProperty(template.url())
	}

	/**
	 * Unescaped First value of a query parameter e.g. request.query.search
	 * @param key
	 */
	DslProperty rawQuery(String key) {
		return new DslProperty(template.query(key))
	}

	/**
	 * Unescaped nth value of a query parameter (zero indexed) e.g. request.query.search.[5]
	 * @param key
	 * @param index
	 */
	DslProperty rawQuery(String key, int index) {
		return new DslProperty(template.query(key, index))
	}

	/**
	 * Unescaped URL path
	 */
	DslProperty rawPath() {
		return new DslProperty(template.path())
	}

	/**
	 * Unescaped nth value of a URL path (zero indexed) e.g. {{{ request.path.[2] }}}* @param index
	 */
	DslProperty rawPath(int index) {
		return new DslProperty(template.path(index))
	}

	/**
	 * Unescaped First value of a request header e.g. request.headers.X-Request-Id
	 * @param key
	 */
	DslProperty rawHeader(String key) {
		return new DslProperty(template.header(key))
	}

	/**
	 * Unescaped nth value of a request header (zero indexed) e.g. request.headers.X-Request-Id
	 * @param key
	 */
	DslProperty rawHeader(String key, int index) {
		return new DslProperty(template.header(key, index))
	}

	/**
	 * Unescaped Retruns the tempalte for retrieving the first value of a cookie with certain key
	 * @param key
	 */
	DslProperty rawCookie(String key) {
		return new DslProperty(template.cookie(key))
	}

	/**
	 * Unescaped Request body text (avoid for non-text bodies)
	 */
	DslProperty rawBody() {
		return new DslProperty(template.body())
	}

	/**
	 * Unescaped Request body text for the given JsonPath
	 */
	DslProperty rawBody(String jsonPath) {
		return new DslProperty(template.body(jsonPath))
	}

}


