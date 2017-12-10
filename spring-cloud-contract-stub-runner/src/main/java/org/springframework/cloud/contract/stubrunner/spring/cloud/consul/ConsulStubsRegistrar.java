package org.springframework.cloud.contract.stubrunner.spring.cloud.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubRunning;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubsRegistrar;
import org.springframework.util.StringUtils;

/**
 * Registers all stubs in Zookeeper Service Discovery
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
public class ConsulStubsRegistrar implements StubsRegistrar {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final StubRunning stubRunning;
	private final ConsulClient consulClient;
	private final StubMapperProperties stubMapperProperties;
	private final ConsulDiscoveryProperties consulDiscoveryProperties;
	private final InetUtils inetUtils;
	private final List<NewService> services = new LinkedList<>();

	public ConsulStubsRegistrar(StubRunning stubRunning, ConsulClient consulClient,
		StubMapperProperties stubMapperProperties,
		ConsulDiscoveryProperties consulDiscoveryProperties,
		InetUtils inetUtils) {
		this.stubRunning = stubRunning;
		this.consulClient = consulClient;
		this.stubMapperProperties = stubMapperProperties;
		this.consulDiscoveryProperties = consulDiscoveryProperties;
		this.inetUtils = inetUtils;
	}

	@Override public void registerStubs() {
		Map<StubConfiguration, Integer> activeStubs = this.stubRunning.runStubs()
				.validNamesAndPorts();
		for (Map.Entry<StubConfiguration, Integer> entry : activeStubs.entrySet()) {
			NewService newService = newService(entry.getKey(), entry.getValue());
			this.services.add(newService);
			try {
				this.consulClient.agentServiceRegister(newService);
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

	protected NewService newService(StubConfiguration stubConfiguration, Integer port) {
		NewService newService = new NewService();
		newService.setAddress(StringUtils.hasText(this.consulDiscoveryProperties.getHostname()) ?
			this.consulDiscoveryProperties.getHostname() :
			this.inetUtils.findFirstNonLoopbackAddress().getHostName());
		newService.setId(stubConfiguration.getArtifactId());
		newService.setName(name(stubConfiguration));
		newService.setPort(port);
		return newService;
	}

	protected String name(StubConfiguration stubConfiguration) {
		String resolvedName = this.stubMapperProperties.fromIvyNotationToId(
				stubConfiguration.toColonSeparatedDependencyNotation());
		if (StringUtils.hasText(resolvedName)) {
			return resolvedName;
		}
		return stubConfiguration.getArtifactId();
	}

	@Override
	public void close() throws Exception {
		for (NewService service : this.services) {
			this.consulClient.agentServiceDeregister(service.getId());
		}
	}
}
