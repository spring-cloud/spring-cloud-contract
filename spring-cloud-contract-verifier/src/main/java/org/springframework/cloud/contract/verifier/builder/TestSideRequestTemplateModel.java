/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import groovy.json.JsonOutput;
import groovy.lang.GString;
import groovy.transform.CompileStatic;
import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.QueryParameter;
import org.springframework.cloud.contract.spec.internal.QueryParameters;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.util.ContentUtils;
import org.springframework.cloud.contract.verifier.util.MapConverter;

/**
 * Representation of the request side to be used for response templating in the generated
 * tests.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class TestSideRequestTemplateModel {

	/**
	 * Request URL
	 */
	private final String url;

	/**
	 * Map containing query parameters
	 */
	private final Map<String, List<Object>> query;

	/**
	 * List of path entries
	 */
	private final Path path;

	/**
	 * Map containing request headers
	 */
	private final Map<String, List<String>> headers;

	/**
	 * Request body as it would be sent to the controller
	 */
	private final String body;

	/**
	 * Escaped request body that can be put into test
	 */
	private final String escapedBody;

	private TestSideRequestTemplateModel(String url, Map<String, List<Object>> query, Path path,
			Map<String, List<String>> headers, String body, String escapedBody) {
		this.url = url;
		this.query = query;
		this.path = path;
		this.headers = headers;
		this.body = body;
		this.escapedBody = escapedBody;
	}

	public String getUrl() {
		return this.url;
	}

	public Map<String, List<Object>> getQuery() {
		return this.query;
	}

	public Path getPath() {
		return this.path;
	}

	public Map<String, List<String>> getHeaders() {
		return this.headers;
	}

	public String getBody() {
		return this.body;
	}

	public String getEscapedBody() {
		return this.escapedBody;
	}

	public static TestSideRequestTemplateModel from(final Request request) {
		Url urlPath = request.getUrl() != null ? request.getUrl() : request.getUrlPath();
		String url = MapConverter.getTestSideValues(urlPath).toString();
		Path paths = new Path(buildPathsFromUrl(url));
		QueryParameters queryParameters = urlPath.getQueryParameters();
		Map<String, List<Object>> query = query(queryParameters);
		boolean queryParamsPresent = query == null || query.isEmpty();
		String fullUrl = fullUrl(url, query, queryParamsPresent);
		boolean headersEntriesPresent = request.getHeaders() != null && !request.getHeaders().getEntries().isEmpty();
		Map<String, List<String>> headers = headers(request, headersEntriesPresent);
		String escapedBody = trimmedAndEscapedBody(request.getBody());
		String body = getBodyAsRawJson(request.getBody());
		return new TestSideRequestTemplateModel(fullUrl, query, paths, headers, body, escapedBody);
	}

	private static Map<String, List<String>> headers(Request request, boolean headersEntriesPresent) {
		if (!headersEntriesPresent) {
			return new HashMap<>();
		}
		return request.getHeaders().getEntries().stream()
				.collect(Collectors.groupingBy(Header::getName,
						Collectors.mapping((Function<Object, String>) o -> MapConverter.getTestSideValues(o).toString(),
								Collectors.toList())));
	}

	private static String fullUrl(String url, Map<String, List<Object>> query, boolean queryParamsPresent) {
		if (queryParamsPresent) {
			return url;
		}
		String joinedParams = query.entrySet().stream().map(
				entry -> entry.getValue().stream().map(s -> entry.getKey() + "=" + s).collect(Collectors.joining("&")))
				.collect(Collectors.joining("&"));
		return url + "?" + joinedParams;
	}

	private static Map<String, List<Object>> query(QueryParameters queryParameters) {
		if (queryParameters == null) {
			return new HashMap<>();
		}
		return queryParameters.getParameters().stream().collect(Collectors.groupingBy(QueryParameter::getName,
				Collectors.mapping(MapConverter::getTestSideValues, Collectors.toList())));
	}

	private static List<String> buildPathsFromUrl(String url) {
		String fakeUrl = "https://foo.bar" + (url.startsWith("/") ? url : "/" + url);
		List<String> paths;
		try {
			paths = new LinkedList<>(Arrays.asList(new URL(fakeUrl).getPath().split("/")));
		}
		catch (MalformedURLException ex) {
			throw new IllegalStateException(ex);
		}
		if (!paths.isEmpty()) {
			paths.remove(0);
		}
		return paths;
	}

	private static String trimmedAndEscapedBody(Object body) {
		String rawBody = getBodyAsRawJson(body);
		return StringEscapeUtils.escapeJava(rawBody);
	}

	private static String getBodyAsRawJson(Object body) {
		Object bodyValue = extractServerValueFromBody(body);
		if (bodyValue instanceof GString || bodyValue instanceof String) {
			return bodyValue.toString();
		}
		else if (bodyValue instanceof FromFileProperty) {
			return null;
		}
		return bodyValue != null ? new JsonOutput().toJson(bodyValue) : null;
	}

	private static Object extractServerValueFromBody(Object bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = ContentUtils.extractValue((GString) bodyValue, ContentUtils.GET_TEST_SIDE_FUNCTION);
		}
		else {
			bodyValue = MapConverter.transformValues(bodyValue, ContentUtils.GET_TEST_SIDE);
		}
		return bodyValue;
	}

}

@CompileStatic
class Path extends ArrayList<String> {

	Path(List<String> list) {
		this.addAll(list);
	}

	@Override
	public String toString() {
		return "/" + String.join("/", this);
	}

}
