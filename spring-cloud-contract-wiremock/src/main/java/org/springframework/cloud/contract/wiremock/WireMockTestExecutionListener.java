/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Dirties the test context if WireMock was running on a fixed port.
 *
 * @author Marcin Grzejszczak
 * @author Matt Garner
 * @author Waldemar Panas
 * @since 1.2.6
 */
public final class WireMockTestExecutionListener extends AbstractTestExecutionListener {

	private static final Log log = LogFactory.getLog(WireMockTestExecutionListener.class);

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		if (applicationContextBroken(testContext)
				|| wireMockConfigurationMissing(testContext)
				|| annotationMissing(testContext)) {
			return;
		}
		if (!portIsFixed(testContext)) {
			if (log.isDebugEnabled()) {
				log.debug("Re-registering default mappings");
			}
			wireMockConfig(testContext).initIfNotRunning();
		}
	}

	@Override
	public void afterTestClass(TestContext testContext) {
		if (applicationContextBroken(testContext)
				|| wireMockConfigurationMissing(testContext)
				|| annotationMissing(testContext)) {
			return;
		}
		if (portIsFixed(testContext)) {
			if (log.isWarnEnabled()) {
				log.warn("You've used fixed ports for WireMock setup - "
						+ "will mark context as dirty. Please use random ports, as much "
						+ "as possible. Your tests will be faster and more reliable and this "
						+ "warning will go away");
			}
			testContext
					.markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug(
						"Resetting mappings for the next test to restart them. That's necessary when"
								+ " reusing the same context with new servers running on random ports");
			}
			wireMockConfig(testContext).reRegisterServerWithResetMappings();
		}
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		if (applicationContextBroken(testContext)
				|| wireMockConfigurationMissing(testContext)
				|| annotationMissing(testContext)) {
			return;
		}
		WireMockConfiguration wireMockConfiguration = wireMockConfig(testContext);
		if (wireMockConfiguration.wireMock.isResetMappingsAfterEachTest()) {
			if (log.isDebugEnabled()) {
				log.debug("Resetting mappings for the next test.");
			}
			wireMockConfiguration.resetMappings();
		}
	}

	private boolean annotationMissing(TestContext testContext) {
		if (testContext.getTestClass()
				.getAnnotationsByType(AutoConfigureWireMock.class).length == 0) {
			if (log.isDebugEnabled()) {
				log.debug("No @AutoConfigureWireMock annotation found on ["
						+ testContext.getTestClass() + "]. Skipping");
			}
			return true;
		}
		return false;
	}

	private boolean wireMockConfigurationMissing(TestContext testContext) {
		boolean missing = !testContext(testContext)
				.containsBean(WireMockConfiguration.class.getName());
		if (log.isDebugEnabled()) {
			log.debug("WireMockConfiguration is missing [" + missing + "]");
		}
		return missing;
	}

	private ApplicationContext testContext(TestContext testContext) {
		return testContext.getApplicationContext();
	}

	private boolean applicationContextBroken(TestContext testContext) {
		try {
			testContext.getApplicationContext();
			return false;
		}
		catch (Exception ex) {
			if (log.isDebugEnabled()) {
				log.debug("Application context is broken due to", ex);
			}
			return true;
		}
	}

	private WireMockConfiguration wireMockConfig(TestContext testContext) {
		return testContext(testContext).getBean(WireMockConfiguration.class);
	}

	private boolean portIsFixed(TestContext testContext) {
		WireMockConfiguration wireMockProperties = wireMockConfig(testContext);
		boolean httpPortDynamic = wireMockProperties.wireMock.getServer().isPortDynamic();
		boolean httpsPortDynamic = wireMockProperties.wireMock.getServer()
				.isHttpsPortDynamic();
		if (log.isDebugEnabled()) {
			int httpPort = wireMockProperties.wireMock.getServer().getPort();
			int httpsPort = wireMockProperties.wireMock.getServer().getHttpsPort();
			log.debug("Http port [" + httpPort + "] dynamic [" + httpPortDynamic + "]"
					+ " https port [" + httpsPort + "] dynamic [" + httpsPortDynamic
					+ "]");
		}
		return !httpPortDynamic || !httpsPortDynamic;
	}

}
