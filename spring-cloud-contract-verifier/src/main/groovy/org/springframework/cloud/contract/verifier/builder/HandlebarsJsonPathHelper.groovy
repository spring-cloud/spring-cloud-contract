package org.springframework.cloud.contract.verifier.builder

import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.transform.CompileStatic

/**
 * A Handlebars helper for the {@code jsonpath} helper function
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class HandlebarsJsonPathHelper implements Helper<Map<String, TestSideRequestTemplateModel>> {

	public static final String NAME = "jsonpath"
	public static final String REQUEST_MODEL_NAME = "request"

	@Override
	Object apply(Map<String, TestSideRequestTemplateModel> context, Options options) throws IOException {
		String jsonPath = options.param(0)
		TestSideRequestTemplateModel model = context.get(REQUEST_MODEL_NAME)
		DocumentContext documentContext = JsonPath.parse(model.rawBody)
		Object o = documentContext.read(jsonPath)
		if (o instanceof String) {
			return '"' + o + '"'
		}
		return o
	}

}
