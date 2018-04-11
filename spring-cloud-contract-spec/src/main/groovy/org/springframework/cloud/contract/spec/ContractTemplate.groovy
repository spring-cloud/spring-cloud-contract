package org.springframework.cloud.contract.spec

/**
 * Contract for defining templated responses.
 *
 * If no implementation is provided then Handlebars will be picked as a default implementation.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
interface ContractTemplate {

	/**
	 * Handlebars is using the Mustache template thus it looks like this
	 *  {{{ Mustache }}}. In this case the opening template would return {{{
	 */
	String openingTemplate()

	/**
	 * Handlebars is using the Mustache template thus it looks like this
	 *  {{{ Mustache }}}. In this case the closing template would return }}}
	 */
	String closingTemplate()

	/**
	 * Returns the template for retrieving a URL path and query from request
	 */
	String url()

	/**
	 * Returns the template for retrieving first value of a query parameter e.g. {{{ request.query.search }}}
	 * @param key
	 */
	String query(String key)

	/**
	 * Returns the template for retrieving nth value of a query parameter (zero indexed) e.g. {{{ request.query.search.[5] }}}
	 * @param key
	 * @param index
	 */
	String query(String key, int index)

	/**
	 * Returns the template for retrieving a URL path
	 */
	String path()

	/**
	 * Returns the template for retrieving nth value of a URL path (zero indexed) e.g. {{{ request.path.[2] }}}
	 * @param index
	 */
	String path(int index)

	/**
	 * Returns the template for retrieving the first value of a request header e.g. {{{ request.headers.X-Request-Id }}}
	 * @param key
	 */
	String header(String key)

	/**
	 * Returns the template for retrieving the nth value of a request header (zero indexed) e.g. {{{ request.headers.X-Request-Id.[5] }}}
	 * @param key
	 * @param index
	 */
	String header(String key, int index)

	/**
	 * Retruns the tempalte for retrieving the first value of a cookie with certain key
	 * @param key
	 */
	String cookie(String key)

	/**
	 * Request body text (avoid for non-text bodies) e.g. {{{ request.body }}} . The body will not be escaped
	 * so you won't be able to directly embed it in a JSON for example.
	 */
	String body()

	/**
	 * Request body text (avoid for non-text bodies) e.g. {{{ escapejsonbody }}} . The body will not be escaped
	 * so you will be able to embed it
	 */
	String escapedBody()

	/**
	 * Request body text for the given JsonPath. e.g. {{{ jsonpath this '$.a.b.c' }}}
	 */
	String body(String jsonPath)
}