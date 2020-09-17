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

package org.springframework.cloud.contract.spec.internal;

import java.util.Objects;

import org.springframework.cloud.contract.spec.util.ValidateUtils;

/**
 * Represents a single HTTP query parameter.
 *
 * @since 1.0.0
 */
public class QueryParameter extends DslProperty {

	private String name;

	public QueryParameter(String name, DslProperty dslProperty) {
		super(dslProperty.getClientValue(), dslProperty.getServerValue());
		ValidateUtils.validateServerValueIsAvailable(dslProperty.getServerValue(), "Query parameter \'" + name + "\'");
		this.name = name;
	}

	public QueryParameter(String name, MatchingStrategy matchingStrategy) {
		super(matchingStrategy);
		ValidateUtils.validateServerValueIsAvailable(matchingStrategy, "Query parameter \'" + name + "\'");
		this.name = name;
	}

	public QueryParameter(String name, Object value) {
		super(ContractUtils.CLIENT_VALUE.apply(value), ContractUtils.SERVER_VALUE.apply(value));
		ValidateUtils.validateServerValueIsAvailable(value, "Query parameter \'" + name + "\'");
		this.name = name;
	}

	public static QueryParameter build(String key, Object value) {
		if (value instanceof MatchingStrategy) {
			return new QueryParameter(key, (MatchingStrategy) value);
		}
		else if (value instanceof RegexProperty) {
			return new QueryParameter(key, ((RegexProperty) value).dynamicClientEscapedConcreteProducer());
		}
		return new QueryParameter(key, value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		QueryParameter that = (QueryParameter) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name);
	}

	@Override
	public String toString() {
		return "QueryParameter{" + "name='" + name + '\'' + ", value=" + super.toString() + '}';
	}

}
