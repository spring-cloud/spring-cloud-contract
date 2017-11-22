package org.springframework.cloud.contract.verifier.builder

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import org.apache.commons.text.StringEscapeUtils

import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.verifier.util.ContentUtils
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * Representation of the request side to be used for response templating in
 * the generated tests.
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
	 * List of path entries
	 */
	final Path path

	/**
	 * Map containing request headers
	 */
	final Map<String, List<String>> headers

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
		Path paths = new Path(buildPathsFromUrl(url))
		Map<String, List<String>> query = (Map<String, List<String>>) (request.url ?: request.urlPath)
				.queryParameters?.parameters?.groupBy { it.name }?.collectEntries {
			[(it.key): it.value.collect { MapConverter.getTestSideValues(it) }]
		}
		String fullUrl = (query == null || query.isEmpty()) ? url :
				url + "?" + query.collect { String name, List<String> values ->
			return values.collect { "${name}=${it}"}.join("&") }.join("&")
		Map<String, List<String>> headers = (Map<String, List<String>>) (request.headers?.entries?.groupBy {
			it.name
		}?.collectEntries {
			[(it.key): it.value.collect {  MapConverter.getTestSideValues(it) }]
		})
		String body = trimmedAndEscapedBody(request.body)
		String rawBody = getBodyAsRawJson(request.body)
		return new TestSideRequestTemplateModel(fullUrl, query, paths, headers, body, rawBody)
	}

	private static List<String> buildPathsFromUrl(String url) {
		String fakeUrl = "http://foo.bar" + (url.startsWith("/") ? url : "/" + url)
		List<String> paths = new URL(fakeUrl).path.split("/") as List<String>
		if (!paths.isEmpty()) {
			paths.remove(0)
		}
		return paths
	}

	private static String trimmedAndEscapedBody(Object body) {
		String rawBody = getBodyAsRawJson(body)
		return StringEscapeUtils.escapeJava(rawBody)
	}

	private static String getBodyAsRawJson(Object body) {
		Object bodyValue = extractServerValueFromBody(body)
		return bodyValue != null ? new JsonOutput().toJson(bodyValue) : bodyValue
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

@CompileStatic
class Path extends ArrayList<String> {

	Path(List<String> list) {
		this.addAll(list)
	}

	@Override
	String toString() {
		return "/" + this.join("/")
	}
}
