package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import wiremock.com.github.jknack.handlebars.Helper

import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsEscapeHelper
import org.springframework.cloud.contract.verifier.builder.handlebars.HandlebarsJsonPathHelper

/**
 * Default implementation of {@link ResponseTemplateTransformer} that contains
 * default set of handlebars helpers
 *
 * @author Marcin Grzejszczak
 * @since 1.2.0
 */
class DefaultResponseTransformer extends ResponseTemplateTransformer {
	DefaultResponseTransformer() {
		super(false, defaultHelpers())
	}

	DefaultResponseTransformer(boolean global) {
		super(global)
	}

	DefaultResponseTransformer(boolean global, String helperName, Helper helper) {
		super(global, helperName, helper)
	}

	DefaultResponseTransformer(boolean global, Map<String, Helper> helpers) {
		super(global, helpers)
	}

	private static Map<String, Helper> defaultHelpers() {
		Map<String, Helper> helpers = new HashMap<>()
		helpers.put(HandlebarsJsonPathHelper.NAME, new HandlebarsJsonPathHelper())
		helpers.put(HandlebarsEscapeHelper.NAME, new HandlebarsEscapeHelper())
		return helpers
	}
}
