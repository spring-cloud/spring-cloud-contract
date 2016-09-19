package org.springframework.cloud.contract.stubrunner.spring.cloud;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration;
import org.springframework.cloud.contract.stubrunner.spring.cloud.ribbon.StubRunnerRibbonAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Autoconfiguration available in Cloud profile. Stubs out the discovery client communication.
 * Imports only HTTP related classes.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
@Configuration
@Profile("cloud")
@ConditionalOnProperty(value = "stubrunner.cloudfoundry.enabled", matchIfMissing = true)
@Import({ StubRunnerConfiguration.class, StubRunnerSpringCloudAutoConfiguration.class, StubRunnerRibbonAutoConfiguration.class })
public class StubRunnerCloudFoundryAutoConfiguration {
}
