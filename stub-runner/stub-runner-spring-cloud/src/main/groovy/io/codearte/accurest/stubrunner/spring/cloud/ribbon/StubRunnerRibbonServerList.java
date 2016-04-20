package io.codearte.accurest.stubrunner.spring.cloud.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import io.codearte.accurest.stubrunner.RunningStubs;
import io.codearte.accurest.stubrunner.StubConfiguration;
import io.codearte.accurest.stubrunner.StubFinder;
import io.codearte.accurest.stubrunner.spring.cloud.StubMapperProperties;
import io.codearte.accurest.stubrunner.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stub Runner representation of a server list
 *
 * @author Marcin Grzejszczak
 */
class StubRunnerRibbonServerList implements ServerList {

	private final ServerList<?> serverList;

	@SuppressWarnings("unchecked")
	StubRunnerRibbonServerList(final StubFinder stubFinder,
									  final StubMapperProperties stubMapperProperties,
									  final IClientConfig clientConfig,
									  final ServerList<?> delegate) {;
		String serviceName = clientConfig.getClientName();
		String mappedServiceName = StringUtils
				.hasText(stubMapperProperties.fromServiceIdToIvyNotation(serviceName)) ?
				stubMapperProperties.fromServiceIdToIvyNotation(serviceName) : serviceName;
		RunningStubs runningStubs = stubFinder.findAllRunningStubs();
		final Map.Entry<StubConfiguration, Integer> entry = runningStubs.getEntry(mappedServiceName);
		final Collection servers = new ArrayList<>();
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
		serverList = new ServerList() {
			@Override
			public List<?> getInitialListOfServers() {
				List combinedList = new ArrayList<>();
				combinedList.addAll(servers);
				combinedList.addAll(delegate.getInitialListOfServers());
				return combinedList;
			}

			@Override
			public List<?> getUpdatedListOfServers() {
				List combinedList = new ArrayList<>();
				combinedList.addAll(servers);
				combinedList.addAll(delegate.getUpdatedListOfServers());
				return combinedList;
			}
		};
	}

	@Override
	public List<?> getInitialListOfServers() {
		return serverList.getInitialListOfServers();
	}

	@Override
	public List<?> getUpdatedListOfServers() {
		return serverList.getUpdatedListOfServers();
	}
}
