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

	private static final Pattern ESCAPED_LEGACY_JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{\\{jsonpath this '(.*)'}}}.*\$")
	private static final Pattern ESCAPED_JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{\\{jsonPath request.body '(.*)'}}}.*\$")
	private static final Pattern LEGACY_JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{jsonpath this '(.*)'}}.*\$")
	private static final Pattern JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{jsonPath request.body '(.*)'}}.*\$")
	private static final List<Pattern> PATTERNS = [ESCAPED_LEGACY_JSON_PATH_PATTERN,
							  ESCAPED_JSON_PATH_PATTERN, LEGACY_JSON_PATH_PATTERN, JSON_PATH_PATTERN]
	private static final String LEGACY_JSON_PATH_TEMPLATE_NAME = HandlebarsJsonPathHelper.NAME
	private static final String JSON_PATH_TEMPLATE_NAME = WireMockHelpers.jsonPath.name()

	@Delegate
	private final ContractTemplate contractTemplate = new CompositeContractTemplate()

	@Override
	String transform(Request request, String testContents) {
		TestSideRequestTemplateModel templateModel = TestSideRequestTemplateModel.from(request)
		Map<String, TestSideRequestTemplateModel> model = [(HandlebarsJsonPathHelper.REQUEST_MODEL_NAME): templateModel]
		Template bodyTemplate = uncheckedCompileTemplate(testContents)
		return templatedResponseBody(model, bodyTemplate)
	}

	@Override
	boolean containsTemplateEntry(String line) {
		return (line.contains(contractTemplate.openingTemplate()) && line.contains(contractTemplate.closingTemplate())) ||
				(line.contains(contractTemplate.escapedOpeningTemplate()) && line.contains(contractTemplate.escapedClosingTemplate()))
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

	private String templatedResponseBody(Map< String, TestSideRequestTemplateModel> model, Template bodyTemplate) {
		return uncheckedApplyTemplate(bodyTemplate, model)
	}

	private String uncheckedApplyTemplate(Template template, Object context) {
		try {
			return template.apply(context)
		} catch (IOException e) {
			throw new RuntimeException(e)
		}
	}

	private Template uncheckedCompileTemplate(String content) {
		try {
			Handlebars handlebars = new Handlebars()
			handlebars.registerHelper(HandlebarsJsonPathHelper.NAME, new HandlebarsJsonPathHelper())
			handlebars.registerHelper(WireMockHelpers.jsonPath.name(), new HandlebarsJsonPathHelper())
			WireMockHelpers.values()
					.findAll { it != WireMockHelpers.jsonPath}
					.each { WireMockHelpers helper ->
				handlebars.registerHelper(helper.name(), helper)
			}
			return handlebars.compileInline(content)
		} catch (IOException e) {
			throw new RuntimeException(e)
		}
	}
}

