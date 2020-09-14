/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.http;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.contract.spec.internal.HttpMethods;

/**
 * Abstraction over a HTTP request.
 *
 * Warning! This API is experimental and can change in time.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class Request {

	private final HttpMethods.HttpMethod method;

	private final String path;

	private final Body body;

	private final Map<String, Object> headers;

	private final Map<String, Object> cookies;

	Request(HttpMethods.HttpMethod method, String path, Body body,
			Map<String, Object> headers, Map<String, Object> cookies) {
		this.method = method;
		this.path = path;
		this.body = body;
		this.headers = headers == null ? new HashMap<>() : headers;
		this.cookies = cookies == null ? new HashMap<>() : cookies;
	}

	/**
	 * @return content type from headers
	 */
	public String contentType() {
		Object value = this.headers.entrySet().stream()
				.filter(e -> e.getKey().toLowerCase().equals("content-type")).findFirst()
				.orElse(new AbstractMap.SimpleEntry<>("", null)).getValue();
		if (value == null) {
			return null;
		}
		return value.toString();
	}

	/**
	 * @return HTTP method
	 */
	public HttpMethods.HttpMethod method() {
		return this.method;
	}

	/**
	 * @return HTTP path
	 */
	public String path() {
		return this.path;
	}

	/**
	 * @return request body
	 */
	public Body body() {
		return this.body;
	}

	/**
	 * @return request headers
	 */
	public Map<String, Object> headers() {
		return this.headers;
	}

	/**
	 * @return request cookies
	 */
	public Map<String, Object> cookies() {
		return this.cookies;
	}

	/**
	 * Factory method to pick the HTTP method.
	 * @return method builder
	 */
	public static Request.MethodBuilder given() {
		return new Request.MethodBuilder();
	}

	/**
	 * Builder over HTTP methods.
	 */
	public static class MethodBuilder {

		/**
		 * Factory method for DELETE HTTP method.
		 * @param path to call
		 * @return builder
		 */
		public Request.Builder delete(String path) {
			return new Request.Builder(HttpMethods.HttpMethod.DELETE, path);
		}

		/**
		 * Factory method for GET HTTP method.
		 * @param path to call
		 * @return builder
		 */
		public Request.Builder get(String path) {
			return new Request.Builder(HttpMethods.HttpMethod.GET, path);
		}

		/**
		 * Factory method for HEAD HTTP method.
		 * @param path to call
		 * @return builder
		 */
		public Request.Builder head(String path) {
			return new Request.Builder(HttpMethods.HttpMethod.HEAD, path);
		}

		/**
		 * Factory method for OPTIONS HTTP method.
		 * @param path to call
		 * @return builder
		 */
		public Request.Builder options(String path) {
			return new Request.Builder(HttpMethods.HttpMethod.OPTIONS, path);
		}

		/**
		 * Factory method for PATCH HTTP method.
		 * @param path to call
		 * @return builder
		 */
		public Request.Builder patch(String path) {
			return new Request.Builder(HttpMethods.HttpMethod.PATCH, path);
		}

		/**
		 * Factory method for POST HTTP method.
		 * @param path to call
		 * @return builder
		 */
		public Request.Builder post(String path) {
			return new Request.Builder(HttpMethods.HttpMethod.POST, path);
		}

		/**
		 * Factory method for PUT HTTP method.
		 * @param path to call
		 * @return builder
		 */
		public Request.Builder put(String path) {
			return new Request.Builder(HttpMethods.HttpMethod.PUT, path);
		}

		/**
		 * Factory method for TRACE HTTP method.
		 * @param path to call
		 * @return builder
		 */
		public Request.Builder trace(String path) {
			return new Request.Builder(HttpMethods.HttpMethod.TRACE, path);
		}

	}

	/**
	 * Builder for a {@link Request}.
	 */
	public static class Builder {

		final HttpMethods.HttpMethod method;

		final String path;

		Body body;

		Map<String, Object> headers = new HashMap<>();

		Map<String, Object> cookies = new HashMap<>();

		Builder(HttpMethods.HttpMethod method, String path) {
			this.method = method;
			this.path = path;
		}

		/**
		 * @param body HTTP body
		 * @return builder
		 */
		public Request.Builder body(Object body) {
			this.body = new Body(body);
			return this;
		}

		/**
		 * @param headers HTTP headers
		 * @return builder
		 */
		public Request.Builder headers(Map<String, Object> headers) {
			this.headers = headers;
			return this;
		}

		/**
		 * @param key HTTP key
		 * @param value HTTP value
		 * @return builder
		 */
		public Request.Builder header(String key, Object value) {
			this.headers.put(key, value);
			return this;
		}

		/**
		 * @param cookies HTTP cookies
		 * @return builder
		 */
		public Request.Builder cookies(Map<String, Object> cookies) {
			this.cookies = cookies;
			return this;
		}

		/**
		 * @return built {@link Request}
		 */
		public Request build() {
			return new Request(this.method, this.path, this.body, this.headers,
					this.cookies);
		}

	}

}
