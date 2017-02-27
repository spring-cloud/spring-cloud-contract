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
class StubGeneratorProvider {
	
	private final List<StubGenerator> converters = []

	StubGeneratorProvider() {
		this.converters.addAll(SpringFactoriesLoader.loadFactories(StubGenerator, null))
	}

	StubGeneratorProvider(List<StubGenerator> converters) {
		this.converters.addAll(converters)
	}

	Collection<StubGenerator> converterForName(String fileName) {
		return this.converters.findAll { it.canHandleFileName(fileName) }
	}

	Collection<StubGenerator> allOrDefault(StubGenerator defaultStubGenerator) {
		return this.converters.empty ? [defaultStubGenerator] : this.converters
	}
}
