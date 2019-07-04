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

import org.springframework.cloud.contract.spec.util.ValidateUtils;

/**
 * Represents a URL that may contain query parameters.
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

	// Can be overridable by extensions
	private static Object testUrl(Object url) {
		return DslPropertyConverter.INSTANCE.testSide(url);
	}

	public QueryParameters getQueryParameters() {
		return queryParameters;
	}

	public void setQueryParameters(QueryParameters queryParameters) {
		this.queryParameters = queryParameters;
	}

}
