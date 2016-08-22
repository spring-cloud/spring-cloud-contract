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

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

/**
 * Registers all stubs in Eureka Service Discovery
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
public class EurekaStubsRegistrar implements AutoCloseable, StubsRegistrar {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final StubRunning stubRunning;
	private final EurekaClient eurekaClient;
	private final StubMapperProperties stubMapperProperties;
	private final InetUtils inetUtils;
	private final List<ApplicationInfoManager> discoveryList = new LinkedList<>();

	protected EurekaStubsRegistrar(StubRunning stubRunning, EurekaClient eurekaClient,
			StubMapperProperties stubMapperProperties, InetUtils inetUtils) {
		this.stubRunning = stubRunning;
		this.eurekaClient = eurekaClient;
		this.stubMapperProperties = stubMapperProperties;
		this.inetUtils = inetUtils;
	}

	@Override public void registerStubs() {
		Map<StubConfiguration, Integer> activeStubs = this.stubRunning.runStubs()
				.validNamesAndPorts();
		for (Map.Entry<StubConfiguration, Integer> entry : activeStubs.entrySet()) {
			EurekaInstanceConfigBean bean = new EurekaInstanceConfigBean(inetUtils);
			bean.setNonSecurePort(entry.getValue());
			bean.setHostname(name(entry.getKey()));
			try {
				ApplicationInfoManager manager = new ApplicationInfoManager(bean);
				this.discoveryList.add(manager);
				manager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
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
		for (ApplicationInfoManager discovery : this.discoveryList) {
			discovery.setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
		}
		this.eurekaClient.shutdown();
	}
}
