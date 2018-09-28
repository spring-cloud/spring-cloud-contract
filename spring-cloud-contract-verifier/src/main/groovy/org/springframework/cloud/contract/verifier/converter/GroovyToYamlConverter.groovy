package org.springframework.cloud.contract.verifier.converter

import java.nio.file.Files

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import groovy.transform.CompileStatic
import groovy.util.logging.Commons

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter

/**
 * Converts Groovy DSLs to YAML for the given folder
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
@CompileStatic
@Commons
class GroovyToYamlConverter {

	private final static GroovyToYamlConverter INSTANCE = new GroovyToYamlConverter()

	private final YAMLMapper mapper = new YAMLMapper()

	GroovyToYamlConverter() {
		this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
	}

	protected File doReplaceGroovyContractWithYaml(File file) {
		// base dir: target/copied_contracts/contracts/
		// target/copied_contracts/contracts/foo/baz/bar.groovy
		Collection<Contract> collection = ContractVerifierDslConverter.convertAsCollection(file.parentFile, file)
		List<YamlContract> yamls = new YamlContractConverter().convertTo(collection)
		// rm target/copied_contracts/contracts/foo/baz/bar.groovy
		file.delete()
		// [contracts/foo/baz/bar.groovy] -> [contracts/foo/baz/bar.yml]
		File ymlContractVersion = new File(file.parentFile, file.name.replace(".groovy", ".yml"))
		// store the YMLs instead of groovy files
		Files.write(ymlContractVersion.toPath(), mapper.writeValueAsBytes(yamls))
		return ymlContractVersion
	}

	/**
	 * If a contract ends with [.groovy] we will move it to the [original]
	 * folder, convert the [.groovy] version to [.yml] and store it instead
	 * of the Groovy file. From that we will continue processing as if
	 * from the very beginning there was only a [.yml] file
	 *
	 * @param baseDir
	 */
	static void replaceGroovyContractWithYaml(File baseDir) {
		baseDir.eachFileRecurse {
			if (it.name.endsWith(".groovy")) {
				if (log.isDebugEnabled()) {
					log.debug("Will replace groovy contract [${it.name}] to a YAML version")
				}
				INSTANCE.doReplaceGroovyContractWithYaml(it)
			}
		}
	}
}
