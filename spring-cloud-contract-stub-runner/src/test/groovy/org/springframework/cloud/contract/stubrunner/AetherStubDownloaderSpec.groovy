package org.springframework.cloud.contract.stubrunner

import io.specto.hoverfly.junit.HoverflyRule
import org.junit.Rule

import spock.lang.Specification

class AetherStubDownloaderSpec extends Specification {

    @Rule
    HoverflyRule hoverflyRule = HoverflyRule.buildFromClassPathResource("simulation.json").build()

    def 'Should be able to download from a repository using username and password authentication'() {
        given:
        final StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
            .withUsername("andrew.morgan")
            .withPassword("k+hbZp8rpolRucXB09dGE/CxPXxidQryQUYSGbeo6JE=")
            .withProxy("localhost", hoverflyRule.proxyPort)
            .withStubRepositoryRoot("https://test.jfrog.io/test/libs-snapshot-local")
            .build()

        final AetherStubDownloader aetherStubDownloader = new AetherStubDownloader(stubRunnerOptions)

        when:
        final def jar = aetherStubDownloader.downloadAndUnpackStubJar(new StubConfiguration("io.test", "test-simulations-svc", "1.0-SNAPSHOT"))

        then:
        jar != null
    }
}
