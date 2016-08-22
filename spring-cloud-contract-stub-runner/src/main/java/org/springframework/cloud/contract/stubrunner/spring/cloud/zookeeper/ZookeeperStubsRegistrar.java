package org.springframework.cloud.contract.stubrunner.spring.cloud.zookeeper;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubRunning;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubsRegistrar;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.util.StringUtils;

/**
 * Registers all stubs in Zookeeper Service Discovery
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
public class ZookeeperStubsRegistrar implements AutoCloseable, StubsRegistrar {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final StubRunning stubRunning;
	private final CuratorFramework curatorFramework;
	private final StubMapperProperties stubMapperProperties;
	private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;
	private final List<ServiceDiscovery> discoveryList = new LinkedList<>();

	protected ZookeeperStubsRegistrar(StubRunning stubRunning, CuratorFramework curatorFramework,
			StubMapperProperties stubMapperProperties,
			ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
		this.stubRunning = stubRunning;
		this.curatorFramework = curatorFramework;
		this.stubMapperProperties = stubMapperProperties;
		this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
	}

	@Override public void registerStubs() {
		Map<StubConfiguration, Integer> activeStubs = this.stubRunning.runStubs()
				.validNamesAndPorts();
		for (Map.Entry<StubConfiguration, Integer> entry : activeStubs.entrySet()) {
			ServiceInstance serviceInstance = serviceInstance(entry.getKey(), entry.getValue());
			ServiceDiscovery serviceDiscovery = serviceDiscovery(serviceInstance);
			this.discoveryList.add(serviceDiscovery);
			try {
				serviceDiscovery.start();
				if (log.isDebugEnabled()) {
					log.debug("Successfully registered stub [" + entry.getKey().toColonSeparatedDependencyNotation()
							+ "] in Service Discovery");
				}
			}
			catch (Exception e) {
				log.warn("Exception occurred while trying to register a stub [" + entry.getKey().toColonSeparatedDependencyNotation()
						+ "] in Service Discovery", e);
			}
		}
	}

	protected ServiceInstance serviceInstance(StubConfiguration stubConfiguration, int port) {
		try {
			return ServiceInstance.builder().uriSpec(new UriSpec(this.zookeeperDiscoveryProperties.getUriSpec()))
					.address("localhost").port(port).name(name(stubConfiguration))
					.build();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private String name(StubConfiguration stubConfiguration) {
		String resolvedName = this.stubMapperProperties.fromIvyNotationToId(
				stubConfiguration.toColonSeparatedDependencyNotation());
		if (StringUtils.hasText(resolvedName)) {
			return resolvedName;
		}
		return stubConfiguration.getArtifactId();
	}

	protected ServiceDiscovery serviceDiscovery(ServiceInstance serviceInstance) {
		return ServiceDiscoveryBuilder.builder(Void.class)
				.basePath(this.zookeeperDiscoveryProperties.getRoot())
				.client(this.curatorFramework).thisInstance(serviceInstance).build();
	}

	@Override
	public void close() throws Exception {
		for (ServiceDiscovery discovery : this.discoveryList) {
			discovery.close();
		}
	}
}
