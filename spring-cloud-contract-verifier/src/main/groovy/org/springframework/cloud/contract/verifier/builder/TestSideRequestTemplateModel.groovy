package org.springframework.cloud.contract.verifier.builder

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import org.apache.commons.lang3.StringEscapeUtils
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.verifier.util.ContentUtils
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * Representation of the request side. Contains all data necessary to perform
 * response templating
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@Immutable
@CompileStatic
class TestSideRequestTemplateModel {
	/**
	 * Request URL
	 */
	final String url

	/**
	 * Map containing query parameters
	 */
	final Map<String, List<String>> query

	/**
	 * Map containing request headers
	 */
	final Map<String, String> headers

	/**
	 * Escaped request body that can be put into test
	 */
	final String body

	/**
	 * Request body as it would be sent to the controller
	 */
	final String rawBody

	static TestSideRequestTemplateModel from(final Request request) {
		String url = MapConverter.getTestSideValues(request.url ?: request.urlPath)
		Map<String, List<String>> query = (Map<String, List<String>>) (request.url ?: request.urlPath)
				.queryParameters?.parameters?.groupBy { it.name }?.collectEntries {
			[(it.key): it.value.collect { MapConverter.getTestSideValues(it) }]
		}
		Map<String, String> headers = (request.headers?.entries?.collectEntries {
			[(it.name): MapConverter.getTestSideValues(it)]
		})
		String body = trimmedAndEscapedBody(request.body)
		String rawBody = getBodyAsRawJson(request.body)
		return new TestSideRequestTemplateModel(url, query, headers, body, rawBody)
	}

	private static String trimmedAndEscapedBody(Object body) {
		String rawBody = getBodyAsRawJson(body)
		return StringEscapeUtils.escapeJava(rawBody)
	}

	protected static String getBodyAsRawJson(Object body) {
		Object bodyValue = extractServerValueFromBody(body)
		return new JsonOutput().toJson(bodyValue)
	}

	protected static Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = ContentUtils.extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = MapConverter.transformValues(bodyValue, {
				it instanceof DslProperty ? it.serverValue : it
			})
		}
		return bodyValue
	}
}
