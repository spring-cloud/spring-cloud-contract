package org.springframework.cloud.contract.verifier.builder

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
}