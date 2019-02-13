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
 * For backward compatibility when assertions take place,
 * first checks the custom setup. Writes in a new format, can read
 * the old format.
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
@CompileStatic
class CompositeContractTemplate implements ContractTemplate {

	private final CustomHandlebarsContractTemplate custom = new CustomHandlebarsContractTemplate()
	private final HandlebarsContractTemplate template = new HandlebarsContractTemplate()

	@Override
	boolean startsWithTemplate(String text) {
		if (this.custom.startsWithTemplate(text)) {
			return true
		}
		return template.startsWithTemplate(text)
	}

	@Override
	boolean startsWithEscapedTemplate(String text) {
		return this.template.startsWithEscapedTemplate(text)
	}

	@Override
	String openingTemplate() {
		return this.template.openingTemplate()
	}

	@Override
	String closingTemplate() {
		return this.template.closingTemplate()
	}

	@Override
	String escapedOpeningTemplate() {
		return this.template.escapedOpeningTemplate()
	}

	@Override
	String escapedClosingTemplate() {
		return this.template.escapedClosingTemplate()
	}

	@Override
	String url() {
		return this.template.url()
	}

	@Override
	String query(String key) {
		return this.template.query(key)
	}

	@Override
	String query(String key, int index) {
		return this.template.query(key, index)
	}

	@Override
	String path() {
		return this.template.path()
	}

	@Override
	String path(int index) {
		return this.template.path(index)
	}

	@Override
	String header(String key) {
		return this.template.header(key)
	}

	@Override
	String header(String key, int index) {
		return this.template.header(key, index)
	}

	@Override
	String cookie(String key) {
		return this.template.cookie(key)
	}

	@Override
	String body() {
		return this.template.body()
	}

	@Override
	String escapedBody() {
		// WireMock doesn't support proper escaping of JSON body
		// that's why we need to use our custom handlebars extension
		return this.custom.escapedBody()
	}

	@Override
	String escapedBody(String jsonPath) {
		return this.template.escapedBody(jsonPath)
	}

	@Override
	String body(String jsonPath) {
		return this.template.body(jsonPath)
	}

	@Override
	String escapedUrl() {
		return this.template.escapedUrl()
	}

	@Override
	String escapedQuery(String key) {
		return this.template.escapedQuery(key)
	}

	@Override
	String escapedQuery(String key, int index) {
		return this.template.escapedQuery(key, index)
	}

	@Override
	String escapedPath() {
		return this.template.escapedPath()
	}

	@Override
	String escapedPath(int index) {
		return this.template.escapedPath(index)
	}

	@Override
	String escapedHeader(String key) {
		return this.template.escapedHeader(key)
	}

	@Override
	String escapedHeader(String key, int index) {
		return this.template.escapedHeader(key, index)
	}

	@Override
	String escapedCookie(String key) {
		return this.template.escapedCookie(key)
	}
}
