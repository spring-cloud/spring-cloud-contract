package io.codearte.accurest.stubrunner

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.ToString

/**
 * Represents a single JSON file that was found in the folder with
 * potential WireMock stubs
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
@PackageScope
class WiremockMappingDescriptor {
	final File descriptor

	WiremockMappingDescriptor(File mappingDescriptor) {
		this.descriptor = mappingDescriptor
	}

	StubMapping getMapping() {
		return StubMapping.buildFrom(descriptor.getText('UTF-8'))
	}
}
