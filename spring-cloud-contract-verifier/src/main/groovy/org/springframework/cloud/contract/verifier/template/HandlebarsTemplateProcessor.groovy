package org.springframework.cloud.contract.verifier.template

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
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

	private static final Pattern JSON_PATH_PATTERN = Pattern.compile("^.*\\{\\{\\{jsonpath this '(.*)'}}}.*\$")

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
		if (!matcher.matches()) {
			return ""
		}
		return matcher.group(1)
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
			return handlebars.compileInline(content)
		} catch (IOException e) {
			throw new RuntimeException(e)
		}
	}
}

