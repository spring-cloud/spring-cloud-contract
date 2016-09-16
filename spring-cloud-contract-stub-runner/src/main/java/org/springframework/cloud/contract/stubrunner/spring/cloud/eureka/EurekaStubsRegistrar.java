package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubRunning;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubsRegistrar;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.util.StringUtils;

/**
 * Registers all stubs in Eureka Service Discovery
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
public class EurekaStubsRegistrar implements StubsRegistrar {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final StubRunning stubRunning;
	private final Eureka eurekaClient;
	private final StubMapperProperties stubMapperProperties;
	private final InetUtils inetUtils;
	private final EurekaInstanceConfigBean eurekaInstanceConfigBean;
	private final List<Renewer> discoveryList = new LinkedList<>();

	public EurekaStubsRegistrar(StubRunning stubRunning, Eureka eureka,
			StubMapperProperties stubMapperProperties, InetUtils inetUtils,
			EurekaInstanceConfigBean eurekaInstanceConfigBean) {
		this.stubRunning = stubRunning;
		this.stubMapperProperties = stubMapperProperties;
		this.eurekaClient = eureka;
		this.inetUtils = inetUtils;
		this.eurekaInstanceConfigBean = eurekaInstanceConfigBean;
	}

	@Override public void registerStubs() {
		Map<StubConfiguration, Integer> activeStubs = this.stubRunning.runStubs()
				.validNamesAndPorts();
		for (Map.Entry<StubConfiguration, Integer> entry : activeStubs.entrySet()) {
			Application application = new Application(name(entry.getKey()), entry.getKey().getArtifactId(),
					StringUtils.hasText(this.eurekaInstanceConfigBean.getHostname()) ?
							this.eurekaInstanceConfigBean.getHostname() :
							this.inetUtils.findFirstNonLoopbackAddress().getHostName(), entry.getValue());
			try {
				Registration register = this.eurekaClient.register(application);
				this.discoveryList.add(new Renewer(
						this.eurekaClient.clientConfig.getInstanceInfoReplicationIntervalSeconds() / 2, this.eurekaClient, register));
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

	private String name(StubConfiguration stubConfiguration) {
		String resolvedName = this.stubMapperProperties.fromIvyNotationToId(
				stubConfiguration.toColonSeparatedDependencyNotation());
		if (StringUtils.hasText(resolvedName)) {
			return resolvedName;
		}
		return stubConfiguration.getArtifactId();
	}

	@Override
	public void close() throws Exception {
		for (Renewer renewer : this.discoveryList) {
			this.eurekaClient.shutdown(renewer.registration);
			renewer.scheduler.shutdown();
		}
	}
}
