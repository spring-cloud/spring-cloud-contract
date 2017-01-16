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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.contract.stubrunner.spring.cloud.ConditionalOnStubbedDiscoveryEnabled;
import org.springframework.cloud.contract.stubrunner.spring.cloud.StubMapperProperties;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;

import com.netflix.loadbalancer.ServerList;

@Configuration
@ConditionalOnClass(ServerList.class)
@ConditionalOnBean(StubMapperProperties.class)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = StubRunnerRibbonConfiguration.class)
@ConditionalOnStubbedDiscoveryEnabled
@ConditionalOnProperty(value = "stubrunner.cloud.ribbon.enabled", matchIfMissing = true)
public class StubRunnerRibbonAutoConfiguration {

}
