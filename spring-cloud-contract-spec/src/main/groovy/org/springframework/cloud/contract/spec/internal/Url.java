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
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.GStringImpl;

import org.springframework.cloud.contract.spec.util.ValidateUtils;

/**
 * Represents a URL that may contain query parameters
 *
 * @since 1.0.0
 */
public class Url extends DslProperty {

	private QueryParameters queryParameters;

	public Url(DslProperty prop) {
		super(prop.getClientValue(), prop.getServerValue());
		ValidateUtils.validateServerValueIsAvailable(prop.getServerValue(), "Url");
	}

	public Url(Object url) {
		super(url, testUrl(url));
		ValidateUtils.validateServerValueIsAvailable(url, "Url");
	}

	private static Object testUrl(Object url) {
		if (url instanceof GString) {
			boolean anyPattern = Arrays.stream(((GString) url).getValues())
					.anyMatch(it -> it instanceof RegexProperty);
			if (!anyPattern) {
				return url;
			}
			List<Object> generatedValues = Arrays.stream(((GString) url).getValues())
					.map(it -> it instanceof RegexProperty
							? ((RegexProperty) it).generate() : it)
					.collect(Collectors.toList());
			Object[] arrayOfObjects = generatedValues.toArray();
			String newUrl = new GStringImpl(arrayOfObjects,
					DefaultGroovyMethods
							.asType(Arrays.copyOf(((GString) url).getStrings(),
									((GString) url).getStrings().length), String[].class))
											.toString();
			return new Url(newUrl);
		}

		return url;
	}

	public QueryParameters getQueryParameters() {
		return queryParameters;
	}

	public void setQueryParameters(QueryParameters queryParameters) {
		this.queryParameters = queryParameters;
	}

}
