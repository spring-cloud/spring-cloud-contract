package org.springframework.cloud;

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.cloud.frauddetection.FraudDetectionController;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

public abstract class MvcTest {

	public static WebTarget webTarget;

	private static Server server;
	private static Client client;

	@BeforeClass
	public static void setupTest() throws Exception {
		int port = findAvailableTcpPort(8000);
		URI baseUri = UriBuilder.fromUri("http://localhost").port(port).build();
		// Create Server
		Server server = new Server(port);
		// Configure ServletContextHandler
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		// Create Servlet Container
		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);
		// Tells the Jersey Servlet which REST service/class to load.
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", FraudDetectionController.class.getCanonicalName());
		// Start the server
		server.start();
		ClientConfig clientConfig = new ClientConfig();
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