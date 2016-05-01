package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.messaging.AccurestMessaging

/**
 * Factory of StubRunners. Basing on the options and passed collaborators
 * downloads the stubs and returns a list of corresponding stub runners.
 */
@Slf4j
@CompileStatic
class StubRunnerFactory {

	private final StubRunnerOptions stubRunnerOptions
	private final StubDownloader stubDownloader
	private final AccurestMessaging accurestMessaging

	StubRunnerFactory(StubRunnerOptions stubRunnerOptions, StubDownloader stubDownloader, AccurestMessaging accurestMessaging) {
		this.stubRunnerOptions = stubRunnerOptions
		this.stubDownloader = stubDownloader
		this.accurestMessaging = accurestMessaging
	}

	Collection<StubRunner> createStubsFromServiceConfiguration() {
		return stubRunnerOptions.getDependencies().collect { StubConfiguration stubsConfiguration ->
			Map.Entry<StubConfiguration, File> entry = stubDownloader.downloadAndUnpackStubJar(stubRunnerOptions, stubsConfiguration)
			if (!entry) {
				return null
			}
			return createStubRunner(entry.key, entry.value)
		}.findAll { it != null }
	}

	private StubRunner createStubRunner(StubConfiguration stubsConfiguration, File unzipedStubDir) {
		if (!unzipedStubDir) {
			return null
		}
		return createStubRunner(unzipedStubDir, stubsConfiguration, stubRunnerOptions)
	}

	private StubRunner createStubRunner(File unzippedStubsDir, StubConfiguration stubsConfiguration,
	                                    StubRunnerOptions stubRunnerOptions) {
		return new StubRunner(stubRunnerOptions, unzippedStubsDir.path, stubsConfiguration, accurestMessaging)
	}

}
