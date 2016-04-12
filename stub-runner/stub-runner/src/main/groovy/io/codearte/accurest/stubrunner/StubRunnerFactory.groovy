package io.codearte.accurest.stubrunner

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j

/**
 * Factory of StubRunners. Basing on the options and passed collaborators
 * downloads the stubs and returns a list of corresponding stub runners.
 */
@Slf4j
@CompileStatic
class StubRunnerFactory {

	private final StubRunnerOptions stubRunnerOptions
	private final Collection<StubConfiguration> collaborators
	private final StubDownloader stubDownloader

	StubRunnerFactory(StubRunnerOptions stubRunnerOptions,
	                  Collection<StubConfiguration> collaborators,
	                            StubDownloader stubDownloader) {
		this.stubRunnerOptions = stubRunnerOptions
		this.collaborators = collaborators
		this.stubDownloader = stubDownloader
	}

	Collection<StubRunner> createStubsFromServiceConfiguration() {
		return collaborators.collect { StubConfiguration stubsConfiguration ->
			final File unzipedStubDir = stubDownloader.downloadAndUnpackStubJar(stubRunnerOptions.workOffline,
					stubRunnerOptions.stubRepositoryRoot,
					stubsConfiguration.groupId, stubsConfiguration.artifactId, stubsConfiguration.classifier)
			return createStubRunner(unzipedStubDir, stubsConfiguration)
		}.findAll { it != null }
	}

	private StubRunner createStubRunner(File unzipedStubDir, StubConfiguration stubsConfiguration) {
		if (!unzipedStubDir) {
			return null
		}
		return createStubRunner(unzipedStubDir, stubsConfiguration, stubRunnerOptions)
	}

	private StubRunner createStubRunner(File unzippedStubsDir, StubConfiguration stubsConfiguration,
										StubRunnerOptions stubRunnerOptions) {
		return new StubRunner(stubRunnerOptions, unzippedStubsDir.path, stubsConfiguration)
	}

}
