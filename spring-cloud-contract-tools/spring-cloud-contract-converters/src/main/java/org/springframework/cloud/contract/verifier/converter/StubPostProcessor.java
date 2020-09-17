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

import java.util.List;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Post processor of stub mappings.
 *
 * @param <T> type of stub mapping
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public interface StubPostProcessor<T> {

	/**
	 * List of registered stub post processors.
	 */
	List<StubPostProcessor> PROCESSORS = SpringFactoriesLoader.loadFactories(StubPostProcessor.class, null);

	/**
	 * @param stubMapping - generated stub mapping
	 * @param contract - contract for which the mapping was generated
	 * @return modified stub mapping
	 */
	default T postProcess(T stubMapping, Contract contract) {
		return stubMapping;
	}

	/**
	 * @param contract - contract for which the mapping was generated
	 * @return {@code true} if this post process should be applied
	 */
	boolean isApplicable(Contract contract);

}
