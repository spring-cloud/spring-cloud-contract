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

package org.springframework.cloud.contract.spec.internal;

import org.springframework.cloud.contract.spec.ContractTemplate;

/**
 * Represents the structure of templates using Handlebars compatible with WireMock
 * template model requirements.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class HandlebarsContractTemplate implements ContractTemplate {

	@Override
	public String openingTemplate() {
		return "{{";
	}

	@Override
	public String closingTemplate() {
		return "}}";
	}

	@Override
	public String escapedOpeningTemplate() {
		return "{{{";
	}

	@Override
	public String escapedClosingTemplate() {
		return "}}}";
	}

	@Override
	public String url() {
		return wrapped("request.url");
	}

	@Override
	public String query(String key) {
		return query(key, 0);
	}

	@Override
	public String query(String key, int index) {
		return wrapped("request.query." + key + ".[" + index + "]");
	}

	@Override
	public String path() {
		return wrapped("request.path");
	}

	@Override
	public String path(int index) {
		return wrapped("request.path.[" + index + "]");
	}

	@Override
	public String header(String key) {
		return header(key, 0);
	}

	@Override
	public String header(String key, int index) {
		return wrapped("request.headers." + key + ".[" + index + "]");
	}

	@Override
	public String cookie(String key) {
		return wrapped("request.cookies." + key);
	}

	@Override
	public String body() {
		return wrapped("request.body");
	}

	@Override
	public String escapedBody() {
		return escapedWrapped("escapejsonbody");
	}

	@Override
	public String escapedBody(String jsonPath) {
		return escapedWrapped("jsonPath request.body \'" + jsonPath + "\'");
	}

	@Override
	public String body(String jsonPath) {
		return wrapped("jsonPath request.body \'" + jsonPath + "\'");
	}

	@Override
	public String escapedUrl() {
		return escapedWrapped("request.url");
	}

	@Override
	public String escapedQuery(String key) {
		return escapedQuery(key, 0);
	}

	@Override
	public String escapedQuery(String key, int index) {
		return escapedWrapped("request.query." + key + ".[" + index + "]");
	}

	@Override
	public String escapedPath() {
		return escapedWrapped("request.path");
	}

	@Override
	public String escapedPath(int index) {
		return escapedWrapped("request.path.[" + index + "]");
	}

	@Override
	public String escapedHeader(String key) {
		return escapedHeader(key, 0);
	}

	@Override
	public String escapedHeader(String key, int index) {
		return escapedWrapped("request.headers." + key + ".[" + index + "]");
	}

	@Override
	public String escapedCookie(String key) {
		return escapedWrapped("request.cookies." + key);
	}

	private String wrapped(String text) {
		return openingTemplate() + text + closingTemplate();
	}

	private String escapedWrapped(String text) {
		return escapedOpeningTemplate() + text + escapedClosingTemplate();
	}

}
