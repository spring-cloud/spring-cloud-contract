package org.springframework.cloud.contract.stubrunner

import java.nio.file.Files

import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactSource
import au.com.dius.pact.provider.junit.loader.PactLoader
import au.com.dius.pact.provider.junit.sysprops.ValueResolver
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jetbrains.annotations.NotNull
import org.junit.Ignore
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.spec.pact.PactContractConverter

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
class PactStubDownloaderBuilderSpec extends Specification {

	@Ignore("Flakey")
	def "should retrieve pacts from broker"() throws IOException {
		given:
			Collection<Pact> pacts = new PactContractConverter().convertTo([Contract.make {
				request {
					url "/foo"
					method GET()
				}
				response {
					status OK()
				}
			},Contract.make {
				request {
					url "/bar"
					method GET()
				}
				response {
					status OK()
				}
			}])
			StubRunnerOptions options = new StubRunnerOptionsBuilder()
					.withProperties(props())
					.build()
			PactStubDownloader downloader = new PactStubDownloader(options) {
				@NotNull @Override PactLoader pactBrokerLoader(ValueResolver resolver,
															   List<String> tags) {
					return new PactLoader() {
						@Override List<Pact> load(String providerName) {
							return pacts
						}
						@Override PactSource getPactSource() {
							return null
						}
					}
				}
			}
		when:
			Map.Entry<StubConfiguration, File> entry = downloader
					.downloadAndUnpackStubJar(new StubConfiguration("com.example:bobby:+:classifier"))
		then:
			entry != null
			entry.getValue().exists()
			File contracts = new File(entry.getValue(), "com/example/bobby/contracts")
			contracts.exists()
			contracts.list() != null
			File mappings = new File(entry.getValue(), "com/example/bobby/mappings")
			mappings.exists()
			mappings.list() != null
			mappings.list().size() == 2
			StubMapping.buildFrom(new String(Files.readAllBytes(mappings.listFiles()[0].toPath())))
			StubMapping.buildFrom(new String(Files.readAllBytes(mappings.listFiles()[1].toPath())))
	}

	Map<String, String> props() {
		Map<String, String> map = new HashMap<>()
//		map.put("pactbroker.host", "localhost")
//		map.put("pactbroker.port", String.valueOf(this.port))
//		map.put("pactbroker.host", "test.pact.dius.com.au")
//		map.put("pactbroker.port", "443")
//		map.put("pactbroker.protocol", "https")
//		map.put("pactbroker.auth.scheme", "Basic")
//		map.put("pactbroker.auth.username", "dXfltyFMgNOFZAxr8io9wJ37iUpY42M")
//		map.put("pactbroker.auth.password", "O5AIZWxelWbLvqMd8PkAVycBJh2Psyg1")
		return map
	}

	//	@After
	//	void tearDown() {
	//		SnapshotRecordResult recording = WireMock.stopRecording()
	//		List<StubMapping> mappings = recording.getStubMappings()
	//		storeMappings(mappings)
	//	}

//	private void recordFromBroker() {
//		WireMock.startRecording(WireMock.recordSpec()
//				.forTarget("https://test.pact.dius.com.au")
//				.extractTextBodiesOver(9999999L)
//				.extractBinaryBodiesOver(9999999L)
//				.makeStubsPersistent(false))
//	}

	//	private void storeMappings(List<StubMapping> mappings) {
	//		try {
	//			File proxiedStubs = new File("target/stubs")
	//			proxiedStubs.mkdirs()
	//			for (StubMapping mapping : mappings) {
	//				File stub = new File(proxiedStubs, "foo" + ".json")
	//				stub.createNewFile()
	//				Files.write(stub.toPath(), mapping.toString().getBytes())
	//			}
	//		} catch (Exception e) {
	//			throw new RuntimeException(e)
	//		}
	//	}

	def "should retrieve pacts from broker using stubrunner options"() throws IOException {
		given:
			StubRunnerOptions options = new StubRunnerOptionsBuilder()
					.withStubRepositoryRoot("pact://https://test.pact.dius.com.au:443")
					.withUsername("dXfltyFMgNOFZAxr8io9wJ37iUpY42M")
					.withPassword("O5AIZWxelWbLvqMd8PkAVycBJh2Psyg1")
					.build()
			PactStubDownloader downloader = new PactStubDownloader(options)
		when:
			Map.Entry<StubConfiguration, File> entry = downloader
					.downloadAndUnpackStubJar(new StubConfiguration("com.example:bobby:+:classifier"))
		then:
			entry != null
			entry.getValue().exists()
			File contracts = new File(entry.getValue(), "com/example/bobby/contracts")
			contracts.exists()
			contracts.list() != null
			contracts.list().size() > 0
			File mappings = new File(entry.getValue(), "com/example/bobby/mappings")
			mappings.exists()
			mappings.list() != null
			mappings.list().size() > 0
	}
}
