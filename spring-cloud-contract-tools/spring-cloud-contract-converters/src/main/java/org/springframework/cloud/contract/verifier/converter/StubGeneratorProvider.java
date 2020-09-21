/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Retrieves file converters from the class path and operates on them.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class StubGeneratorProvider {

	private final List<StubGenerator> converters = new ArrayList<StubGenerator>();

	public StubGeneratorProvider() {
		this.converters.addAll(SpringFactoriesLoader.loadFactories(StubGenerator.class, null));
	}

	public StubGeneratorProvider(List<StubGenerator> converters) {
		this.converters.addAll(converters);
	}

	public Collection<StubGenerator> converterForName(final String fileName) {
		return this.converters.stream().filter(stubGenerator -> stubGenerator.canHandleFileName(fileName))
				.collect(Collectors.toList());
	}

	public Collection<StubGenerator> allOrDefault(StubGenerator defaultStubGenerator) {
		return this.converters.isEmpty() ? Collections.singletonList(defaultStubGenerator) : this.converters;
	}

}
