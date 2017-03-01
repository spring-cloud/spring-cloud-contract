package org.springframework.cloud.contract.verifier.builder.handlebars

import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.github.tomakehurst.wiremock.extension.responsetemplating.RequestTemplateModel
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.verifier.builder.TestSideRequestTemplateModel

/**
 * A Handlebars helper for the {@code jsonpath} helper function.
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
		if (model instanceof TestSideRequestTemplateModel) {
			return returnObjectForTest(model, jsonPath)
		} else if (model instanceof RequestTemplateModel) {
			return returnObjectForStub(model, jsonPath)
		}
		throw new IllegalArgumentException("Unsupported model")
	}

	private Object returnObjectForStub(Object model, String jsonPath) {
		DocumentContext documentContext = JsonPath.parse(((RequestTemplateModel) model).body)
		return documentContext.read(jsonPath)
	}

	private Object returnObjectForTest(Object model, String jsonPath) {
		DocumentContext documentContext = JsonPath.parse(((TestSideRequestTemplateModel) model).rawBody)
		return documentContext.read(jsonPath)
	}

}
