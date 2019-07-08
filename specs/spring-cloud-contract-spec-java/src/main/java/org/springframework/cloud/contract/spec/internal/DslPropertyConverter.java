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

package org.springframework.cloud.contract.spec.internal;

import java.util.List;

import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * A hook mechanism to allow external languages and frameworks to convert types that are
 * not in Java for Spring Cloud Contract to understand.
 *
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
public interface DslPropertyConverter {

	/**
	 * Default no op implementation.
	 */
	DslPropertyConverter DEFAULT = new DslPropertyConverter() {
	};

	/**
	 * A composite over available instances or default if none is present.
	 */
	DslPropertyConverter INSTANCE = instance();

	/**
	 * @return a composite {@link DslPropertyConverter} around a list of
	 * {@link DslPropertyConverter} or a default no op instance
	 */
	static DslPropertyConverter instance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		List<DslPropertyConverter> converters = SpringFactoriesLoader
				.loadFactories(DslPropertyConverter.class, null);
		if (converters.isEmpty()) {
			return DEFAULT;
		}
		return new DslPropertyConverter() {
			@Override
			public Object testSide(Object object) {
				Object convertedObject = object;
				for (DslPropertyConverter converter : converters) {
					convertedObject = converter.testSide(convertedObject);
				}
				return convertedObject;
			}

			@Override
			public Object stubSide(Object object) {
				Object convertedObject = object;
				for (DslPropertyConverter converter : converters) {
					convertedObject = converter.stubSide(convertedObject);
				}
				return convertedObject;
			}
		};
	}

	/**
	 * Conversion mechanism for the test side manipulation.
	 * @param object object to manipulate
	 * @return converted object for the test side
	 */
	default Object testSide(Object object) {
		return object;
	}

	/**
	 * Conversion mechanism for the stub side manipulation.
	 * @param object object to manipulate
	 * @return converted object for the stub side
	 */
	default Object stubSide(Object object) {
		return object;
	}

}
