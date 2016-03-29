package io.codearte.accurest.stubrunner.spring.cloud;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.codearte.accurest.stubrunner.RunningStubs;
import io.codearte.accurest.stubrunner.StubFinder;
import io.codearte.accurest.stubrunner.util.StringUtils;

/**
 * Custom version of {@link DiscoveryClient} that tries to find an instance
 * in one of the started WireMock servers
 *
 * @author Marcin Grzejszczak
 */
public class StubRunnerDiscoveryClient implements DiscoveryClient {

	private final DiscoveryClient delegate;
	private final StubFinder stubFinder;
	private final StubMapperProperties stubMapperProperties;

	public StubRunnerDiscoveryClient(DiscoveryClient delegate, StubFinder stubFinder,
			StubMapperProperties stubMapperProperties) {
		this.delegate = delegate;
		this.stubFinder = stubFinder;
		this.stubMapperProperties = stubMapperProperties;
	}

	@Override
	public String description() {
		return delegate.description();
	}

	@Override
	public ServiceInstance getLocalServiceInstance() {
		return delegate.getLocalServiceInstance();
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		String ivyNotation = stubMapperProperties.fromServiceIdToIvyNotation(serviceId);
		String serviceToFind = StringUtils.hasText(ivyNotation) ? ivyNotation : serviceId;
		URL stubUrl = stubFinder.findStubUrl(serviceToFind);
		if (stubUrl == null) {
			return delegate.getInstances(serviceId);
		}
		return Collections.<ServiceInstance>singletonList(
				new StubRunnerServiceInstance(serviceId, stubUrl.getHost(), stubUrl.getPort(), toUri(stubUrl))
		);
	}

	private URI toUri(URL url) {
		try {
			return url.toURI();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<String> getServices() {
		List<String> services = delegate.getServices();
		RunningStubs runningStubs = stubFinder.findAllRunningStubs();
		services.addAll(runningStubs.getAllServicesNames());
		return services;
	}
}
