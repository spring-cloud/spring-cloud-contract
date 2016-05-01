package io.codearte.accurest.maven.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.stubrunner.StubConfiguration
import io.codearte.accurest.stubrunner.StubRunner
import io.codearte.accurest.stubrunner.StubRunnerOptions

import javax.inject.Named

@Named
@CompileStatic
@Slf4j
class LocalStubRunner {

    StubRunner run(String contractsDir, StubRunnerOptions options) {
        log.info("Launching StubRunner with contracts from ${contractsDir}")
        StubRunner stubRunner = new StubRunner(options, contractsDir, new StubConfiguration(contractsDir))
        stubRunner.runStubs()
        return stubRunner
    }
}
