/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.contract.wiremock;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.springframework.util.Assert;

/**
 * Utility class to work with WireMock.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.3
 */
public final class WireMockUtils {

	private WireMockUtils() {
		throw new IllegalStateException("Don't instantiate");
	}

	/**
	 * Thanks to Tom Akehurst: I looked at tcpdump while running the failing
	 * test. HttpUrlConnection is doing something weird - it's creating a
	 * connection in a previous test case, which works fine, then the usual
	 * fin -> fin ack etc. etc. ending handshake happens. But it seems it
	 * isn't discarded, but reused after that. Because the server thinks
	 * (rightly) that the connection is closed, it just sends a RST packet.
	 * Calling the admin endpoint just happened to remove the dead connection
	 * from the pool. This also fixes the problem (which using the Java HTTP
	 * client): System.setProperty("http.keepAlive", "false");
	 **/
	public static CloseableHttpResponse getMappingsEndpoint(int port) {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		try {
			CloseableHttpResponse response = client
					.execute(new HttpHost("localhost", port), new HttpGet("/__admin/mappings"));
			Assert.isTrue(response.getStatusLine().getStatusCode() == 200, "Status code must be 200");
			return response;
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
