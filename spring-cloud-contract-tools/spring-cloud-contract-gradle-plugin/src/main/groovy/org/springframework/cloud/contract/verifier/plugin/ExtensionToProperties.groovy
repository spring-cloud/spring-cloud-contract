package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * @author Marcin Grzejszczak
 */
@PackageScope
@CompileStatic
class ExtensionToProperties {

	protected static ContractVerifierConfigProperties fromExtension(ContractVerifierExtension extension) {
		return new ContractVerifierConfigProperties(
				testFramework: extension.getTestFramework(),
				testMode: extension.getTestMode(),
				basePackageForTests: extension.getBasePackageForTests(),
				baseClassForTests: extension.getBaseClassForTests(),
				nameSuffixForTests: extension.getNameSuffixForTests(),
				ruleClassForTests: extension.getRuleClassForTests(),
				excludedFiles: extension.getExcludedFiles(),
				includedFiles: extension.getIncludedFiles(),
				ignoredFiles: extension.getIgnoredFiles(),
				imports: extension.getImports(),
				staticImports: extension.getStaticImports(),
				contractsDslDir: extension.getContractsDslDir(),
				generatedTestSourcesDir: extension.getGeneratedTestSourcesDir(),
				generatedTestResourcesDir: extension.getGeneratedTestResourcesDir(),
				stubsOutputDir: extension.getStubsOutputDir(),
				stubsSuffix: extension.getStubsSuffix(),
				assertJsonSize: extension.getAssertJsonSize(),
				packageWithBaseClasses: extension.getPackageWithBaseClasses(),
				baseClassMappings: extension.getBaseClassMappings(),
				excludeBuildFolders: extension.getExcludeBuildFolders()
		)
	}
}
