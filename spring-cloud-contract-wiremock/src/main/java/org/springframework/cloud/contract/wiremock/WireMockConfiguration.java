/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.cloud.contract.wiremock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;

/**
 * @author Dave Syer
 *
 */
@Configuration
public class WireMockConfiguration implements SmartLifecycle, ImportAware {

	private volatile boolean running;

	private WireMockServer server;

	@Autowired(required = false)
	private Options options;

	@Value("${wiremock.server.port:8080}")
	private int port = 8080;

	@Value("${wiremock.server.https-port:-1}")
	private int httpsPort = -1;

	@Override
	public void setImportMetadata(AnnotationMetadata metadata) {
		AnnotationAttributes map = AnnotationAttributes.fromMap(
				metadata.getAnnotationAttributes(AutoConfigureWireMock.class.getName()));
		int port = map.getNumber("port").intValue();
		if (port > 0) {
			this.port = port;
		}
		int httpsPort = map.getNumber("httpsPort").intValue();
		if (httpsPort > 0) {
			this.httpsPort = httpsPort;
		}
	}

	@PostConstruct
	public void init() {
		if (options == null) {
			com.github.tomakehurst.wiremock.core.WireMockConfiguration factory = WireMockSpring.options();
			if (port != 8080) {
				factory.port(port);
			}
			if (httpsPort != -1) {
				factory.httpsPort(httpsPort);
			}
			this.options = factory;
		}
		server = new WireMockServer(options);
	}

	@Override
	public void start() {
		server.start();
		WireMock.configureFor("localhost", server.port());
		running = true;
	}

	@Override
	public void stop() {
		if (running) {
			server.stop();
			running = false;
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

}
