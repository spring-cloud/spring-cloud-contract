package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.ContractTemplate

/**
 * Represents the structure of templates using Handlebars
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class HandlebarsContractTemplate implements ContractTemplate {

	@Override
	String openingTemplate() {
		return "{{{"
	}

	@Override
	String closingTemplate() {
		return "}}}"
	}

	@Override
	String url() {
		return wrapped("request.url")
	}

	@Override
	String query(String key) {
		return wrapped("request.query.${key}.[0]")
	}

	@Override
	String query(String key, int index) {
		return wrapped("request.query.${key}.[${index}]")
	}

	@Override
	String header(String key) {
		return wrapped("request.headers.${key}.[0]")
	}

	@Override
	String header(String key, int index) {
		return wrapped("request.headers.${key}.[${index}]")
	}

	@Override
	String body() {
		return wrapped("request.body")
	}

	@Override
	String body(String jsonPath) {
		return wrapped("jsonpath this '${jsonPath}'")
	}

	private String wrapped(String text) {
		return openingTemplate() + text + closingTemplate()
	}
}
