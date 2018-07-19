package org.springframework.cloud.contract.verifier.builder.imports

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
@PackageScope
class ImportDefinitions {

	final List<String> imports
	final List<String> staticImports

	ImportDefinitions(List<String> imports, List<String> staticImports = []) {
		this.imports = imports
		this.staticImports = staticImports
	}
}