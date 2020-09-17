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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QueryParameters {

	private List<QueryParameter> parameters = new LinkedList<QueryParameter>();

	public void parameter(Map<String, Object> singleParameter) {
		Iterator<Map.Entry<String, Object>> iterator = singleParameter.entrySet().iterator();
		if (iterator.hasNext()) {
			Map.Entry<String, Object> first = iterator.next();
			if (first != null) {
				parameters.add(QueryParameter.build(first.getKey(), first.getValue()));
			}
		}
	}

	public void parameter(String parameterName, Object parameterValue) {
		parameters.add(QueryParameter.build(parameterName, parameterValue));
	}

	public List<QueryParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<QueryParameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		QueryParameters that = (QueryParameters) o;
		return Objects.equals(parameters, that.parameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parameters);
	}

	@Override
	public String toString() {
		return "QueryParameters{" + "\nparameters=" + parameters + '}';
	}

}
