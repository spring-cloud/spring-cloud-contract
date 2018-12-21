package org.springframework.cloud.contract.verifier.builder


import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.file.ContractMetadata 

/**
 * Builds a single test.
 *
 * @since 1.1.0
 */
trait SingleTestGenerator {

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
	 * @deprecated use {@link SingleTestGenerator#buildClass(ContractVerifierConfigProperties, Collection, String, GeneratedClassData)}
	 */
	@Deprecated
	abstract String buildClass(ContractVerifierConfigProperties properties,
			Collection<ContractMetadata> listOfFiles, String className, String classPackage, String includedDirectoryRelativePath)

	/**
	 * Creates contents of a single test class in which all test scenarios from
	 * the contract metadata should be placed.
	 *
	 * @param properties                    - properties passed to the plugin
	 * @param listOfFiles                   - list of parsed contracts with additional metadata
	 * @param generatedClassData            - information about the generated class
	 * @param includedDirectoryRelativePath - relative path to the included directory
	 * @return contents of a single test class
	 */
	String buildClass(ContractVerifierConfigProperties properties,
			Collection<ContractMetadata> listOfFiles, String includedDirectoryRelativePath, GeneratedClassData generatedClassData) {
		return buildClass(properties, listOfFiles, generatedClassData.className, generatedClassData.classPackage, includedDirectoryRelativePath)
	}

	/**
	 * Extension that should be appended to the generated test class. E.g. {@code .java} or {@code .php}
	 *
	 * @param properties - properties passed to the plugin
	 */
	abstract String fileExtension(ContractVerifierConfigProperties properties)

	static class GeneratedClassData {
		public final String className
		public final String classPackage
		public final java.nio.file.Path testClassPath

		GeneratedClassData(String className, String classPackage,
				java.nio.file.Path testClassPath) {
			this.className = className
			this.classPackage = classPackage
			this.testClassPath = testClassPath
		}
	}
}
