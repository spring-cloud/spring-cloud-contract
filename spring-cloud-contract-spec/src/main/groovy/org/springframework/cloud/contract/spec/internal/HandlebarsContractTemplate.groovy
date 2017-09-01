package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.ContractTemplate

/**
 * Represents the structure of templates using Handlebars compatible with
 * WireMock template model requirements.
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
		return query(key, 0)
	}

	@Override
	String query(String key, int index) {
		return wrapped("request.query.${key}.[${index}]")
	}

	@Override
	String path() {
		return wrapped("request.path")
	}

	@Override
	String path(int index) {
		return wrapped("request.path.[${index}]")
	}

	@Override
	String header(String key) {
		return header(key, 0)
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
	String escapedBody() {
		return wrapped("escapejsonbody")
	}

	@Override
	String body(String jsonPath) {
		return wrapped("jsonpath this '${jsonPath}'")
	}

	private String wrapped(String text) {
		return openingTemplate() + text + closingTemplate()
	}
}
