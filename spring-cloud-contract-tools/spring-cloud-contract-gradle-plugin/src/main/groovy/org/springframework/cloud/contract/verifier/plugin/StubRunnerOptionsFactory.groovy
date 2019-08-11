package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties

/**
 * Helper class to create StubRunnerOptions.
 *
 * @author Anatoliy Balakirev
 */
@CompileStatic
@PackageScope
class StubRunnerOptionsFactory {

	static StubRunnerOptions createStubRunnerOptions(ContractVerifierExtension.ContractRepository contractRepository,
													 StubRunnerProperties.StubsMode contractsMode, boolean deleteStubsAfterTest,
													 Map<String, String> contractsProperties, boolean failOnNoContracts) {
		StubRunnerOptionsBuilder options = new StubRunnerOptionsBuilder()
				.withOptions(StubRunnerOptions.fromSystemProps())
				.withStubRepositoryRoot(contractRepository.repositoryUrl.getOrNull())
				.withStubsMode(contractsMode)
				.withUsername(contractRepository.username.getOrNull())
				.withPassword(contractRepository.password.getOrNull())
				.withDeleteStubsAfterTest(deleteStubsAfterTest)
				.withProperties(contractsProperties)
				.withFailOnNoStubs(failOnNoContracts)
		if (contractRepository.proxyPort.getOrNull()) {
			options = options.withProxy(contractRepository.proxyHost.getOrNull(), contractRepository.proxyPort.getOrNull())
		}
		return options.build()
	}
}
