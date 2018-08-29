package org.springframework.cloud.contract.verifier.builder.handlebars

import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.WiremockHelpers
import wiremock.com.github.jknack.handlebars.Helper
import wiremock.com.github.jknack.handlebars.Options
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
		if (context instanceof Map<String, Object>) {
			// legacy
			Map<String, Object> oldContext = (Map<String, Object>) context
			String jsonPath = options.param(0)
			Object model = oldContext.get(REQUEST_MODEL_NAME)
			if (model instanceof TestSideRequestTemplateModel) {
				return returnObjectForTest(model, jsonPath)
			} else if (model instanceof RequestTemplateModel) {
				return returnObjectForStub(model, jsonPath)
			}
			throw new IllegalArgumentException("Unsupported model")
		} else if (context instanceof String) {
			Object value = WiremockHelpers.jsonPath.apply(context, options)
			if (testSideModel(options)) {
				return processTestResponseValue(value)
			}
			return value
		}
		throw new IllegalArgumentException("Unsupported context")
	}

	private boolean testSideModel(Options options) {
		Object model = options.context.model()
		if (!(model instanceof Map)) {
			return false
		}
		Map map = (Map) model
		return map.values().any { it instanceof TestSideRequestTemplateModel }
	}

	private Object returnObjectForStub(Object model, String jsonPath) {
		DocumentContext documentContext = JsonPath.parse(((RequestTemplateModel) model).body)
		return documentContext.read(jsonPath)
	}

	private Object returnObjectForTest(TestSideRequestTemplateModel model, String jsonPath) {
		String body = removeSurroundingQuotes(model.rawBody).replace('\\"', '"')
		DocumentContext documentContext = JsonPath.parse(body)
		Object value = documentContext.read(jsonPath)
		return processTestResponseValue(value)
	}

	private Object processTestResponseValue(Object value) {
		if (value instanceof Long) {
			return String.valueOf(value) + "L"
		}
		return value
	}

	private String removeSurroundingQuotes(String body) {
		if (body.startsWith('"') && body.endsWith('"')) {
			return body.substring(1, body.length() - 1)
		}
		return body
	}

}
