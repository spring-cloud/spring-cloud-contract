package org.springframework.cloud.contract.wiremock;

/**
 * Allows customization of {@link com.github.tomakehurst.wiremock.core.WireMockConfiguration}
 *
 * @author Marcin Grzejszczak
 * @since 1.2.2
 */
public interface WireMockConfigurationCustomizer {

	void customize(com.github.tomakehurst.wiremock.core.WireMockConfiguration config);
}
