package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import spock.lang.Specification

class ContractVerifierSpec extends Specification {
    protected Project project

    def setup() {
        def dateString = new Date().format("yyyy-MM-dd_HH-mm-ss")
        def testFolder = new File("build/generated-tests/${getClass().simpleName}/${dateString}")
        testFolder.mkdirs()
        project = ProjectBuilder.builder().withProjectDir(testFolder).build()
    }

    def "Applies groovy plugin"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.plugins.hasPlugin(GroovyPlugin)
    }

    def "Creates contracts extension"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.extensions.findByType(ContractVerifierConfigProperties) != null
    }

    def "Creates generateContractTests task"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.tasks.findByName("generateContractTests") != null
    }

    def "Configures generateContractTests task as a dependency of the check task"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.tasks.check.getDependsOn().contains("generateContractTests")
    }

    def "Creates generateWireMockClientStubs task"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.tasks.findByName("generateWireMockClientStubs") != null
    }

    def "Creates verifierStubsJar task"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.tasks.findByName("verifierStubsJar") != null
    }

    def "Configures generateWireMockClientStubs task as a dependency of the verifierStubsJar task"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.tasks.verifierStubsJar.getDependsOn().contains("generateWireMockClientStubs")
    }

    def "Creates copyContracts task"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.tasks.findByName("copyContracts") != null
    }

    def "Configures copyContracts task as a dependency of the verifierStubsJar task"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
        Task copyContracts = project.tasks.copyContracts
        assert copyContracts != null

        expect:
        project.tasks.verifierStubsJar.getDependsOn().contains(copyContracts)
    }

    /**
     * project.evaluate() is used here in order to trigger the evaluation lifecycle of a project.
     * This method is currently exposed via the internal API and is subject to change, however, Gradle
     * does not yet expose a way to test this portion of the lifecycle.
     */
    def "Configures maven-publish plugin, if available"() {
        given:
        project.plugins.apply(MavenPublishPlugin)
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
        project.evaluate() // Currently internal method to trigger afterEvaluate blocks.

        expect:
        def publications = project.extensions.getByType(PublishingExtension).publications
        publications.size() > 0
        publications.findByName("stubs") != null
    }

    def "Adds wiremock as a testCompile dependency"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.configurations.testCompile.dependencies.find { it.group == "com.github.tomakehurst" && it.name == "wiremock" } != null
    }

    def "Adds jsonassert as a testCompile dependency"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.configurations.testCompile.dependencies.find { it.group == "com.toomuchcoding.jsonassert" && it.name == "jsonassert" } != null
    }

    def "Adds assertj-core as a testCompile dependency"() {
        given:
        project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

        expect:
        project.configurations.testCompile.dependencies.find { it.group == "org.assertj" && it.name == "assertj-core" } != null
    }
}
