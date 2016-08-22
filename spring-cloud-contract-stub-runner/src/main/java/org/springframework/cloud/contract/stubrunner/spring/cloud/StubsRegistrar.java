package org.springframework.cloud.contract.stubrunner.spring.cloud;

/**
 * Contract for registering stubs in a Service Discovery.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
public interface StubsRegistrar {
	void registerStubs();
}