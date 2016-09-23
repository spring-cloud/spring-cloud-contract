package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.PackageScope
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * @author Marcin Grzejszczak
 */
@PackageScope
class ExtensionToProperties {

	protected static ContractVerifierConfigProperties fromExtension(ContractVerifierExtension extension) {
		return new ContractVerifierConfigProperties(
				targetFramework: extension.targetFramework,
				testMode: extension.testMode,
				basePackageForTests: extension.basePackageForTests,
				baseClassForTests: extension.baseClassForTests,
				nameSuffixForTests: extension.nameSuffixForTests,
				ruleClassForTests: extension.ruleClassForTests,
				excludedFiles: extension.excludedFiles,
				ignoredFiles: extension.ignoredFiles,
				imports: extension.imports,
				staticImports: extension.staticImports,
				contractsDslDir: extension.contractsDslDir,
				generatedTestSourcesDir: extension.generatedTestSourcesDir,
				stubsOutputDir: extension.stubsOutputDir,
				stubsSuffix: extension.stubsSuffix,
				assertJsonSize: extension.assertJsonSize,
				packageWithBaseClasses: extension.packageWithBaseClasses,
				baseClassMappings: extension.baseClassMappings
		)
	}
}
