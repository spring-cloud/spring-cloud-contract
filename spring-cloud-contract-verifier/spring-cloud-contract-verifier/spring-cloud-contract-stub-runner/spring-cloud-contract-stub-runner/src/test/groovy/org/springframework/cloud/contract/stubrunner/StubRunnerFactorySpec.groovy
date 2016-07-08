/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpContractVerifierMessaging
import spock.lang.Specification

class StubRunnerFactorySpec extends Specification {

	@Rule
	TemporaryFolder folder = new TemporaryFolder()

	String stubs = "a:b,c:d"
	StubDownloader downloader = Mock(StubDownloader)
	StubRunnerOptions stubRunnerOptions
	StubRunnerFactory factory

	void setup() {
		stubRunnerOptions = new StubRunnerOptionsBuilder()
				.withStubRepositoryRoot(folder.root.absolutePath) // FIXME: not used
				.withStubs(stubs).build()
		factory = new StubRunnerFactory(stubRunnerOptions, downloader, new NoOpContractVerifierMessaging())
	}

	def "Should download stub definitions many times"() {
		given:
		folder.newFolder("mappings")
		1 * downloader.downloadAndUnpackStubJar(_, _) >> new AbstractMap.SimpleEntry(new StubConfiguration('a:b'), folder.root)
		1 * downloader.downloadAndUnpackStubJar(_, _) >> new AbstractMap.SimpleEntry(new StubConfiguration('c:d'), folder.root)
		when:
		Collection<StubRunner> stubRunners = collectOnlyPresentValues(factory.createStubsFromServiceConfiguration())
		then:
		stubRunners.size() == 2
	}

	private List<StubRunner> collectOnlyPresentValues(Collection<StubRunner> stubRunners) {
		return stubRunners.findAll { it != null }
	}
}
