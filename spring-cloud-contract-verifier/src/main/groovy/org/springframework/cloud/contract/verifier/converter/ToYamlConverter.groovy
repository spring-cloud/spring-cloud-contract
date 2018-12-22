package org.springframework.cloud.contract.verifier.converter

import java.nio.file.Files

import groovy.transform.CompileStatic
import groovy.util.logging.Commons

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.core.io.support.SpringFactoriesLoader
/**
 * Converts contracts to YAML for the given folder
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
@CompileStatic
@Commons
class ToYamlConverter {

	private final static ToYamlConverter INSTANCE = new ToYamlConverter()

	private YamlContractConverter yamlContractConverter = new YamlContractConverter()
	private static final List<ContractConverter> CONTRACT_CONVERTERS =
			SpringFactoriesLoader.loadFactories(ContractConverter, null)

	protected File doReplaceGroovyContractWithYaml(ContractConverter converter, File file) {
		// base dir: target/copied_contracts/contracts/
		// target/copied_contracts/contracts/foo/baz/bar.groovy
		Collection<Contract> collection = converter.convertFrom(file)
		List<YamlContract> yamls = this.yamlContractConverter.convertTo(collection)
		// rm target/copied_contracts/contracts/foo/baz/bar.groovy
		file.delete()
		// [contracts/foo/baz/bar.groovy] -> [contracts/foo/baz/bar.yml]
		Map<String, byte[]> stored = this.yamlContractConverter.store(yamls)
		Map.Entry<String, byte[]> first = stored.entrySet().first()
		File ymlContractVersion = new File(file.parentFile, first.getKey())
		// store the YMLs instead of groovy files
		Files.write(ymlContractVersion.toPath(), first.value)
		return ymlContractVersion
	}

	/**
	 * If a contract ends with e.g. [.groovy] we will move it to the [original]
	 * folder, convert the [.groovy] version to [.yml] and store it instead
	 * of the Groovy file. From that we will continue processing as if
	 * from the very beginning there was only a [.yml] file
	 *
	 * @param baseDir
	 */
	static void replaceContractWithYaml(File baseDir) {
		baseDir.eachFileRecurse { File file ->
			ContractConverter converter = CONTRACT_CONVERTERS.find {
				it.isAccepted(file)
			}
			if (converter) {
				if (log.isDebugEnabled()) {
					log.debug("Will replace contract [${file.name}] to a YAML version")
				}
				INSTANCE.doReplaceGroovyContractWithYaml(converter, file)
			}
		}
	}
}
