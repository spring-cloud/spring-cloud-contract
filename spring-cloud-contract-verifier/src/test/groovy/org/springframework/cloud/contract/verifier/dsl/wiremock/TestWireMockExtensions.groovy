package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.extension.Extension

/**
 * Extension that registers the default transformer and the custom one
 */
class TestWireMockExtensions implements WireMockExtensions {
	@Override
	List<Extension> extensions() {
		return [
				new DefaultResponseTransformer(),
				new CustomExtension()
		]
	}
}

class CustomExtension implements Extension {

	@Override
	String getName() {
		return "foo-transformer"
	}
}
