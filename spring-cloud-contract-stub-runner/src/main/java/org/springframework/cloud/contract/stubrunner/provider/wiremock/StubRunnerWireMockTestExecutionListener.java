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

package org.springframework.cloud.contract.stubrunner.provider.wiremock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.stubrunner.HttpServerStub;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.wiremock.WireMockUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Stops the {@link HttpServerStub} after each test class
 *
 * @author Marcin Grzejszczak
 * @since 1.2.6
 */
public final class StubRunnerWireMockTestExecutionListener
		extends AbstractTestExecutionListener {

	private static final Log log = LogFactory
			.getLog(StubRunnerWireMockTestExecutionListener.class);

	private static Map<ApplicationContext, Map<WireMockHttpServerStub, PortAndMappings>> STUBS = new ConcurrentHashMap<>();

	@Override
	public void beforeTestClass(TestContext testContext) {
		if (testContext.getTestClass().getAnnotationsByType(AutoConfigureStubRunner.class).length == 0) {
			if (log.isTraceEnabled()) {
				log.trace("No @AutoConfigureStubRunner annotation found on [" + testContext.getTestClass() + "]. Skipping");
			}
			return;
		}
		Map<WireMockHttpServerStub, PortAndMappings> stubs = STUBS
				.get(testContext.getApplicationContext());
		if (stubs != null) {
			if (log.isDebugEnabled()) {
				log.debug("Found a matching application context from ["
						+ testContext.getTestClass().getName() + "]");
			}
			for (Map.Entry<WireMockHttpServerStub, PortAndMappings> entry : stubs
					.entrySet()) {
				while (entry.getKey().isRunning()) {
					entry.getKey().stop();
				}
				List<StubMapping> mappings = entry.getValue().mappings;
				if (log.isDebugEnabled()) {
					log.debug("Stopped a running WireMock instance at " + "port ["
							+ entry.getValue().port + "] with stub mappings size ["
							+ mappings.size() + "]. Restarting the stub.");
				}
				entry.getKey().start(entry.getValue().port);
				entry.getKey().registerDescriptors(mappings);
				WireMockUtils.getMappingsEndpoint(entry.getValue().port);
			}
		}
	}

	@Override
	public void afterTestClass(TestContext testContext) {
		if (testContext.getTestClass().getAnnotationsByType(AutoConfigureStubRunner.class).length == 0) {
			if (log.isTraceEnabled()) {
				log.trace("No @AutoConfigureStubRunner annotation found on [" + testContext.getTestClass() + "]. Skipping");
			}
			return;
		}
		STUBS.put(testContext.getApplicationContext(), WireMockHttpServerStub.SERVERS);
		if (log.isDebugEnabled()) {
			log.debug("Stopping servers " + WireMockHttpServerStub.SERVERS);
		}
		for (HttpServerStub serverStub : WireMockHttpServerStub.SERVERS.keySet()) {
			serverStub.stop();
		}
	}
}