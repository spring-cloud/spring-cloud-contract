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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Stops the WireMock server after each test class and restarts it before every class
 *
 * @author Marcin Grzejszczak
 * @since 1.2.6
 */
public final class WireMockTestExecutionListener extends AbstractTestExecutionListener {

	private static final Log log = LogFactory.getLog(WireMockTestExecutionListener.class);

	@Override public void beforeTestClass(TestContext testContext) {
		try {
			if (wireMockConfigMissing(testContext)) {
				return;
			}
			WireMockConfiguration wireMockConfiguration = wireMockConfiguration(testContext);
			if (log.isDebugEnabled()) {
				log.debug("WireMock configuration is running [" + wireMockConfiguration.isRunning() + "]");
			}
			if (!wireMockConfiguration.isRunning()) {
				wireMockConfiguration.init();
				wireMockConfiguration.start();
				WireMockUtils.getMappingsEndpoint(wireMockConfiguration.port());
			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception occurred while trying to init WireMock configuration", e);
			}
		}
	}

	private boolean wireMockConfigMissing(TestContext testContext) {
		boolean missing = !testContext.getApplicationContext().containsBean(WireMockConfiguration.class.getName());
		if (log.isDebugEnabled()) {
			log.debug("WireMockConfig is missing [" + missing + "]");
		}
		return missing;
	}

	@Override public void afterTestClass(TestContext testContext) {
		try {
			if (wireMockConfigMissing(testContext)) {
				return;
			}
			stopWireMockConfiguration(testContext);
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception occurred while trying to init WireMock configuration", e);
			}
		}
	}

	private void stopWireMockConfiguration(TestContext testContext) {
		WireMockConfiguration wireMockConfiguration = wireMockConfiguration(testContext);
		if (wireMockConfiguration.isRunning()) {
			if (log.isDebugEnabled()) {
				log.debug("WireMock is running, will stop it");
			}
			wireMockConfiguration.stop();
		}
	}

	private WireMockConfiguration wireMockConfiguration(TestContext testContext) {
		return testContext.getApplicationContext().getBean(WireMockConfiguration.class);
	}
}
