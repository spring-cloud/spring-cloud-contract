package org.springframework.cloud.contract.verifier.builder;

import java.util.Collection;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;

/**
 * Builds a single test.
 *
 * @since 1.1.0
 */
public interface SingleTestGenerator {
	/**
	 * Creates contents of a single test class in which all test scenarios from
	 * the contract metadata should be placed.
	 *
	 * @param properties                    - properties passed to the plugin
	 * @param listOfFiles                   - list of parsed contracts with additional metadata
	 * @param className                     - the name of the generated test class
	 * @param classPackage                  - the name of the package in which the test class should be stored
	 * @param includedDirectoryRelativePath - relative path to the included directory
	 * @return contents of a single test class
	 */
	String buildClass(ContractVerifierConfigProperties properties,
			Collection<ContractMetadata> listOfFiles, String className, String classPackage, String includedDirectoryRelativePath);

	/**
	 * Extension that should be appended to the generated test class. E.g. {@code .java} or {@code .php}
	 *
	 * @param properties - properties passed to the plugin
	 */
	String fileExtension(ContractVerifierConfigProperties properties);
}
