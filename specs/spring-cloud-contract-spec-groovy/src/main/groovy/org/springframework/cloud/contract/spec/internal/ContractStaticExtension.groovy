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

package org.springframework.cloud.contract.spec.internal

import java.util.stream.Collectors

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.GStringImpl

import org.springframework.cloud.contract.spec.Contract

@CompileStatic
class ContractStaticExtension {

	@CompileStatic
	static Contract make(final Contract type, @DelegatesTo(Contract) Closure closure) {
		return doMake(closure)
	}

	/**
	 * Factory method to create the DSL
	 */
	@CompileStatic
	private static Contract doMake(@DelegatesTo(Contract) Closure closure) {
		Contract dsl = new Contract()
		closure.delegate = dsl
		closure()
		Contract.assertContract(dsl)
		return dsl
	}

	/**
	 * Factory method to create the DSL
	 */
	@CompileStatic
	static Object testUrl(final Url type, Object url) {
		if (url instanceof GString) {
			boolean anyPattern = Arrays.stream(((GString) url).getValues())
									   .anyMatch({ it -> it instanceof RegexProperty });
			if (!anyPattern) {
				return url;
			}
			List<Object> generatedValues = Arrays.stream(((GString) url).getValues())
												 .map({ it -> it instanceof RegexProperty
														 ? ((RegexProperty) it).generate() : it })
												 .collect(Collectors.toList());
			Object[] arrayOfObjects = generatedValues.toArray();
			String[] strings = Arrays.copyOf(((GString) url).getStrings(),
					((GString) url).getStrings().length, String[].class);
			String newUrl = new GStringImpl(arrayOfObjects, strings).toString();
			return new Url(newUrl);
		}

		return url;
	}
}
