package org.springframework.cloud.contract.verifier.builder.imports

import groovy.transform.PackageScope

import org.springframework.cloud.contract.verifier.builder.JavaTestGenerator

/**
 * @author Olga Maciaszek-Sharma
 */
@PackageScope
class ImportProviderFactory {

	static TestTypeSpecificImportProvider getImportProvider(JavaTestGenerator.TestType testType) {
		if (JavaTestGenerator.TestType.HTTP == testType) {
			return new HttpImportProvider()  // TODO: pass restassured package
		}
	}
}
