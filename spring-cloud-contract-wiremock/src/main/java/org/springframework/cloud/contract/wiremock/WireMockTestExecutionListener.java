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
				wireMockConfiguration.reset();
				wireMockConfiguration.init();
				wireMockConfiguration.start();
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
