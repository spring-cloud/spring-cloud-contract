/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.spec.internal;

/**
 * Contains Http Methods.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.2
 */
public final class HttpMethods {

	public HttpMethods() {
		System.out.println("WARNING: HttpMethods shouldn't be instantiated");
	}

	/**
	 * @return {@code GET}.
	 */
	public static HttpMethod GET() {
		return HttpMethod.GET;
	}

	/**
	 * @return {@code HEAD}.
	 */
	public static HttpMethod HEAD() {
		return HttpMethod.HEAD;
	}

	/**
	 * @return {@code POST}.
	 */
	public static HttpMethod POST() {
		return HttpMethod.POST;
	}

	/**
	 * @return {@code PUT}.
	 */
	public static HttpMethod PUT() {
		return HttpMethod.PUT;
	}

	/**
	 * @return {@code PATCH}.
	 */
	public static HttpMethod PATCH() {
		return HttpMethod.PATCH;
	}

	/**
	 * @return {@code DELETE}.
	 */
	public static HttpMethod DELETE() {
		return HttpMethod.DELETE;
	}

	/**
	 * @return {@code OPTIONS}.
	 */
	public static HttpMethod OPTIONS() {
		return HttpMethod.OPTIONS;
	}

	/**
	 * @return {@code TRACE}.
	 */
	public static HttpMethod TRACE() {
		return HttpMethod.TRACE;
	}

	public enum HttpMethod {

		GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

	}

}
