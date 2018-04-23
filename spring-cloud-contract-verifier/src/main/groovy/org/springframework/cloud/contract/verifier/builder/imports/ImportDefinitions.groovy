package org.springframework.cloud.contract.verifier.builder.imports

/**
 * @author Olga Maciaszek-Sharma
 */

class ImportDefinitions {

	final List<String> imports
	final List<String> staticImports

	ImportDefinitions(List<String> imports, List<String> staticImports = []) {
		this.imports = imports
		this.staticImports = staticImports
	}
}