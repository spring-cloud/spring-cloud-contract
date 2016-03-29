package io.codearte.accurest.stubrunner.spring.cloud;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

/**
 * {@link ServiceInstance} with a helpful constructor
 *
 * @author Marcin Grzejszczak
 */
public class StubRunnerServiceInstance implements ServiceInstance {

	private final String serviceId;
	private final String host;
	private final int port;
	private final URI uri;

	public StubRunnerServiceInstance(String serviceId, String host, int port, URI uri) {
		this.serviceId = serviceId;
		this.host = host;
		this.port = port;
		this.uri = uri;
	}

	@Override
	public String getServiceId() {
		return serviceId;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public Map<String, String> getMetadata() {
		return new HashMap<>();
	}
}
