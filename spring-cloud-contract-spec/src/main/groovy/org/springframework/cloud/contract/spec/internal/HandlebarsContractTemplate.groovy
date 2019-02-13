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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic

import org.springframework.cloud.contract.spec.ContractTemplate

/**
 * Represents the structure of templates using Handlebars compatible with
 * WireMock template model requirements.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class HandlebarsContractTemplate implements ContractTemplate {

	@Override
	String openingTemplate() {
		return "{{"
	}

	@Override
	String closingTemplate() {
		return "}}"
	}

	@Override
	String escapedOpeningTemplate() {
		return "{{{"
	}

	@Override
	String escapedClosingTemplate() {
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
		return escapedWrapped("request.body")
	}

	@Override
	String escapedBody(String jsonPath) {
		return escapedWrapped("jsonPath request.body '${jsonPath}'")
	}

	@Override
	String body(String jsonPath) {
		return wrapped("jsonPath request.body '${jsonPath}'")
	}

	@Override
	String escapedUrl() {
		return escapedWrapped("request.url")
	}

	@Override
	String escapedQuery(String key) {
		return escapedQuery(key, 0)
	}

	@Override
	String escapedQuery(String key, int index) {
		return escapedWrapped("request.query.${key}.[${index}]")
	}

	@Override
	String escapedPath() {
		return escapedWrapped("request.path")
	}

	@Override
	String escapedPath(int index) {
		return escapedWrapped("request.path.[${index}]")
	}

	@Override
	String escapedHeader(String key) {
		return escapedHeader(key, 0)
	}

	@Override
	String escapedHeader(String key, int index) {
		return escapedWrapped("request.headers.${key}.[${index}]")
	}

	@Override
	String escapedCookie(String key) {
		return escapedWrapped("request.cookies.${key}")
	}

	private String wrapped(String text) {
		return openingTemplate() + text + closingTemplate()
	}

	private String escapedWrapped(String text) {
		return escapedOpeningTemplate() + text + escapedClosingTemplate()
	}
}
