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

import java.util.Objects;
import java.util.function.Consumer;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		Url url = (Url) o;
		return Objects.equals(queryParameters, url.queryParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), queryParameters);
	}

	@Override
	public String toString() {
		return "Url{" + "\nqueryParameters=" + queryParameters + "} \n"
				+ super.toString();
	}

	/**
	 * The query parameters part of the contract.
	 * @param consumer function to manipulate the query parameters
	 */
	public void queryParameters(Consumer<QueryParameters> consumer) {
		this.queryParameters = new QueryParameters();
		consumer.accept(this.queryParameters);
	}

	/**
	 * The query parameters part of the contract.
	 * @param consumer function to manipulate the query parameters
	 */
	public void queryParameters(@DelegatesTo(QueryParameters.class) Closure consumer) {
		this.queryParameters = new QueryParameters();
		consumer.setDelegate(this.queryParameters);
		consumer.call();
	}

}
