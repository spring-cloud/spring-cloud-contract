package org.springframework.cloud.contract.stubrunner.provider.wiremock

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Extension
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.ChunkedDribbleDelay
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response

import org.springframework.cloud.contract.verifier.dsl.wiremock.DefaultResponseTransformer
import org.springframework.cloud.contract.verifier.dsl.wiremock.WireMockExtensions
/**
 * Extension that registers the default response transformer and a custom one too
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

class CustomExtension extends ResponseTransformer {

	/**
	 * We expect the mapping to contain "foo-transformer" in the list
	 * of "response-transformers" in the stub mapping
	 */
	@Override
	String getName() {
		return "foo-transformer"
	}

	/**
	 * Transformer returns the "surprise!" body regardless of what you
	 * the stub mapping returns
	 */
	@Override
	Response transform(Request request, Response response, FileSource files, Parameters parameters) {
		return new Response(response.status, response.statusMessage,
				"surprise!", response.headers, response.wasConfigured(), response.fault,
				new ChunkedDribbleDelay(0, 0), response.fromProxy)
	}

	/**
	 * We don't want this extension to be applied to every single mapping.
	 * We just want this to take place when a mapping explicitly expresses that in the
	 * "response-transformers" section
	 */
	@Override
	boolean applyGlobally() {
		return false
	}
}
