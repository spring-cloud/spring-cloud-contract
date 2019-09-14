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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import groovy.lang.GString;
import org.codehaus.groovy.runtime.GStringImpl;

/**
 * Groovy converter that understand types like {@link GString}.
 *
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
public class GroovyDslPropertyConverter implements DslPropertyConverter {

	@Override
	public Object testSide(Object object) {
		if (object instanceof GString) {
			boolean anyPattern = Arrays.stream(((GString) object).getValues())
					.anyMatch(it -> it instanceof RegexProperty);
			if (!anyPattern) {
				return object;
			}
			List<Object> generatedValues = Arrays.stream(((GString) object).getValues())
					.map(it -> it instanceof RegexProperty
							? ((RegexProperty) it).generate() : it)
					.collect(Collectors.toList());
			Object[] arrayOfObjects = generatedValues.toArray();
			String[] strings = Arrays.copyOf(((GString) object).getStrings(),
					((GString) object).getStrings().length, String[].class);
			String newUrl = new GStringImpl(arrayOfObjects, strings).toString();
			return new Url(newUrl);
		}
		return object;
	}

}
