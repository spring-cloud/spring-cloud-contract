package io.codearte.accurest.stubrunner

import io.codearte.accurest.messaging.noop.NoOpAccurestMessaging
import org.junit.Rule
import org.junit.rules.TemporaryFolder
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
		factory = new StubRunnerFactory(stubRunnerOptions, downloader, new NoOpAccurestMessaging())
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
