package org.springframework.cloud.contract.stubrunner

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class ContractDownloaderSpec extends Specification {

	StubDownloader stubDownloader = Stub()
	StubConfiguration stubConfiguration = new StubConfiguration()

	def 'should set inclusion pattern on config when path pattern was explicitly provided with a separator at the beginning'() {
		given:
			String contractPath = '/a/b/c/d'
			ContractDownloader contractDownloader = new ContractDownloader(stubDownloader,
					stubConfiguration, contractPath, '', '')
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
		and:

		when:
			contractDownloader.unpackedDownloadedContracts(properties)
		then:
			properties.includedContracts == ''
	}
}
