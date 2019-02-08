/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.apache.commons.text.StringEscapeUtils

import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
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
	 * Request body as it would be sent to the controller
	 */
	final String body

	/**
	 * Escaped request body that can be put into test
	 */
	final String escapedBody

	private TestSideRequestTemplateModel(String url, Map<String, List<String>> query, Path path, Map<String, List<String>> headers, String body, String escapedBody) {
		this.url = url
		this.query = query
		this.path = path
		this.headers = headers
		this.body = body
		this.escapedBody = escapedBody
	}

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
			List<Object> headerValues = []
			for (Object value : it.value) {
				headerValues.add(MapConverter.getTestSideValues(value))
			}
			[(it.key): headerValues]
		})
		String escapedBody = trimmedAndEscapedBody(request.body)
		String body = getBodyAsRawJson(request.body)
		return new TestSideRequestTemplateModel(fullUrl, query, paths, headers, body, escapedBody)
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
		if (bodyValue instanceof GString || bodyValue instanceof String) {
			return bodyValue.toString()
		}
		else if (bodyValue instanceof FromFileProperty) {
			return null
		}
		return bodyValue != null ? new JsonOutput().toJson(bodyValue) : bodyValue
	}

	protected static Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = ContentUtils.
				extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue } as Closure)
		}
		else {
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
