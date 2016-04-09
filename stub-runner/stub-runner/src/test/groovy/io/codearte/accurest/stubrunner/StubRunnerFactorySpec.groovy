package io.codearte.accurest.stubrunner

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class StubRunnerFactorySpec extends Specification {

	@Rule
	TemporaryFolder folder = new TemporaryFolder()

	Collection<StubConfiguration> collaborators = [new StubConfiguration("a:b"), new StubConfiguration("c:d")]
	StubDownloader downloader = Mock(StubDownloader)
	StubRunnerOptions stubRunnerOptions = new StubRunnerOptions(stubRepositoryRoot: 'http://sth.net')
	StubRunnerFactory factory = new StubRunnerFactory(stubRunnerOptions, collaborators, downloader)

	def "Should download stub definitions many times"() {
		given:
		folder.newFolder("mappings")
		2 * downloader.downloadAndUnpackStubJar(_, _, _, _, _) >> folder.root
		stubRunnerOptions.stubRepositoryRoot = folder.root.absolutePath
		when:
		Collection<StubRunner> stubRunners = collectOnlyPresentValues(factory.createStubsFromServiceConfiguration())
		then:
		stubRunners.size() == 2
	}

	private List<StubRunner> collectOnlyPresentValues(Collection<StubRunner> stubRunners) {
		return stubRunners.findAll { it != null }
	}
}
