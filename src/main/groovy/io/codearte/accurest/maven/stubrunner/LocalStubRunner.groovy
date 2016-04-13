package io.codearte.accurest.maven.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.maven.AccurestConverter
import io.codearte.accurest.stubrunner.StubConfiguration
import io.codearte.accurest.stubrunner.StubRunner
import io.codearte.accurest.stubrunner.StubRunnerOptions

import javax.inject.Named
import java.nio.file.Files

import static io.codearte.accurest.maven.AccurestConverter.convertAccurestToStubs

@Named
@CompileStatic
@Slf4j
class LocalStubRunner {

    void run(File contractsDir, StubRunnerOptions options) {
        log.info("Launching StubRunner with contracts from ${contractsDir}")
        File mappingsOutput = Files.createTempDirectory('accurest').toFile()
        mappingsOutput.deleteOnExit()
        AccurestConfigProperties config = new AccurestConfigProperties(contractsDslDir: contractsDir, stubsOutputDir: mappingsOutput)
        convertAccurestToStubs(config)
        new StubRunner(options, contractsDir.getPath(), new StubConfiguration(mappingsOutput.toString())).runStubs()
    }
}
