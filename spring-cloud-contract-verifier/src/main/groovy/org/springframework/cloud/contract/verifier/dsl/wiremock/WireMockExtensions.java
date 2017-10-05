package org.springframework.cloud.contract.verifier.dsl.wiremock;

import java.util.List;

import com.github.tomakehurst.wiremock.extension.Extension;

/**
 * Contract that describes a list of {@link Extension} extensions
 * that should be applied to the response
 *
 * @author Marcin Grzejszczak
 * @since 1.2.0
 */
public interface WireMockExtensions {
	List<Extension> extensions();
}
