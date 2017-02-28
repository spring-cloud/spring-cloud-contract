package org.springframework.cloud.contract.verifier.builder

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.ContractTemplate
import org.springframework.cloud.contract.spec.internal.HandlebarsContractTemplate
import org.springframework.cloud.contract.spec.internal.Request

/**
 * Default Handlebars template processor
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
@PackageScope
class HandlebarsTemplateProcessor implements TemplateProcessor, ContractTemplate {

	@Delegate private final ContractTemplate contractTemplate = new HandlebarsContractTemplate()

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

