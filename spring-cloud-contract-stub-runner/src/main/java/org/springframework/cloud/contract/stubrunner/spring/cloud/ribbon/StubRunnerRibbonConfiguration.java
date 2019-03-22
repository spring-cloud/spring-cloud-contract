/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner.spring.cloud.ribbon;

import java.util.ArrayList;
import java.util.List;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
class StubRunnerRibbonConfiguration {

	@Bean
	static StubRunnerRibbonBeanPostProcessor stubRunnerRibbonBeanPostProcessor(
			BeanFactory beanFactory) {
		return new StubRunnerRibbonBeanPostProcessor(beanFactory);
	}

	@Bean
	@ConditionalOnMissingBean
	public ServerList<?> noOpServerList() {
		return new ServerList<Server>() {
			@Override
			public List<Server> getInitialListOfServers() {
				return new ArrayList<>();
			}

			@Override
			public List<Server> getUpdatedListOfServers() {
				return new ArrayList<>();
			}
		};
	}

}
