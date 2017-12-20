/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.spring.cloud.eureka;

import java.util.Map;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.EurekaClientConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubRunning;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerConfiguration;
import org.springframework.cloud.contract.stubrunner.spring.cloud.ConditionalOnStubbedDiscoveryDisabled;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubsRegistrar;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Autoconfiguration for registering stubs in a Eureka Service discovery
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter({StubRunnerConfiguration.class, EurekaClientAutoConfiguration.class})
@ConditionalOnClass(CloudEurekaClient.class)
@ConditionalOnStubbedDiscoveryDisabled
@ConditionalOnEurekaEnabled
@ConditionalOnProperty(value = "stubrunner.cloud.eureka.enabled", matchIfMissing = true)
public class StubRunnerSpringCloudEurekaAutoConfiguration {

	@Profile("!cloud")
	@Configuration
	protected static class NonCloudConfig {
		@Bean(initMethod = "registerStubs")
		public StubsRegistrar stubsRegistrar(StubRunning stubRunning, Eureka eureka,
				StubMapperProperties stubMapperProperties, InetUtils inetUtils, EurekaInstanceConfigBean eurekaInstanceConfigBean) {
			return new EurekaStubsRegistrar(stubRunning, eureka, stubMapperProperties, inetUtils, eurekaInstanceConfigBean);
		}
	}

	@Profile("cloud")
	@Configuration
	protected static class CloudConfig {
		private static final int DEFAULT_PORT = 80;
		private static final Log log = LogFactory.getLog(CloudConfig.class);

		@Autowired Environment environment;

		@Bean(initMethod = "registerStubs")
		public StubsRegistrar cloudStubsRegistrar(StubRunning stubRunning, Eureka eureka,
				StubMapperProperties stubMapperProperties, InetUtils inetUtils, EurekaInstanceConfigBean eurekaInstanceConfigBean) {
			return new EurekaStubsRegistrar(stubRunning, eureka, stubMapperProperties, inetUtils, eurekaInstanceConfigBean) {
				@Override protected String hostName(Map.Entry<StubConfiguration, Integer> entry) {
					String hostname =
							CloudConfig.this.environment.getProperty("application.hostname") +
									"-" + entry.getValue() + "." + CloudConfig.this.environment.getProperty("application.domain");
					log.info("Registering stub [" + entry.getKey().getArtifactId() + "] with hostname [" + hostname + "]");
					return hostname;
				}

				@Override protected int port(Map.Entry<StubConfiguration, Integer> entry) {
					return DEFAULT_PORT;
				}
			};
		}
	}

	@Bean(name = "eurekaRegistrar")
	public Eureka eureka(InetUtils inetUtils, ApplicationInfoManager manager,
			EurekaClientConfig config, ApplicationContext applicationContext,
			EurekaClientConfigBean eurekaClientConfigBean) {
		return new Eureka(inetUtils, eurekaClientConfigBean, new CloudEurekaClient(manager, config, applicationContext));
	}
}
