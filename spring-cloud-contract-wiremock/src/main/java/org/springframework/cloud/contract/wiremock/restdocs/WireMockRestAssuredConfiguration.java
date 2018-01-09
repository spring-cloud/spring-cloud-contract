package org.springframework.cloud.contract.wiremock.restdocs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsRestAssuredConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.restdocs.restassured3.RestAssuredRestDocumentationConfigurer;

/**
 * Custom configuration for Spring RestDocs that adds a WireMock snippet (for generating
 * JSON stubs). Applied automatically if you use
 * {@link org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs @AutoConfigureRestDocs} in your test case and this class
 * is available. JSON stubs are generated and added to the restdocs path under "stubs".
 *
 * @see WireMockRestDocs for a convenient entry point for customizing and asserting the
 * stub behaviour
 *
 * @author Eddú Meléndez
 */
@Configuration
@ConditionalOnClass(RestAssuredRestDocumentationConfigurer.class)
public class WireMockRestAssuredConfiguration implements RestDocsRestAssuredConfigurationCustomizer {

	@Override
	public void customize(RestAssuredRestDocumentationConfigurer configurer) {
		configurer.snippets().withDefaults(new WireMockSnippet());
	}

}
