/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		int port = findAvailableTcpPort(10000);
		URI baseUri = UriBuilder.fromUri("http://localhost").port(port).build();
		// Create Server
		Server server = new Server(port);
		// Configure ServletContextHandler
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		// Create Servlet Container
		ServletHolder jerseyServlet = context
				.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);
		// Tells the Jersey Servlet which REST service/class to load.
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
				FraudDetectionController.class.getCanonicalName());
		// Start the server
		server.start();
		ClientConfig clientConfig = new ClientConfig();
		client = ClientBuilder.newClient(clientConfig);
		webTarget = client.target(baseUri);
		try {
			server.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void cleanupTest() {
		if (client != null) {
			client.close();
		}
		if (server != null) {
			try {
				server.stop();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void assertThatRejectionReasonIsNull(Object rejectionReason) {
		assert rejectionReason == null;
	}

}
