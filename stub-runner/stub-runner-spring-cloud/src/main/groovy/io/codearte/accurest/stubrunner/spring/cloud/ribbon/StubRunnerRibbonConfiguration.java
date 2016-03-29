package io.codearte.accurest.stubrunner.spring.cloud.ribbon;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import io.codearte.accurest.stubrunner.RunningStubs;
import io.codearte.accurest.stubrunner.StubConfiguration;
import io.codearte.accurest.stubrunner.StubFinder;
import io.codearte.accurest.stubrunner.spring.cloud.StubMapperProperties;
import io.codearte.accurest.stubrunner.util.StringUtils;

/**
 * Ribbon AutoConfiguration that manipulates the service id to make the service
 * be picked from the list of available WireMock instance if one is available.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
public class StubRunnerRibbonConfiguration {

	@Bean
	@Primary
	@SuppressWarnings("unchecked")
	public ServerList<?> stubRunnerRibbonServerList(final ServerList<?> serverList,
			StubFinder stubFinder,
			final StubMapperProperties stubMapperProperties,
			IClientConfig clientConfig) {
		String serviceName = clientConfig.getClientName();
		String mappedServiceName = StringUtils
				.hasText(stubMapperProperties.fromServiceIdToIvyNotation(serviceName)) ?
				stubMapperProperties.fromServiceIdToIvyNotation(serviceName) : serviceName;
		RunningStubs runningStubs = stubFinder.findAllRunningStubs();
		final Map.Entry<StubConfiguration, Integer> entry = runningStubs.getEntry(mappedServiceName);
		final Collection servers = new ArrayList<Server>();
		if (entry != null) {
			servers.add(new Server("localhost", entry.getValue()) {
				@Override
				public MetaInfo getMetaInfo() {
					return new MetaInfo() {
						@Override
						public String getAppName() {
							return stubMapperProperties.fromIvyNotationToId(entry.getKey().toColonSeparatedDependencyNotation());
						}

						@Override
						public String getServerGroup() {
							return null;
						}

						@Override
						public String getServiceIdForDiscovery() {
							return stubMapperProperties.fromIvyNotationToId(entry.getKey().getArtifactId());
						}

						@Override
						public String getInstanceId() {
							return stubMapperProperties.fromIvyNotationToId(entry.getKey().getArtifactId());
						}
					};
				}
			});
		}
		return new ServerList() {
			@Override
			public List<?> getInitialListOfServers() {
				List combinedList = new ArrayList<>();
				combinedList.addAll(servers);
				combinedList.addAll(serverList.getInitialListOfServers());
				return combinedList;
			}

			@Override
			public List<?> getUpdatedListOfServers() {
				List combinedList = new ArrayList<>();
				combinedList.addAll(servers);
				combinedList.addAll(serverList.getUpdatedListOfServers());
				return combinedList;
			}
		};
	}

}
