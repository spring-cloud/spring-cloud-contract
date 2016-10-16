package org.springframework.cloud.contract.stubrunner

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import spock.lang.Specification
/**
 * @author Marcin Grzejszczak
 */
class ContractDownloaderSpec extends Specification {

	StubDownloader stubDownloader = Stub()
	StubConfiguration stubConfiguration = new StubConfiguration('')

	File file = new File(File.separator + ['some','path','to','somewhere'].join(File.separator))

	def 'should set inclusion pattern on config when path pattern was explicitly provided with a separator at the beginning'() {
		given:
			String contractPath = File.separator + ['a','b','c','d'].join(File.separator)
			ContractDownloader contractDownloader = new ContractDownloader(stubDownloader,
					stubConfiguration, contractPath, '', '')
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
		and:
			stubDownloader.downloadAndUnpackStubJar(_, _) >> new AbstractMap.SimpleEntry(stubConfiguration, file)
		when:
			contractDownloader.unpackedDownloadedContracts(properties)
		then:
			properties.includedContracts.startsWith('^')
			properties.includedContracts.endsWith('$')
			properties.includedContracts.contains(fileSeparated('/some/path/to/somewhere/a/b/c/d.*'))
	}

	def 'should set inclusion pattern on config when path pattern was explicitly provided without a separator at the beginning'() {
		given:
			String contractPath = File.separator + ['a','b','c','d'].join(File.separator)
			ContractDownloader contractDownloader = new ContractDownloader(stubDownloader,
					stubConfiguration, contractPath, '', '')
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
		and:
			stubDownloader.downloadAndUnpackStubJar(_, _) >> new AbstractMap.SimpleEntry(stubConfiguration, file)
		when:
			contractDownloader.unpackedDownloadedContracts(properties)
		then:
			properties.includedContracts.startsWith('^')
			properties.includedContracts.endsWith('$')
			properties.includedContracts.contains(fileSeparated('/some/path/to/somewhere/a/b/c/d.*'))
	}

	private static String fileSeparated(String string) {
		return string.replace('/', File.separator).replace("\\", "\\\\")
	}
}
