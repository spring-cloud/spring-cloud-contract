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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.ContractTemplate

/**
 * Represents the structure of templates using Handlebars compatible with
 * WireMock template model requirements.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0*
 * @deprecated use{@link HandlebarsContractTemplate}
 */
@CompileStatic
@PackageScope
class CustomHandlebarsContractTemplate implements ContractTemplate {

	@Override
	String openingTemplate() {
		return "{{{"
	}

	@Override
	String closingTemplate() {
		return "}}}"
	}

	@Override
	String url() {
		return wrapped("request.url")
	}

	@Override
	String query(String key) {
		return query(key, 0)
	}

	@Override
	String query(String key, int index) {
		return wrapped("request.query.${key}.[${index}]")
	}

	@Override
	String path() {
		return wrapped("request.path")
	}

	@Override
	String path(int index) {
		return wrapped("request.path.[${index}]")
	}

	@Override
	String header(String key) {
		return header(key, 0)
	}

	@Override
	String header(String key, int index) {
		return wrapped("request.headers.${key}.[${index}]")
	}

	@Override
	String cookie(String key) {
		return wrapped("request.cookies.${key}")
	}

	@Override
	String body() {
		return wrapped("request.body")
	}

	@Override
	String escapedBody() {
		return wrapped("escapejsonbody")
	}

	@Override
	String escapedBody(String jsonPath) {
		return body(jsonPath)
	}

	@Override
	String body(String jsonPath) {
		return wrapped("jsonpath this '${jsonPath}'")
	}

	private String wrapped(String text) {
		return openingTemplate() + text + closingTemplate()
	}
}
