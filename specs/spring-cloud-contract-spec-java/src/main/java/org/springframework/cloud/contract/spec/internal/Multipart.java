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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Multipart extends DslProperty {

	public Multipart(Map<String, DslProperty> multipart) {
		super(extractValue(multipart, ContractUtils.CLIENT_VALUE),
				extractValue(multipart, ContractUtils.SERVER_VALUE));
	}

	public Multipart(List<DslProperty> multipartAsList) {
		super(multipartAsList.stream().map(DslProperty::getClientValue)
				.collect(Collectors.toList()),
				multipartAsList.stream().map(DslProperty::getServerValue)
						.collect(Collectors.toList()));
	}

	public Multipart(Object value) {
		super(ContractUtils.CLIENT_VALUE.apply(value),
				ContractUtils.SERVER_VALUE.apply(value));
	}

	public Multipart(DslProperty multipartAsValue) {
		super(multipartAsValue.getClientValue(), multipartAsValue.getServerValue());
	}

	public Multipart(MatchingStrategy matchingStrategy) {
		super(matchingStrategy);
	}

	public static Multipart build(Object value) {
		if (value instanceof MatchingStrategy) {
			return new Multipart((MatchingStrategy) value);
		}
		return new Multipart(value);
	}

	private static Map<String, Object> extractValue(Map<String, DslProperty> multipart,
			final Function valueProvider) {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		multipart.forEach(
				(s, dslProperty) -> map.put(s, valueProvider.apply(dslProperty)));
		return map;
	}

}
