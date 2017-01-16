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

package org.springframework.cloud.contract.stubrunner.spring.cloud.ribbon;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;

/**
 * Ribbon AutoConfiguration that manipulates the service id to make the service
 * be picked from the list of available WireMock instance if one is available.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class StubRunnerRibbonBeanPostProcessor implements BeanPostProcessor {

	private final BeanFactory beanFactory;
	private StubFinder stubFinder;
	private StubMapperProperties stubMapperProperties;
	private IClientConfig clientConfig;

	StubRunnerRibbonBeanPostProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	private StubFinder stubFinder() {
		if (this.stubFinder == null) {
			this.stubFinder = this.beanFactory.getBean(StubFinder.class);
		}
		return this.stubFinder;
	}

	private StubMapperProperties stubMapperProperties() {
		if (this.stubMapperProperties == null) {
			this.stubMapperProperties = this.beanFactory.getBean(StubMapperProperties.class);
		}
		return this.stubMapperProperties;
	}

	private IClientConfig clientConfig() {
		if (this.clientConfig == null) {
			this.clientConfig = this.beanFactory.getBean(IClientConfig.class);
		}
		return this.clientConfig;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ServerList && !(bean instanceof StubRunnerRibbonServerList)) {
			return new StubRunnerRibbonServerList(stubFinder(), stubMapperProperties(), clientConfig(), (ServerList<?>) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
