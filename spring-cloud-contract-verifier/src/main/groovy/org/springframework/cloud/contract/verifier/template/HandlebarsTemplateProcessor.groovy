package org.springframework.cloud.contract.verifier.template

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import wiremock.com.github.jknack.handlebars.Handlebars
import wiremock.com.github.jknack.handlebars.Template
import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.ContractTemplate
import org.springframework.cloud.contract.spec.internal.HandlebarsContractTemplate
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsJsonPathHelper
import org.springframework.cloud.contract.verifier.builder.TestSideRequestTemplateModel

import java.util.regex.Matcher
import java.util.regex.Pattern
/**
 * Default Handlebars template processor
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class HandlebarsTemplateProcessor implements TemplateProcessor, ContractTemplate {

	private static final Log log = LogFactory.getLog(HandlebarsTemplateProcessor)

	private static final Pattern JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{\\{jsonpath this '(.*)'}}}.*\$")
	private static final Pattern WIREMOCK_JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{jsonPath request.body '(.*)'}}.*\$")
	private static final Pattern WIREMOCK_REPLACE_JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{jsonPath request\\.body &amp;apos;.*?&amp;apos;}}.*\$")

	@Delegate
	private final ContractTemplate contractTemplate = new HandlebarsContractTemplate()

	@Override
	String transform(Request request, String testContents) {
		TestSideRequestTemplateModel templateModel = TestSideRequestTemplateModel.from(request)
		Map<String, TestSideRequestTemplateModel> model = [(HandlebarsJsonPathHelper.REQUEST_MODEL_NAME): templateModel]
		Template bodyTemplate = uncheckedCompileTemplate(testContents)
		return templatedResponseBody(model, bodyTemplate)
	}

	@Override
	boolean containsTemplateEntry(String line) {
		return line.matches('^.*\\{\\{\\{.*}}}.*$')
	}

	@Override
	boolean containsJsonPathTemplateEntry(String line) {
		return line.contains(openingTemplate() + HandlebarsJsonPathHelper.NAME)
	}

	@Override
	String jsonPathFromTemplateEntry(String line) {
		if (!containsJsonPathTemplateEntry(line)) {
			return ""
		}
		Matcher matcher = JSON_PATH_PATTERN.matcher(line)
		boolean scContractMatches = matcher.matches()
		Matcher wireMockMatcher = WIREMOCK_JSON_PATH_PATTERN.matcher(line)
		boolean wireMockMatches = wireMockMatcher.matches()
		if (!scContractMatches && !wireMockMatches) {
			return ""
		}
		return scContractMatches ? matcher.group(1) : wireMockMatcher.group(1)
	}

	private String templatedResponseBody(Map<String, TestSideRequestTemplateModel> model, Template bodyTemplate) {
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
			String contentInOneLine = content.replace(System.getProperty("line.separator"), "")
			Matcher matcher = JSON_PATH_PATTERN.matcher(contentInOneLine)
			boolean scContractMatches = matcher.matches()
			if (scContractMatches) {
				if (log.isDebugEnabled()) {
					log.debug("Found the Spring Cloud Contract jsonpath format. Will apply the Handlebars helper")
				}
				handlebars.registerHelper(HandlebarsJsonPathHelper.NAME, new HandlebarsJsonPathHelper())
				return handlebars.compileInline(content)
			}
			Matcher wireMockMatcher = WIREMOCK_REPLACE_JSON_PATH_PATTERN.matcher(contentInOneLine)
			boolean wireMockMatches = wireMockMatcher.matches()
			StringBuffer sb = new StringBuffer(content.length())
			if (wireMockMatches) {
				content.eachLine {
					Matcher lineMatcher = WIREMOCK_REPLACE_JSON_PATH_PATTERN.matcher(it)
					while (lineMatcher.find()) {
						String group = lineMatcher.group()
						String changedGroup = group.replaceAll("&amp;apos;", "'")
						lineMatcher.appendReplacement(sb, Matcher.quoteReplacement(changedGroup))
					}
				}
				handlebars.registerHelper("jsonPath", new com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsJsonPathHelper())
				content = sb.toString()
			}
			return handlebars.compileInline(content)
		} catch (IOException e) {
			throw new RuntimeException(e)
		}
	}
}

