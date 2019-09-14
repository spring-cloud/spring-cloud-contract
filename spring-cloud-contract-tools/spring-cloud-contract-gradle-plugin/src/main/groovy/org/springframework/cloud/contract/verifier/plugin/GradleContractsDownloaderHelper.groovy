package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import org.springframework.util.StringUtils

/**
 * @author Anatoliy Balakirev
 */
@PackageScope
@CompileStatic
class GradleContractsDownloaderHelper {

	private static final String LATEST_VERSION = '+'

	@PackageScope
	static StubConfiguration stubConfiguration(ContractVerifierExtension.Dependency contractDependency) {
		String groupId = contractDependency.groupId.getOrNull()
		String artifactId = contractDependency.artifactId.getOrNull()
		String version = StringUtils.hasText(contractDependency.version.getOrNull()) ?
				contractDependency.version.getOrNull() : LATEST_VERSION
		String classifier = contractDependency.classifier.getOrNull()
		String stringNotation = contractDependency.stringNotation.getOrNull()
		if (StringUtils.hasText(stringNotation)) {
			StubConfiguration stubConfiguration = new StubConfiguration(stringNotation)
			return new StubConfiguration(stubConfiguration.groupId, stubConfiguration.artifactId,
					stubConfiguration.version, stubConfiguration.classifier)
		}
		return new StubConfiguration(groupId, artifactId, version, classifier)
	}

}
