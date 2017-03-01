package org.springframework.cloud.contract.verifier.builder

import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.github.tomakehurst.wiremock.extension.responsetemplating.RequestTemplateModel
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.transform.CompileStatic
/**
 * A Handlebars helper for the {@code jsonpath} helper function. It can't operate on direct
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class HandlebarsJsonPathHelper implements Helper<Map<String, Object>> {

	public static final String NAME = "jsonpath"
	public static final String REQUEST_MODEL_NAME = "request"

	@Override
	Object apply(Map<String, Object> context, Options options) throws IOException {
		String jsonPath = options.param(0)
		Object model = context.get(REQUEST_MODEL_NAME)
		DocumentContext documentContext = documentContext(model)
		Object o = documentContext.read(jsonPath)
		if (o instanceof String) {
			return '"' + o + '"'
		}
		return o
	}

	private DocumentContext documentContext(Object model) {
		if (model instanceof TestSideRequestTemplateModel) {
			return JsonPath.parse(((TestSideRequestTemplateModel) model).rawBody)
		} else if (model instanceof RequestTemplateModel) {
			return JsonPath.parse(((RequestTemplateModel) model).body)
		}
		throw new IllegalArgumentException("Unsupported model")
	}

}
