/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.provider.wiremock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Marks context to be restarted if at least one stub has a fixed port.
 *
 * @author Marcin Grzejszczak
 * @since 1.2.6
 */
public final class StubRunnerWireMockTestExecutionListener extends AbstractTestExecutionListener {

	private static final Log log = LogFactory.getLog(StubRunnerWireMockTestExecutionListener.class);

	@Override
	public void afterTestClass(TestContext testContext) {
		if (testContext.getTestClass().getAnnotationsByType(AutoConfigureStubRunner.class).length == 0) {
			if (log.isDebugEnabled()) {
				log.debug("No @AutoConfigureStubRunner annotation found on [" + testContext.getTestClass()
						+ "]. Skipping");
			}
			return;
		}
		if (!WireMockHttpServerStub.SERVERS.isEmpty()
				&& WireMockHttpServerStub.SERVERS.values().stream().noneMatch(p -> p.random)) {
			if (log.isWarnEnabled()) {
				log.warn("You've used fixed ports for WireMock setup - "
						+ "will mark context as dirty. Please use random ports, as much "
						+ "as possible. Your tests will be faster and more reliable and this "
						+ "warning will go away");
			}
			testContext.markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
		}
		// potential race condition
		WireMockHttpServerStub.SERVERS.clear();
	}

}
