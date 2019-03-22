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

package org.springframework.cloud.contract.verifier.template

import java.util.regex.Matcher
import java.util.regex.Pattern

import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.WireMockHelpers
import groovy.transform.CompileStatic
import wiremock.com.github.jknack.handlebars.Handlebars
import wiremock.com.github.jknack.handlebars.Template

import org.springframework.cloud.contract.spec.ContractTemplate
import org.springframework.cloud.contract.spec.internal.CompositeContractTemplate
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.verifier.builder.TestSideRequestTemplateModel
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsJsonPathHelper

/**
 * Default Handlebars template processor
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class HandlebarsTemplateProcessor implements TemplateProcessor, ContractTemplate {

	private static final Pattern ESCAPED_LEGACY_JSON_PATH_PATTERN = Pattern.
		compile("^.*\\{\\{\\{jsonpath this '(.*)'}}}.*\$")
	private static final Pattern ESCAPED_JSON_PATH_PATTERN = Pattern.
		compile("^.*\\{\\{\\{jsonPath request.body '(.*)'}}}.*\$")
	private static final Pattern LEGACY_JSON_PATH_PATTERN = Pattern.
		compile("^.*\\{\\{jsonpath this '(.*)'}}.*\$")
	private static final Pattern JSON_PATH_PATTERN = Pattern.
		compile("^.*\\{\\{jsonPath request.body '(.*)'}}.*\$")
	private static final List<Pattern> PATTERNS = [ESCAPED_LEGACY_JSON_PATH_PATTERN,
												   ESCAPED_JSON_PATH_PATTERN, LEGACY_JSON_PATH_PATTERN, JSON_PATH_PATTERN]
	private static final String LEGACY_JSON_PATH_TEMPLATE_NAME = HandlebarsJsonPathHelper.NAME
	private static final String JSON_PATH_TEMPLATE_NAME = WireMockHelpers.jsonPath.name()

	final ContractTemplate contractTemplate = new CompositeContractTemplate()

	@Override
	String transform(Request request, String testContents) {
		TestSideRequestTemplateModel templateModel = TestSideRequestTemplateModel.
			from(request)
		Map<String, TestSideRequestTemplateModel> model = [(HandlebarsJsonPathHelper.REQUEST_MODEL_NAME): templateModel]
		Template bodyTemplate = uncheckedCompileTemplate(testContents)
		return templatedResponseBody(model, bodyTemplate)
	}

	@Override
	boolean containsTemplateEntry(String line) {
		return (line.contains(contractTemplate.openingTemplate())
			&& line.contains(contractTemplate.closingTemplate())) ||
			(line.contains(contractTemplate.escapedOpeningTemplate()) &&
				line.contains(contractTemplate.escapedClosingTemplate()))
	}

	@Override
	boolean containsJsonPathTemplateEntry(String line) {
		return line.contains(openingTemplate() + LEGACY_JSON_PATH_TEMPLATE_NAME) ||
			line.contains(openingTemplate() + JSON_PATH_TEMPLATE_NAME) ||
			line.contains(escapedOpeningTemplate() + LEGACY_JSON_PATH_TEMPLATE_NAME) ||
			line.contains(escapedOpeningTemplate() + JSON_PATH_TEMPLATE_NAME)
	}

	@Override
	String jsonPathFromTemplateEntry(String line) {
		if (!containsJsonPathTemplateEntry(line)) {
			return ""
		}
		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(line)
			if (matcher.matches()) {
				return matcher.group(1)
			}
		}
		return ""
	}

	private String templatedResponseBody(Map<String, TestSideRequestTemplateModel> model, Template bodyTemplate) {
		return uncheckedApplyTemplate(bodyTemplate, model)
	}

	private String uncheckedApplyTemplate(Template template, Object context) {
		try {
			return template.apply(context)
		}
		catch (IOException e) {
			throw new RuntimeException(e)
		}
	}

	private Template uncheckedCompileTemplate(String content) {
		try {
			Handlebars handlebars = new Handlebars()
			handlebars.
				registerHelper(HandlebarsJsonPathHelper.NAME, new HandlebarsJsonPathHelper())
			handlebars.registerHelper(WireMockHelpers.jsonPath.
				name(), new HandlebarsJsonPathHelper())
			WireMockHelpers.values()
				.findAll { it != WireMockHelpers.jsonPath }
				.each { WireMockHelpers helper ->
				handlebars.registerHelper(helper.name(), helper)
			}
			return handlebars.compileInline(content)
		}
		catch (IOException e) {
			throw new RuntimeException(e)
		}
	}

	@Override
	boolean startsWithTemplate(String text) {
		return this.contractTemplate.startsWithTemplate(text)
	}

	@Override
	boolean startsWithEscapedTemplate(String text) {
		return this.contractTemplate.startsWithEscapedTemplate(text)
	}

	@Override
	String openingTemplate() {
		return this.contractTemplate.openingTemplate()
	}

	@Override
	String closingTemplate() {
		return this.contractTemplate.closingTemplate()
	}

	@Override
	String escapedOpeningTemplate() {
		return this.contractTemplate.escapedOpeningTemplate()
	}

	@Override
	String escapedClosingTemplate() {
		return this.contractTemplate.escapedClosingTemplate()
	}

	@Override
	String url() {
		return this.contractTemplate.url()
	}

	@Override
	String query(String key) {
		return this.contractTemplate.query(key)
	}

	@Override
	String query(String key, int index) {
		return this.contractTemplate.query(key, index)
	}

	@Override
	String path() {
		return this.contractTemplate.path()
	}

	@Override
	String path(int index) {
		return this.contractTemplate.path(index)
	}

	@Override
	String header(String key) {
		return this.contractTemplate.header(key)
	}

	@Override
	String header(String key, int index) {
		return this.contractTemplate.header(key, index)
	}

	@Override
	String cookie(String key) {
		return this.contractTemplate.cookie(key)
	}

	@Override
	String body() {
		return this.contractTemplate.body()
	}

	@Override
	String body(String jsonPath) {
		return this.contractTemplate.body(jsonPath)
	}

	@Override
	String escapedUrl() {
		return this.contractTemplate.escapedUrl()
	}

	@Override
	String escapedQuery(String key) {
		return this.contractTemplate.escapedQuery(key)
	}

	@Override
	String escapedQuery(String key, int index) {
		return this.contractTemplate.escapedQuery(key, index)
	}

	@Override
	String escapedPath() {
		return this.contractTemplate.escapedPath()
	}

	@Override
	String escapedPath(int index) {
		return this.contractTemplate.escapedPath(index)
	}

	@Override
	String escapedHeader(String key) {
		return this.contractTemplate.escapedHeader(key)
	}

	@Override
	String escapedHeader(String key, int index) {
		return this.contractTemplate.escapedHeader(key, index)
	}

	@Override
	String escapedCookie(String key) {
		return this.contractTemplate.escapedCookie(key)
	}

	@Override
	String escapedBody() {
		return this.contractTemplate.escapedBody()
	}

	@Override
	String escapedBody(String jsonPath) {
		return this.contractTemplate.escapedBody(jsonPath)
	}
}

