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

/**
 * Represents a http cookie.
 *
 * @author Alex Xandra Albert Sim
 * @since 1.2.5
 */
public class Cookie extends DslProperty {

	private String key;

	public Cookie(String key, DslProperty dslProperty) {
		super(dslProperty.getClientValue(), dslProperty.getServerValue());
		this.key = key;
	}

	public Cookie(String key, MatchingStrategy value) {
		super(value);
		this.key = key;
	}

	public Cookie(String key, Object value) {
		super(ContractUtils.CLIENT_VALUE.apply(value),
				ContractUtils.SERVER_VALUE.apply(value));
		this.key = key;
	}

	public static Cookie build(String key, Object value) {
		if (value instanceof MatchingStrategy) {
			return new Cookie(key, (MatchingStrategy) value);
		}
		return new Cookie(key, value);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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
		Cookie cookie = (Cookie) o;
		return Objects.equals(key, cookie.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), key);
	}

	@Override
	public String toString() {
		return "Cookie{" + "key='" + key + '\'' + "}, value=" + super.toString();
	}

}
