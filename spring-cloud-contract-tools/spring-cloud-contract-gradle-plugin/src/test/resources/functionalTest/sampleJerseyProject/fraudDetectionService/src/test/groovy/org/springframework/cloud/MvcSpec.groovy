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

package org.springframework.cloud
import org.springframework.cloud.frauddetection.Application
import org.springframework.cloud.frauddetection.FraudRestApplication
import org.eclipse.jetty.server.Server
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider
import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.jetty.JettyHttpContainerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.UriBuilder

import static org.springframework.util.SocketUtils.findAvailableTcpPort

abstract class MvcSpec extends Specification {

	@Shared
	WebTarget webTarget

	@Shared
	private Server server

	@Shared
	private Client client

	def setupSpec() {

		URI baseUri = UriBuilder.fromUri("http://localhost").port(findAvailableTcpPort(8000)).build()


		ResourceConfig resourceConfig = ResourceConfig.forApplication(new FraudRestApplication())
		resourceConfig.property("contextConfig", new AnnotationConfigApplicationContext(Application))
		server = JettyHttpContainerFactory.createServer(baseUri, resourceConfig, true)

		ClientConfig clientConfig = new ClientConfig()
		clientConfig.connectorProvider(new ApacheConnectorProvider())
		client = ClientBuilder.newClient(clientConfig)

		webTarget = client.target(baseUri)

		server.start()
	}

	def cleanupSpec() {
		client?.close()
		server?.stop()
	}

	void assertThatRejectionReasonIsNull(def rejectionReason) {
		assert !rejectionReason
	}
}
