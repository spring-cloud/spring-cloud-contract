package org.springframework.cloud.contract.verifier.builder.handlebars

import wiremock.com.github.jknack.handlebars.Helper
import wiremock.com.github.jknack.handlebars.Options
import com.github.tomakehurst.wiremock.extension.responsetemplating.RequestTemplateModel
import groovy.transform.CompileStatic
import org.apache.commons.lang3.StringEscapeUtils
import org.springframework.cloud.contract.verifier.builder.TestSideRequestTemplateModel
/**
 * A Handlebars helper for the {@code escapejsonbody} helper function.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class HandlebarsEscapeHelper implements Helper<Map<String, Object>> {

	public static final String NAME = "escapejsonbody"
	public static final String REQUEST_MODEL_NAME = "request"

	@Override
	Object apply(Map<String, Object> context, Options options) throws IOException {
		Object model = context.get(REQUEST_MODEL_NAME)
		if (model instanceof TestSideRequestTemplateModel) {
			return StringEscapeUtils.escapeJson(returnObjectForTest(model).toString())
		} else if (model instanceof RequestTemplateModel) {
			return StringEscapeUtils.escapeJson(returnObjectForStub(model).toString())
		}
		throw new IllegalArgumentException("Unsupported model")
	}

	private Object returnObjectForStub(Object model) {
		return ((RequestTemplateModel) model).body
	}

	private Object returnObjectForTest(Object model) {
		return ((TestSideRequestTemplateModel) model).rawBody
	}

}
