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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a body of a request / response or a message.
 *
 * @since 1.0.0
 */
public class Body extends DslProperty {

	public Body(Map<String, DslProperty> body) {
		super(extractValue(body, ContractUtils.CLIENT_VALUE),
				extractValue(body, ContractUtils.SERVER_VALUE));
	}

	public Body(List<DslProperty> bodyAsList) {
		super(bodyAsList.stream().map(DslProperty::getClientValue)
				.collect(Collectors.toList()),
				bodyAsList.stream().map(DslProperty::getServerValue)
						.collect(Collectors.toList()));
	}

	public Body(Object value) {
		super(ContractUtils.CLIENT_VALUE.apply(value),
				ContractUtils.SERVER_VALUE.apply(value));
	}

	public Body(Byte[] bodyAsValue) {
		super(bodyAsValue);
	}

	public Body(Number bodyAsValue) {
		super(bodyAsValue);
	}

	public Body(String bodyAsValue) {
		super(bodyAsValue, bodyAsValue);
	}

	public Body(DslProperty bodyAsValue) {
		super(bodyAsValue.getClientValue(), bodyAsValue.getServerValue());
	}

	public Body(Serializable serializable) {
		super(serializable, serializable);
	}

	public Body(MatchingStrategy matchingStrategy) {
		super(matchingStrategy, matchingStrategy);
	}

	private static Map<String, Object> extractValue(Map<String, DslProperty> body,
			final Function valueProvider) {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		body.forEach((key, value) -> map.put(key, valueProvider.apply(value)));
		return map;
	}

}
