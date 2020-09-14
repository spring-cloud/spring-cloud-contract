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

import java.util.HashMap;
import java.util.Map;

/**
 * Abstraction over a HTTP response.
 *
 * Warning! This API is experimental and can change in time.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class Response {

	private final int statusCode;

	private final Body body;

	private final Map<String, Object> headers;

	private final Map<String, Object> cookies;

	Response(int statusCode, Body body, Map<String, Object> headers,
			Map<String, Object> cookies) {
		this.statusCode = statusCode;
		this.body = body;
		this.headers = headers;
		this.cookies = cookies;
	}

	/**
	 * @return numerical representation of a status code
	 */
	public int statusCode() {
		return this.statusCode;
	}

	/**
	 * @param key header key
	 * @return header value or null if not present
	 */
	public String header(String key) {
		return this.headers.entrySet().stream()
				.filter(e -> e.getKey().equalsIgnoreCase(key)).findFirst()
				.map(e -> e.getValue().toString()).orElse(null);
	}

	/**
	 * @param key cookie key
	 * @return header value or null if not present
	 */
	public String cookie(String key) {
		return this.cookies.entrySet().stream()
				.filter(e -> e.getKey().equalsIgnoreCase(key)).findFirst()
				.map(e -> e.getValue().toString()).orElse(null);
	}

	/**
	 * @return response body
	 */
	public Body getBody() {
		return this.body;
	}

	/**
	 * @return builder
	 */
	public static Response.Builder builder() {
		return new Response.Builder();
	}

	/**
	 * @return headers
	 */
	public Map<String, Object> headers() {
		return this.headers;
	}

	/**
	 * @return cookies
	 */
	public Map<String, Object> cookies() {
		return this.cookies;
	}

	/**
	 * Response builder.
	 */
	public static class Builder {

		int statusCode;

		Body body;

		Map<String, Object> headers = new HashMap<>();

		Map<String, Object> cookies = new HashMap<>();

		/**
		 * @param status as int
		 * @return builder
		 */
		public Response.Builder statusCode(int status) {
			this.statusCode = status;
			return this;
		}

		/**
		 * @param body - response body
		 * @return builder
		 */
		public Response.Builder body(Object body) {
			this.body = new Body(body);
			return this;
		}

		/**
		 * @param headers - response headers
		 * @return builder
		 */
		public Response.Builder headers(Map<String, Object> headers) {
			this.headers = headers;
			return this;
		}

		/**
		 * @param key header key
		 * @param value header value
		 * @return builder
		 */
		public Response.Builder header(String key, Object value) {
			this.headers.put(key, value);
			return this;
		}

		/**
		 * @param cookies - response cookies
		 * @return builder
		 */
		public Response.Builder cookies(Map<String, Object> cookies) {
			this.cookies = cookies;
			return this;
		}

		/**
		 * @return response
		 */
		public Response build() {
			return new Response(this.statusCode, this.body, this.headers, this.cookies);
		}

	}

}
