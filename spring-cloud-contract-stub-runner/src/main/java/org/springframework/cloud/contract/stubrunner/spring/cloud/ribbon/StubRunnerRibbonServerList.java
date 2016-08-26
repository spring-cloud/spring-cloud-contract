/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.spring.cloud.ribbon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.util.StringUtils;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

/**
 * Stub Runner representation of a server list
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class StubRunnerRibbonServerList implements ServerList<Server> {

	private final ServerList<Server> serverList;

	StubRunnerRibbonServerList(final StubFinder stubFinder,
			final StubMapperProperties stubMapperProperties,
			final IClientConfig clientConfig, final ServerList<?> delegate) {
		String serviceName = clientConfig.getClientName();
		String mappedServiceName = StringUtils
				.hasText(stubMapperProperties.fromServiceIdToIvyNotation(serviceName)) ?
				stubMapperProperties.fromServiceIdToIvyNotation(serviceName) : serviceName;
		RunningStubs runningStubs = stubFinder.findAllRunningStubs();
		final Map.Entry<StubConfiguration, Integer> entry = runningStubs.getEntry(mappedServiceName);
		final Collection<Server> servers = new ArrayList<>();
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
		serverList = new ServerList<Server>() {
			@Override
			public List<Server> getInitialListOfServers() {
				List<Server> combinedList = new ArrayList<>();
				combinedList.addAll(servers);
				combinedList.addAll(delegate.getInitialListOfServers());
				return combinedList;
			}

			@Override
			public List<Server> getUpdatedListOfServers() {
				List<Server> combinedList = new ArrayList<>();
				combinedList.addAll(servers);
				combinedList.addAll(delegate.getUpdatedListOfServers());
				return combinedList;
			}
		};
	}

	@Override
	public List<Server> getInitialListOfServers() {
		return serverList.getInitialListOfServers();
	}

	@Override
	public List<Server> getUpdatedListOfServers() {
		return serverList.getUpdatedListOfServers();
	}
}
