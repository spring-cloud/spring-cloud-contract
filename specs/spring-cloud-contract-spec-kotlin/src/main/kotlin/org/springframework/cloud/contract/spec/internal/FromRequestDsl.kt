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

package org.springframework.cloud.contract.spec.internal

import org.springframework.cloud.contract.spec.ContractTemplate

/**
 * Helper class to reference the request body parameters.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
class FromRequestDsl {

    private val template: ContractTemplate = HandlebarsContractTemplate()

    /**
     * @return URL path and query.
     */
    fun url() = FromRequestDslProperty(template.escapedUrl())

    /**
     * First value of a query parameter e.g. request.query.search.
     * @param key key for the query param
     * @return dsl property
     */
    fun query(key: String) = FromRequestDslProperty(template.escapedQuery(key))

    /**
     * nth value of a query parameter (zero indexed) e.g. request.query.search.[5].
     * @param key key for the query param
     * @param index index of the query param
     * @return dsl property
     */
    fun query(key: String, index: Int) = FromRequestDslProperty(template.escapedQuery(key, index))

    /**
     * URL path.
     * @return dsl property
     */
    fun path() = FromRequestDslProperty(template.escapedPath())

    /**
     * nth value of a URL path (zero indexed) e.g. {{{ request.path.[2] }}}.
     * @param index path index
     * @return dsl property
     */
    fun path(index: Int) = FromRequestDslProperty(template.escapedPath(index))

    /**
     * First value of a request header e.g. request.headers.X-Request-Id.
     * @param key header key
     * @return dsl property
     */
    fun header(key: String) = FromRequestDslProperty(template.escapedHeader(key))

    /**
     * nth value of a request header (zero indexed) e.g. request.headers.X-Request-Id.
     * @param key header key
     * @param index header index
     * @return dsl property
     */
    fun header(key: String, index: Int) = FromRequestDslProperty(template.escapedHeader(key, index))

    /**
     * Returns the template for retrieving the first value of a cookie with certain key.
     * @param key cookie key
     * @return dsl property
     */
    fun cookie(key: String) = FromRequestDslProperty(template.escapedCookie(key))

    /**
     * Request body text (avoid for non-text bodies).
     * @return dsl property
     */
    fun body() = FromRequestDslProperty(template.escapedBody())

    /**
     * Request body text for the given JsonPath.
     * @param jsonPath json path body
     * @return dsl property
     */
    fun body(jsonPath: String) = FromRequestDslProperty(template.escapedBody(jsonPath))

    /**
     * Unescaped URL path and query.
     * @return dsl property
     */
    fun rawUrl() = FromRequestDslProperty(template.url())

    /**
     * Unescaped first value of a query parameter e.g. request.query.search.
     * @param key query key
     * @return dsl property
     */
    fun rawQuery(key: String) = FromRequestDslProperty(template.query(key))

    /**
     * Unescaped nth value of a query parameter (zero indexed) e.g.
     * request.query.search.[5].
     * @param key query key
     * @param index query index
     * @return dsl property
     */
    fun rawQuery(key: String, index: Int) = FromRequestDslProperty(template.query(key, index))

    /**
     * Unescaped URL path.
     * @return dsl property
     */
    fun rawPath() = FromRequestDslProperty(template.path())

    /**
     * Unescaped nth value of a URL path (zero indexed) e.g. {{{ request.path.[2]. }}}.
     * @param index path index
     * @return dsl property
     */
    fun rawPath(index: Int) = FromRequestDslProperty(template.path(index))

    /**
     * Unescaped first value of a request header e.g. request.headers.X-Request-Id.
     * @param key header key
     * @return dsl property
     */
    fun rawHeader(key: String) = FromRequestDslProperty(template.header(key))

    /**
     * Unescaped nth value of a request header (zero indexed) e.g.
     * request.headers.X-Request-Id.
     * @param key header key
     * @param index header index
     * @return dsl property
     */
    fun rawHeader(key: String, index: Int) = FromRequestDslProperty(template.header(key, index))

    /**
     * Unescaped Returns the template for retrieving the first value of a cookie with
     * certain key.
     * @param key cookie key
     * @return dsl property
     */
    fun rawCookie(key: String) = FromRequestDslProperty(template.cookie(key))

    /**
     * Unescaped Request body text (avoid for non-text bodies).
     * @return dsl property
     */
    fun rawBody() = FromRequestDslProperty(template.body())

    /**
     * Unescaped Request body text for the given JsonPath.
     * @param jsonPath json path body
     * @return dsl property
     */
    fun rawBody(jsonPath: String) = FromRequestDslProperty(template.body(jsonPath))

}

class FromRequestDslProperty(private val content: String) : DslProperty<String>(content) {
    // Overridden to support String interpolation
    override fun toString(): String = content
}