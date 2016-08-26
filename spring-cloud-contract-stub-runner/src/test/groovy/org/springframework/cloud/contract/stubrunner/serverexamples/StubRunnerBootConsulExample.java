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

package org.springframework.cloud.contract.stubrunner.serverexamples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.contract.stubrunner.server.EnableStubRunnerServer;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;

/**
 * @author Marcin Grzejszczak
 */
@SpringBootApplication
@EnableStubRunnerServer
@EnableDiscoveryClient
@AutoConfigureStubRunner
public class StubRunnerBootConsulExample {

	public static void main(String[] args) {
		SpringApplication.run(StubRunnerBootConsulExample.class, args);
	}

}
/*

-Dstubrunner.repositoryRoot=classpath:m2repo/repository/
-Dstubrunner.cloud.stubbed.discovery.enabled=false
-Dstubrunner.ids=org.springframework.cloud.contract.verifier.stubs:loanIssuance,org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer,org.springframework.cloud.contract.verifier.stubs:bootService
-Dstubrunner.idsToServiceIds.fraudDetectionServer=someNameThatShouldMapFraudDetectionServer
-Dstubrunner.cloud.consul.enabled=true
-Dstubrunner.camel.enabled=false
-Dspring.cloud.zookeeper.enabled=false
-Deureka.client.enabled=false
-Dspring.cloud.zookeeper.discovery.enabled=false
-Ddebug=true
-Dspring.cloud.consul.host=192.168.99.100

 */
