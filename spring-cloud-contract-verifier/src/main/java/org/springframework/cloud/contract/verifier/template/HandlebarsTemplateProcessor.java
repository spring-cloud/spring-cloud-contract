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

package org.springframework.cloud.contract.verifier.template;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.WireMockHelpers;
import wiremock.com.github.jknack.handlebars.Handlebars;
import wiremock.com.github.jknack.handlebars.Template;

import org.springframework.cloud.contract.spec.ContractTemplate;
import org.springframework.cloud.contract.spec.internal.CompositeContractTemplate;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.builder.TestSideRequestTemplateModel;
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsJsonPathHelper;

/**
 * Default Handlebars template processor.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class HandlebarsTemplateProcessor implements TemplateProcessor, ContractTemplate {

	private static final Pattern ESCAPED_LEGACY_JSON_PATH_PATTERN = Pattern
			.compile("^.*\\{\\{\\{jsonpath this '(.*)'}}}.*$");

	private static final Pattern ESCAPED_JSON_PATH_PATTERN = Pattern
			.compile("^.*\\{\\{\\{jsonPath request.body '(.*)'}}}.*$");

	private static final Pattern LEGACY_JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{jsonpath this '(.*)'}}.*$");

	private static final Pattern JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{jsonPath request.body '(.*)'}}.*$");

	private static final List<Pattern> PATTERNS = Arrays.asList(ESCAPED_LEGACY_JSON_PATH_PATTERN,
			ESCAPED_JSON_PATH_PATTERN, LEGACY_JSON_PATH_PATTERN, JSON_PATH_PATTERN);

	private static final String LEGACY_JSON_PATH_TEMPLATE_NAME = HandlebarsJsonPathHelper.NAME;

	private static final String JSON_PATH_TEMPLATE_NAME = WireMockHelpers.jsonPath.name();

	private final ContractTemplate contractTemplate = new CompositeContractTemplate();

	@Override
	public String transform(Request request, String testContents) {
		TestSideRequestTemplateModel templateModel = TestSideRequestTemplateModel.from(request);
		Map<String, TestSideRequestTemplateModel> model = Collections
				.singletonMap(HandlebarsJsonPathHelper.REQUEST_MODEL_NAME, templateModel);
		Template bodyTemplate = uncheckedCompileTemplate(testContents);
		return templatedResponseBody(model, bodyTemplate);
	}

	@Override
	public boolean containsTemplateEntry(String line) {
		return (line.contains(contractTemplate.openingTemplate()) && line.contains(contractTemplate.closingTemplate()))
				|| (line.contains(contractTemplate.escapedOpeningTemplate())
						&& line.contains(contractTemplate.escapedClosingTemplate()));
	}

	@Override
	public boolean containsJsonPathTemplateEntry(String line) {
		return line.contains(openingTemplate() + LEGACY_JSON_PATH_TEMPLATE_NAME)
				|| line.contains(openingTemplate() + JSON_PATH_TEMPLATE_NAME)
				|| line.contains(escapedOpeningTemplate() + LEGACY_JSON_PATH_TEMPLATE_NAME)
				|| line.contains(escapedOpeningTemplate() + JSON_PATH_TEMPLATE_NAME);
	}

	@Override
	public String jsonPathFromTemplateEntry(String line) {
		if (!containsJsonPathTemplateEntry(line)) {
			return "";
		}
		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}
		return "";
	}

	private String templatedResponseBody(Map<String, TestSideRequestTemplateModel> model, Template bodyTemplate) {
		return uncheckedApplyTemplate(bodyTemplate, model);
	}

	private String uncheckedApplyTemplate(Template template, Object context) {
		try {
			return template.apply(context);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Template uncheckedCompileTemplate(String content) {
		try {
			final Handlebars handlebars = new Handlebars();
			handlebars.registerHelper(HandlebarsJsonPathHelper.NAME, new HandlebarsJsonPathHelper());
			handlebars.registerHelper(WireMockHelpers.jsonPath.name(), new HandlebarsJsonPathHelper());
			Arrays.stream(WireMockHelpers.values()).filter(helper -> !helper.equals(WireMockHelpers.jsonPath))
					.forEach(helper -> handlebars.registerHelper(helper.name(), helper));
			return handlebars.compileInline(content);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean startsWithTemplate(String text) {
		return contractTemplate.startsWithTemplate(text);
	}

	@Override
	public boolean startsWithEscapedTemplate(String text) {
		return contractTemplate.startsWithEscapedTemplate(text);
	}

	@Override
	public String openingTemplate() {
		return contractTemplate.openingTemplate();
	}

	@Override
	public String closingTemplate() {
		return contractTemplate.closingTemplate();
	}

	@Override
	public String escapedOpeningTemplate() {
		return contractTemplate.escapedOpeningTemplate();
	}

	@Override
	public String escapedClosingTemplate() {
		return contractTemplate.escapedClosingTemplate();
	}

	@Override
	public String url() {
		return contractTemplate.url();
	}

	@Override
	public String query(String key) {
		return contractTemplate.query(key);
	}

	@Override
	public String query(String key, int index) {
		return contractTemplate.query(key, index);
	}

	@Override
	public String path() {
		return contractTemplate.path();
	}

	@Override
	public String path(int index) {
		return contractTemplate.path(index);
	}

	@Override
	public String header(String key) {
		return contractTemplate.header(key);
	}

	@Override
	public String header(String key, int index) {
		return contractTemplate.header(key, index);
	}

	@Override
	public String cookie(String key) {
		return contractTemplate.cookie(key);
	}

	@Override
	public String body() {
		return contractTemplate.body();
	}

	@Override
	public String body(String jsonPath) {
		return contractTemplate.body(jsonPath);
	}

	@Override
	public String escapedUrl() {
		return contractTemplate.escapedUrl();
	}

	@Override
	public String escapedQuery(String key) {
		return contractTemplate.escapedQuery(key);
	}

	@Override
	public String escapedQuery(String key, int index) {
		return contractTemplate.escapedQuery(key, index);
	}

	@Override
	public String escapedPath() {
		return contractTemplate.escapedPath();
	}

	@Override
	public String escapedPath(int index) {
		return contractTemplate.escapedPath(index);
	}

	@Override
	public String escapedHeader(String key) {
		return contractTemplate.escapedHeader(key);
	}

	@Override
	public String escapedHeader(String key, int index) {
		return contractTemplate.escapedHeader(key, index);
	}

	@Override
	public String escapedCookie(String key) {
		return contractTemplate.escapedCookie(key);
	}

	@Override
	public String escapedBody() {
		return contractTemplate.escapedBody();
	}

	@Override
	public String escapedBody(String jsonPath) {
		return contractTemplate.escapedBody(jsonPath);
	}

}
