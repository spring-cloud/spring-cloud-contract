package org.springframework.cloud.contract.stubrunner.provider.wiremock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.stubrunner.HttpServerStub;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * Stops the {@link HttpServerStub} after each test class
 *
 * @author Marcin Grzejszczak
 * @since 1.2.6
 */
public final class StubRunnerWireMockTestExecutionListener extends AbstractTestExecutionListener {

	private static final Log log = LogFactory.getLog(StubRunnerWireMockTestExecutionListener.class);

	private static Map<ApplicationContext, Map<WireMockHttpServerStub, PortAndMappings>> STUBS = new ConcurrentHashMap<>();

	@Override public void beforeTestClass(TestContext testContext) {
		Map<WireMockHttpServerStub, PortAndMappings> stubs = STUBS
				.get(testContext.getApplicationContext());
		if (stubs != null) {
			if (log.isDebugEnabled()) {
				log.debug("Found a matching application context from [" + testContext.getTestClass().getName() + "]");
			}
			for (Map.Entry<WireMockHttpServerStub, PortAndMappings> entry : stubs.entrySet()) {
				while (entry.getKey().isRunning()) {
					entry.getKey().stop();
				}
				List<StubMapping> mappings = entry.getValue().mappings;
				if (log.isDebugEnabled()) {
					log.debug("Stopped a running WireMock instance at "
							+ "port [" + entry.getValue().port + "] with stub mappings "
							+ "size [" + mappings.size() + "]. Restarting the stub.");
				}
				entry.getKey().start(entry.getValue().port);
				entry.getKey().registerDescriptors(mappings);
				Assert.isTrue(new RestTemplate().getForEntity("http://localhost:" + entry.getValue().port + "/__admin/mappings", String.class)
						.getStatusCode().is2xxSuccessful(), "__admin/mappings endpoint wasn't accessible");
			}
		}
	}

	@Override public void afterTestClass(TestContext testContext) {
		STUBS.put(testContext.getApplicationContext(), WireMockHttpServerStub.SERVERS);
		if (log.isDebugEnabled()) {
			log.debug("Stopping servers " + WireMockHttpServerStub.SERVERS);
		}
		for (HttpServerStub serverStub : WireMockHttpServerStub.SERVERS.keySet()) {
			serverStub.stop();
		}
	}
}