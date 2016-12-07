package org.springframework.cloud.contract.verifier.converter

import groovy.transform.CompileStatic
import org.springframework.core.io.support.SpringFactoriesLoader

/**
 * Retrieves file converters from the class path and operates on them.
 * 
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class StubGeneratorHolder {
	
	private final List<StubGenerator> converters = []

	StubGeneratorHolder() {
		this.converters.addAll(SpringFactoriesLoader.loadFactories(StubGenerator, null))
	}

	StubGeneratorHolder(List<StubGenerator> converters) {
		this.converters.addAll(converters)
	}

	StubGenerator converterForName(String fileName) {
		return this.converters.find { it.canHandleFileName(fileName) }
	}
}
