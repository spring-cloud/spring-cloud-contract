package org.springframework.cloud.contract.spec;

/**
 * Contract for defining templated responses.
 * <p>
 * If no implementation is provided then Handlebars will be picked as a default implementation.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public interface ContractTemplate {
	/**
	 * Asserts whether the given text starts with proper opening template
	 */
	default boolean startsWithTemplate(String text) {
		return text.startsWith(openingTemplate());
	}

	/**
	 * Asserts whether the given text starts with proper opening template for escaped value
	 */
	default boolean startsWithEscapedTemplate(String text) {
		return text.startsWith(escapedOpeningTemplate());
	}

	/**
	 * Handlebars is using the Mustache template thus it looks like this
	 * {{ Mustache }}. In this case the opening template would return {{
	 */
	String openingTemplate();

	/**
	 * Handlebars is using the Mustache template thus it looks like this
	 * {{ Mustache }}. In this case the closing template would return }}
	 */
	String closingTemplate();

	/**
	 * Handlebars is using the Mustache template thus it looks like this
	 * {{{ Mustache }}}. In this case the opening template would return {{{
	 */
	default String escapedOpeningTemplate() {
		return openingTemplate();
	}

	/**
	 * Handlebars is using the Mustache template thus it looks like this
	 * {{{ Mustache }}}. In this case the closing template would return }}}
	 */
	default String escapedClosingTemplate() {
		return closingTemplate();
	}

	/**
	 * Returns the template for retrieving a URL path and query from request
	 */
	String url();

	/**
	 * Returns the template for retrieving first value of a query parameter e.g. {{ request.query.search }}
	 *
	 * @param key
	 */
	String query(String key);

	/**
	 * Returns the template for retrieving nth value of a query parameter (zero indexed) e.g. {{ request.query.search.[5] }}
	 *
	 * @param key
	 * @param index
	 */
	String query(String key, int index);

	/**
	 * Returns the template for retrieving a URL path
	 */
	String path();

	/**
	 * Returns the template for retrieving nth value of a URL path (zero indexed) e.g. {{ request.path.[2] }}
	 *
	 * @param index
	 */
	String path(int index);

	/**
	 * Returns the template for retrieving the first value of a request header e.g. {{ request.headers.X-Request-Id }}
	 *
	 * @param key
	 */
	String header(String key);

	/**
	 * Returns the template for retrieving the nth value of a request header (zero indexed) e.g. {{ request.headers.X-Request-Id.[5] }}
	 *
	 * @param key
	 * @param index
	 */
	String header(String key, int index);

	/**
	 * Retruns the template for retrieving the first value of a cookie with certain key
	 *
	 * @param key
	 */
	String cookie(String key);

	/**
	 * Request body text (avoid for non-text bodies) e.g. {{ request.body }} . The body will not be escaped
	 * so you won't be able to directly embed it in a JSON for example.
	 */
	String body();

	/**
	 * Request body text for the given JsonPath. e.g. {{ jsonPath request.body '$.a.b.c' }}
	 */
	String body(String jsonPath);

	/**
	 * Returns the template for retrieving a escaped URL path and query from request
	 */
	default String escapedUrl() {
		return url();
	}

	/**
	 * Returns the template for retrieving escaped first value of a query parameter e.g. {{{ request.query.search }}}
	 *
	 * @param key
	 */
	default String escapedQuery(String key) {
		return query(key);
	}

	/**
	 * Returns the template for retrieving esacped nth value of a query parameter (zero indexed) e.g. {{{ request.query.search.[5] }}}
	 *
	 * @param key
	 * @param index
	 */
	default String escapedQuery(String key, int index) {
		return query(key, index);
	}

	/**
	 * Returns the template for retrieving a escaped URL path
	 */
	default String escapedPath() {
		return path();
	}

	/**
	 * Returns the template for retrieving escaped nth value of a URL path (zero indexed) e.g. {{{ request.path.[2] }}}
	 *
	 * @param index
	 */
	default String escapedPath(int index) {
		return path(index);
	}

	/**
	 * Returns the template for retrieving the escaped first value of a request header e.g. {{{ request.headers.X-Request-Id }}}
	 *
	 * @param key
	 */
	default String escapedHeader(String key) {
		return header(key);
	}

	/**
	 * Returns the template for retrieving the esacaped nth value of a request header (zero indexed) e.g. {{{ request.headers.X-Request-Id.[5] }}}
	 *
	 * @param key
	 * @param index
	 */
	default String escapedHeader(String key, int index) {
		return header(key, index);
	}

	/**
	 * Retruns the template for retrieving the escaped first value of a cookie with certain key
	 *
	 * @param key
	 */
	default String escapedCookie(String key) {
		return cookie(key);
	}

	/**
	 * Request body text (avoid for non-text bodies) e.g. {{{ request.body }}} . The body will not be escaped
	 * so you won't be able to directly embed it in a JSON for example.
	 */
	default String escapedBody() {
		return body();
	}

	/**
	 * Request body text for the given JsonPath. e.g. {{{ jsonPath request.body '$.a.b.c' }}}
	 */
	default String escapedBody(String jsonPath) {
		return body(jsonPath);
	}

}
