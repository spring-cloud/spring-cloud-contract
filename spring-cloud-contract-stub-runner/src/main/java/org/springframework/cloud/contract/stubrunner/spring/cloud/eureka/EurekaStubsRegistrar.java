package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubRunning;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubsRegistrar;
import org.springframework.util.StringUtils;

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
	private final Eureka eurekaClient;
	private final StubMapperProperties stubMapperProperties;
	private final List<Registration> discoveryList = new LinkedList<>();

	protected EurekaStubsRegistrar(StubRunning stubRunning, Eureka eureka,
			StubMapperProperties stubMapperProperties) {
		this.stubRunning = stubRunning;
		this.stubMapperProperties = stubMapperProperties;
		this.eurekaClient = eureka;
	}

	@Override public void registerStubs() {
		Map<StubConfiguration, Integer> activeStubs = this.stubRunning.runStubs()
				.validNamesAndPorts();
		for (Map.Entry<StubConfiguration, Integer> entry : activeStubs.entrySet()) {
			Application application = new Application(name(entry.getKey()), name(entry.getKey()), "localhost", entry.getValue());
			try {
				Registration register = this.eurekaClient.register(application);
				this.discoveryList.add(register);
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
		for (Registration registration : this.discoveryList) {
			this.eurekaClient.shutdown(registration);
		}
	}
}
