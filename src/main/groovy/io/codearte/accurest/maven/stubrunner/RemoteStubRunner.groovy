package io.codearte.accurest.maven.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.maven.EnhancedStubRunnerOptions
import io.codearte.accurest.stubrunner.AetherStubDownloader
import io.codearte.accurest.stubrunner.BatchStubRunner
import io.codearte.accurest.stubrunner.BatchStubRunnerFactory
import io.codearte.accurest.stubrunner.RunningStubs
import org.eclipse.aether.RepositorySystemSession

import javax.inject.Inject
import javax.inject.Named

@Named
@CompileStatic
@Slf4j
class RemoteStubRunner {

    private final AetherStubDownloaderFactory aetherStubDownloaderFactory

    @Inject
    RemoteStubRunner(AetherStubDownloaderFactory aetherStubDownloaderFactory) {
        this.aetherStubDownloaderFactory = aetherStubDownloaderFactory
    }

    BatchStubRunner run(EnhancedStubRunnerOptions options, RepositorySystemSession repositorySystemSession) {
        AetherStubDownloader stubDownloader = aetherStubDownloaderFactory.build(repositorySystemSession)
        try {
            log.debug("Launching StubRunner with args: $options")
            BatchStubRunner stubRunner = new BatchStubRunnerFactory(options.getStubsRunnerOptions(), options.getDependencies(), stubDownloader)
                    .buildBatchStubRunner()
            RunningStubs runningCollaborators = stubRunner.runStubs()
            log.info(runningCollaborators.toString())
            return stubRunner
        } catch (Exception e) {
            log.error("An exception occurred while trying to execute the stubs: ${e.message}")
            throw e
        }
    }
}
