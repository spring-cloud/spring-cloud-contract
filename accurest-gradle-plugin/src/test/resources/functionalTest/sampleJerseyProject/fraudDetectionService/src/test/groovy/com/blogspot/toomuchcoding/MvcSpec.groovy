package com.blogspot.toomuchcoding
import com.blogspot.toomuchcoding.frauddetection.Application
import com.blogspot.toomuchcoding.frauddetection.FraudRestApplication
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
