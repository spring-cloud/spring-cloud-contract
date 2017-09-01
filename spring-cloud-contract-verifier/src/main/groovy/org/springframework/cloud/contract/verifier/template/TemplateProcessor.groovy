package org.springframework.cloud.contract.verifier.template

import org.springframework.cloud.contract.spec.internal.Request

/**
 * Contract for conversion of templated responses.
 *
 * If no implementation is provided then Handlebars will be picked as a default implementation.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
interface TemplateProcessor {

	/**
	 * For the given {@link Request} and the test contents should perform a transformation
	 * and return the converted test
	 */
	String transform(Request request, String testContents)

	/**
	 * Returns {@code true} if the current line contains template related entry. E.g. for Handlebars
	 * if a line contains {{{...}}} then it's considered to contain template related entry
	 */
	boolean containsTemplateEntry(String line)

	/**
	 * Returns {@code true} if the current line contains template related entry for json path processing.
	 * E.g. for Handlebars if a line contains {{{jsonpath ...}}} then
	 * it's considered to contain template related entry for json path processing
	 */
	boolean containsJsonPathTemplateEntry(String line)

	/**
	 * Returns the json path entry from the current line that contains template related entry for json path processing.
	 * E.g. for Handlebars if a line contains {{{jsonpath this '$.a.b.c'}}} then
	 * the te method would return {@code $.a.b.c}. Returns empty string if there's no matching
	 * json path entry
	 */
	String jsonPathFromTemplateEntry(String line)

}