/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
