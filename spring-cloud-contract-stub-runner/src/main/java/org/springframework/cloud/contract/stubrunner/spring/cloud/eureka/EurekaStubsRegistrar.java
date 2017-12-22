package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubRunning;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubsRegistrar;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.InstanceInfoFactory;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.EurekaClient;

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
	private final StubMapperProperties stubMapperProperties;
	private final InetUtils inetUtils;
	private final EurekaInstanceConfigBean eurekaInstanceConfigBean;
	private final EurekaClientConfigBean eurekaClientConfigBean;
	private final List<EurekaRegistration> registrations = new LinkedList<>();
	private final ServiceRegistry<EurekaRegistration> serviceRegistry;
	private final ApplicationContext context;

	public EurekaStubsRegistrar(StubRunning stubRunning,
			ServiceRegistry<EurekaRegistration> serviceRegistry,
			StubMapperProperties stubMapperProperties, InetUtils inetUtils,
			EurekaInstanceConfigBean eurekaInstanceConfigBean,
			EurekaClientConfigBean eurekaClientConfigBean,
			ApplicationContext context) {
		this.stubRunning = stubRunning;
		this.stubMapperProperties = stubMapperProperties;
		this.serviceRegistry = serviceRegistry;
		this.inetUtils = inetUtils;
		this.eurekaInstanceConfigBean = eurekaInstanceConfigBean;
		this.eurekaClientConfigBean = eurekaClientConfigBean;
		this.context = context;
	}

	@Override public void registerStubs() {
		Map<StubConfiguration, Integer> activeStubs = this.stubRunning.runStubs()
				.validNamesAndPorts();
		for (Map.Entry<StubConfiguration, Integer> entry : activeStubs.entrySet()) {
			EurekaInstanceConfigBean instance = registration(entry);
			log.info("Will register stub in Eureka " + "[" + instance.getAppname() + ", "
					+ instance.getHostname() + ", " + instance.getNonSecurePort() + ", "
					+ instance.getInstanceId() + "]");
			InstanceInfo instanceInfo = new InstanceInfoFactory().create(instance);
			ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(instance, instanceInfo);
			AbstractDiscoveryClientOptionalArgs args = args();
			EurekaClient client = new CloudEurekaClient(applicationInfoManager, this.eurekaClientConfigBean, args, this.context);
			EurekaRegistration registration = EurekaRegistration.builder(instance)
					.with(this.eurekaClientConfigBean, this.context)
					.with(client)
					.build();
			this.registrations.add(registration);
			try {
				this.serviceRegistry.register(registration);
				log.info("Successfully registered stub " + "[" + entry.getKey()
						.toColonSeparatedDependencyNotation() + "] in Service Discovery");
			}
			catch (Exception e) {
				log.warn("Exception occurred while trying to register a stub [" + entry.getKey().toColonSeparatedDependencyNotation()
						+ "] in Service Discovery", e);
			}
		}
	}

	private AbstractDiscoveryClientOptionalArgs args() {
		try {
			return this.context
					.getBean(AbstractDiscoveryClientOptionalArgs.class);
		} catch (BeansException e) {
			return null;
		}
	}

	private EurekaInstanceConfigBean registration(Map.Entry<StubConfiguration, Integer> entry) {
		EurekaInstanceConfigBean config = new EurekaInstanceConfigBean(this.inetUtils);
		String appName = name(entry.getKey());
		config.setInstanceEnabledOnit(true);
		InetAddress address = this.inetUtils.findFirstNonLoopbackAddress();
		config.setIpAddress(address.getHostAddress());
		config.setHostname(StringUtils.hasText(hostName(entry)) ?
				hostName(entry) : address.getHostName());
		config.setAppname(appName);
		config.setVirtualHostName(appName);
		config.setSecureVirtualHostName(appName);
		int port = port(entry);
		config.setNonSecurePort(port);
		config.setInstanceId(address.getHostAddress() + ":" + entry.getKey().getArtifactId() + ":" + port);
		config.setLeaseRenewalIntervalInSeconds(1);
		return config;
	}

	protected String hostName(Map.Entry<StubConfiguration, Integer> entry) {
		return this.eurekaInstanceConfigBean.getHostname();
	}

	protected int port(Map.Entry<StubConfiguration, Integer> entry) {
		return entry.getValue();
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
		for (EurekaRegistration registration : this.registrations) {
			this.serviceRegistry.deregister(registration);
			registration.close();
		}
	}
}
