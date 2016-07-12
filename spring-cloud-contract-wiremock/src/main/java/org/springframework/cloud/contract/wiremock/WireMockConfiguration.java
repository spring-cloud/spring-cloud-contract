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
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;

/**
 * @author Dave Syer
 *
 */
@Configuration
public class WireMockConfiguration implements SmartLifecycle {

	private volatile boolean running;

	private WireMockServer server;

	@Autowired(required = false)
	private Options options;

	@PostConstruct
	public void init() {
		if (options == null) {
			this.options = com.github.tomakehurst.wiremock.core.WireMockConfiguration
					.wireMockConfig()
					.httpServerFactory(new SpringBootHttpServerFactory());
		}
		server = new WireMockServer(options);
	}

	@Override
	public void start() {
		server.start();
		WireMock.configureFor("localhost", options.portNumber());
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
