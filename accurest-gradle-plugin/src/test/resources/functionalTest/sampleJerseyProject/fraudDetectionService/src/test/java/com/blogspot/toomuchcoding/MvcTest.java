package com.blogspot.toomuchcoding;

import com.blogspot.toomuchcoding.frauddetection.Application;
import com.blogspot.toomuchcoding.frauddetection.FraudRestApplication;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

public abstract class MvcTest {

	public static WebTarget webTarget;

	private static Server server;

	private static Client client;

	@BeforeClass
	public static void setupTest() {

		URI baseUri = UriBuilder.fromUri("http://localhost").port(findAvailableTcpPort(8000)).build();


		ResourceConfig resourceConfig = ResourceConfig.forApplication(new FraudRestApplication());
		resourceConfig.property("contextConfig", new AnnotationConfigApplicationContext(Application.class));
		server = JettyHttpContainerFactory.createServer(baseUri, resourceConfig, true);

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.connectorProvider(new ApacheConnectorProvider());
		client = ClientBuilder.newClient(clientConfig);

		webTarget = client.target(baseUri);

		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void cleanupTest() {
		if(client != null) {
			client.close();
		}
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void assertThatRejectionReasonIsNull(Object rejectionReason) {
		assert rejectionReason == null;
	}
}