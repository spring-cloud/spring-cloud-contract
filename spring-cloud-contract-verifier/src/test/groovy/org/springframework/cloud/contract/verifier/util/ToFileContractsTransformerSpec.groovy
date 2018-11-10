package org.springframework.cloud.contract.verifier.util

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 * @since
 */
class ToFileContractsTransformerSpec extends Specification {

	@Rule TemporaryFolder tmp = new TemporaryFolder()
	File folder

	def setup() {
		folder = tmp.newFolder()
	}

	def "should store contracts as files"() {
		given:
			File input = new File("src/test/resources/dsl")
			String fqn = YamlContractConverter.name
		when:
			List<File> files = new ToFileContractsTransformer().storeContractsAsFiles(input.absolutePath, fqn, folder.absolutePath)
		then:
			files.size() == 1
			files.get(0).name.endsWith(".yml")
	}
}
