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

/**
 * Represents a header of a request / response or a message.
 *
 * @since 1.0.0
 */
public class Header extends DslProperty {

	private String name;

	public Header(String name, DslProperty dslProperty) {
		super(dslProperty.getClientValue(), dslProperty.getServerValue());
		this.name = name;
	}

	public Header(String name, MatchingStrategy value) {
		super(value);
		this.name = name;
	}

	public Header(String name, Object value) {
		super(ContractUtils.CLIENT_VALUE.apply(value),
				ContractUtils.SERVER_VALUE.apply(value));
		this.name = name;
	}

	public static Header build(String key, Object value) {
		if (value instanceof MatchingStrategy) {
			return new Header(key, (MatchingStrategy) value);
		}
		return new Header(key, value);
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
		Header header = (Header) o;
		return Objects.equals(name, header.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name);
	}

	@Override
	public String toString() {
		return "Header{" + "\nname='" + name + '\'' + "} \n" + super.toString();
	}

}
