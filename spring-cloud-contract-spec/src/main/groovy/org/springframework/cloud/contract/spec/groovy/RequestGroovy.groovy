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

package org.springframework.cloud.contract.spec.groovy

import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.util.logging.Commons

import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.ClientDslProperty
import org.springframework.cloud.contract.spec.internal.Common
import org.springframework.cloud.contract.spec.internal.Cookies
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.HttpMethods
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.Multipart
import org.springframework.cloud.contract.spec.internal.OptionalProperty
import org.springframework.cloud.contract.spec.internal.PatternValueDslProperty
import org.springframework.cloud.contract.spec.internal.RegexProperty
import org.springframework.cloud.contract.spec.internal.ServerDslProperty
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.spec.internal.UrlPath
import org.springframework.cloud.contract.spec.util.RegexpUtils

/**
 * Represents the request side of the HTTP communication
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@Commons
@TypeChecked
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class RequestGroovy extends Common {

		void url(Object url, @DelegatesTo(UrlPath) Closure closure) {
		this.url = new Url(url)
		closure.delegate = this.url
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	void url(DslProperty url, @DelegatesTo(UrlPath) Closure closure) {
		this.url = new Url(url)
		closure.delegate = this.url
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	void urlPath(String path, @DelegatesTo(UrlPath) Closure closure) {
		this.urlPath = new UrlPath(path)
		closure.delegate = urlPath
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	void urlPath(GString path, @DelegatesTo(UrlPath) Closure closure) {
		this.urlPath = new UrlPath(path)
		closure.delegate = urlPath
		closure()
	}

	/**
	 * URL to which the request will be sent. Allows to customize
	 * additional query parameters if needed
	 */
	void urlPath(DslProperty path, @DelegatesTo(UrlPath) Closure closure) {
		this.urlPath = new UrlPath(path)
		closure.delegate = urlPath
		closure()
	}

	/**
	 * Allows to configure HTTP headers
	 */
	void headers(@DelegatesTo(RequestHeaders) Closure closure) {
		this.headers = new RequestHeaders()
		closure.delegate = headers
		closure()
	}

	/**
	 * Allows to configure HTTP cookies
	 */
	void cookies(@DelegatesTo(RequestCookies) Closure closure) {
		this.cookies = new RequestCookies()
		closure.delegate = cookies
		closure()
	}

	/**
	 * @deprecated Deprecated in favor of bodyMatchers to support other future bodyMatchers too
	 */
	@Deprecated
	void stubMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		log.warn("stubMatchers method is deprecated. Please use bodyMatchers instead")
		bodyMatchers(closure)
	}

	/**
	 * Allows to set matchers for the body
	 */
	void bodyMatchers(@DelegatesTo(BodyMatchers) Closure closure) {
		this.bodyMatchers = new BodyMatchers()
		closure.delegate = this.bodyMatchers
		closure()
	}

}
