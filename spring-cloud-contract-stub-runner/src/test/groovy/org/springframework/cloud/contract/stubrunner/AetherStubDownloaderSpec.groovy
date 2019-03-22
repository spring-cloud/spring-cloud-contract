/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner

import io.specto.hoverfly.junit.HoverflyRule
import org.eclipse.aether.RepositorySystemSession
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.util.ResourceUtils

class AetherStubDownloaderSpec extends Specification {

	@Rule
	HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode("simulation.json")

	@Rule
	TemporaryFolder folder = new TemporaryFolder()

	def 'should throw an exception when artifact not found in local m2'() {
		given:
			StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
					.withStubsMode(StubRunnerProperties.StubsMode.LOCAL)
					.build()

			AetherStubDownloader aetherStubDownloader = new AetherStubDownloader(stubRunnerOptions)

		when:
			aetherStubDownloader.downloadAndUnpackStubJar(new StubConfiguration("non.existing.group", "missing-artifact-id", "1.0-SNAPSHOT"))

		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.message.contains("Exception occurred while trying to download a stub for group")
	}

	def 'should throw an exception when local m2 gets replaced with a temp dir and a jar is not found in remote'() {
		given:
			StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
					.withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
					.withStubRepositoryRoot("file://" + folder.newFolder().absolutePath)
					.build()
		and:
			String localRepo = AetherFactories.localRepositoryDirectory(true)
			new File(localRepo, "org/springframework/cloud/spring-cloud-contract-spec"
					.replaceAll("/", File.separator)).list().size() > 0

		and:
			AetherStubDownloader aetherStubDownloader = new AetherStubDownloader(stubRunnerOptions)

		when:
			aetherStubDownloader.downloadAndUnpackStubJar(new StubConfiguration("org.springframework.cloud", "spring-cloud-contract-spec", "+", ""))

		then:
			IllegalArgumentException e = thrown(IllegalArgumentException)
			e.message.contains("Could not find metadata org.springframework.cloud:spring-cloud-contract-spec/maven-metadata.xml in remote0")
	}

	@RestoreSystemProperties
	def 'Should use local repository from settings.xml'() {
		given:
			File tempSettings = File.createTempFile("settings", ".xml")
			def m2repoFolder = 'm2repo' + File.separator + 'repository'
			tempSettings.text = '<settings><localRepository>' +
					ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + m2repoFolder).getAbsolutePath() + '</localRepository></settings>'
			System.setProperty("org.apache.maven.user-settings", tempSettings.getAbsolutePath())
			RepositorySystemSession repositorySystemSession =
					AetherFactories.newSession(AetherFactories.newRepositorySystem(), true)

		and:
			StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
					.withStubsMode(StubRunnerProperties.StubsMode.LOCAL)
					.build()
			AetherStubDownloader aetherStubDownloader = new AetherStubDownloader(stubRunnerOptions)

		when:
			def jar = aetherStubDownloader.downloadAndUnpackStubJar(
					new StubConfiguration("org.springframework.cloud.contract.verifier.stubs",
							"bootService", "0.0.1-SNAPSHOT"))

		then:
			jar != null
			repositorySystemSession.getLocalRepository().getBasedir().getAbsolutePath().endsWith(m2repoFolder)
	}
}
