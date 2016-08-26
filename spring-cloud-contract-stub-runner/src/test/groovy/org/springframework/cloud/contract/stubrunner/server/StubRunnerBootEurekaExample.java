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

package org.springframework.cloud.contract.stubrunner.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author Marcin Grzejszczak
 */
// tag::stubrunnereureka[]
@SpringBootApplication
@EnableStubRunnerServer
@EnableEurekaClient
@AutoConfigureStubRunner
public class StubRunnerBootEurekaExample {

	public static void main(String[] args) {
		SpringApplication.run(StubRunnerBootEurekaExample.class, args);
	}

}
// end::stubrunnereureka[]

/*

// tag::stubrunnereureka_args[]
-Dstubrunner.repositoryRoot=http://repo.spring.io/snapshots (1)
-Dstubrunner.cloud.stubbed.discovery.enabled=false (2)
-Dstubrunner.ids=org.springframework.cloud.contract.verifier.stubs:loanIssuance,org.springframework.cloud.contract.verifier.stubs:fraudDetectionServer,org.springframework.cloud.contract.verifier.stubs:bootService (3)
-Dstubrunner.idsToServiceIds.fraudDetectionServer=someNameThatShouldMapFraudDetectionServer (4)

(1) - we tell Stub Runner where all the stubs reside
(2) - we don't want the default behaviour where the discovery service is stubbed. That's why the stub registration will be picked
(3) - we provide a list of stubs to download
(4) - we provide a list of artifactId to serviceId mapping
// end::stubrunnereureka_args[]


-Dstubrunner.cloud.eureka.enabled=true
-Dstubrunner.repositoryRoot=classpath:m2repo/repository/
-Dstubrunner.camel.enabled=false
-Dspring.cloud.zookeeper.enabled=false
-Dspring.cloud.zookeeper.discovery.enabled=false
-Ddebug=true

 */
