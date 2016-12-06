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
class SingleFileConvertersHolder {
	
	private final List<SingleFileConverter> converters = []

	SingleFileConvertersHolder() {
		this.converters.addAll(SpringFactoriesLoader.loadFactories(SingleFileConverter, null))
	}

	SingleFileConvertersHolder(List<SingleFileConverter> converters) {
		this.converters.addAll(converters)
	}

	SingleFileConverter converterForName(String fileName) {
		return this.converters.find { it.canHandleFileName(fileName) }
	}
}
