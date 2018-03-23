package org.springframework.cloud.contract.stubrunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

/**
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
public class AetherStubDownloaderBuilder implements StubDownloaderBuilder {
	private static final Log log = LogFactory.getLog(AetherStubDownloaderBuilder.class);

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.stubsMode == StubRunnerProperties.StubsMode.CLASSPATH) {
			return null;
		}
		log.info("Will download stubs and contracts via Aether");
		return new AetherStubDownloader(stubRunnerOptions);
	}
}
