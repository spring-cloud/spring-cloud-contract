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
 * For backward compatibility when assertions take place, first checks the custom setup.
 * Writes in a new format, can read the old format.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public class CompositeContractTemplate implements ContractTemplate {

	private final CustomHandlebarsContractTemplate custom = new CustomHandlebarsContractTemplate();

	private final HandlebarsContractTemplate template = new HandlebarsContractTemplate();

	@Override
	public boolean startsWithTemplate(String text) {
		if (this.custom.startsWithTemplate(text)) {
			return true;
		}
		return template.startsWithTemplate(text);
	}

	@Override
	public boolean startsWithEscapedTemplate(String text) {
		return this.template.startsWithEscapedTemplate(text);
	}

	@Override
	public String openingTemplate() {
		return this.template.openingTemplate();
	}

	@Override
	public String closingTemplate() {
		return this.template.closingTemplate();
	}

	@Override
	public String escapedOpeningTemplate() {
		return this.template.escapedOpeningTemplate();
	}

	@Override
	public String escapedClosingTemplate() {
		return this.template.escapedClosingTemplate();
	}

	@Override
	public String url() {
		return this.template.url();
	}

	@Override
	public String query(String key) {
		return this.template.query(key);
	}

	@Override
	public String query(String key, int index) {
		return this.template.query(key, index);
	}

	@Override
	public String path() {
		return this.template.path();
	}

	@Override
	public String path(int index) {
		return this.template.path(index);
	}

	@Override
	public String header(String key) {
		return this.template.header(key);
	}

	@Override
	public String header(String key, int index) {
		return this.template.header(key, index);
	}

	@Override
	public String cookie(String key) {
		return this.template.cookie(key);
	}

	@Override
	public String body() {
		return this.template.body();
	}

	@Override
	public String escapedBody() {
		// WireMock doesn't support proper escaping of JSON body
		// that's why we need to use our custom handlebars extension
		return this.custom.escapedBody();
	}

	@Override
	public String escapedBody(String jsonPath) {
		return this.template.escapedBody(jsonPath);
	}

	@Override
	public String body(String jsonPath) {
		return this.template.body(jsonPath);
	}

	@Override
	public String escapedUrl() {
		return this.template.escapedUrl();
	}

	@Override
	public String escapedQuery(String key) {
		return this.template.escapedQuery(key);
	}

	@Override
	public String escapedQuery(String key, int index) {
		return this.template.escapedQuery(key, index);
	}

	@Override
	public String escapedPath() {
		return this.template.escapedPath();
	}

	@Override
	public String escapedPath(int index) {
		return this.template.escapedPath(index);
	}

	@Override
	public String escapedHeader(String key) {
		return this.template.escapedHeader(key);
	}

	@Override
	public String escapedHeader(String key, int index) {
		return this.template.escapedHeader(key, index);
	}

	@Override
	public String escapedCookie(String key) {
		return this.template.escapedCookie(key);
	}

}
