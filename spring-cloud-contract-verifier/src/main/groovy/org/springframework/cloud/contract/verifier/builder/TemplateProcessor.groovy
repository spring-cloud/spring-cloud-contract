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

	/**
	 * Returns {@code true} if the current line contains template related entry. E.g. for Handlebars
	 * if a line contains &#123;&#123;&#123;...&#125;&#125;&#125; then it's considered to contain
	 * template related entry
	 */
	boolean containsTemplateEntry(String line)

	/**
	 * Returns {@code true} if the current line contains template related entry for json path processing.
	 * E.g. for Handlebars if a line contains &#123;&#123;&#123;jsonpath ...&#125;&#125;&#125; then
	 * it's considered to contain template related entry for json path processing
	 */
	boolean containsJsonPathTemplateEntry(String line)

	/**
	 * How does the opening template look like? Handlebars is using the Mustache template thus it looks like this
	 *  &#123;&#123;&#123; Mustache &#125;&#125;&#125;. In this case the opening template would
	 *  return &#123;&#123;&#123;
	 */
	String openingTemplate()

	/**
	 * How does the closing template look like? Handlebars is using the Mustache template thus it looks like this
	 *  &#123;&#123;&#123; Mustache &#125;&#125;&#125;. In this case the closing template would
	 *  return &#125;&#125;&#125;
	 */
	String closingTemplate()
}