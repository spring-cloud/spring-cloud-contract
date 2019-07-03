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
import java.util.Objects;

/**
 * Represents an element of a DSL that can contain client or sever side values
 *
 * @since 1.0.0
 */
public class DslProperty<T> implements Serializable {

	private final T clientValue;

	private final T serverValue;

	public DslProperty(T clientValue, T serverValue) {
		this.clientValue = clientValue;
		this.serverValue = serverValue;
	}

	public DslProperty(T singleValue) {
		this.clientValue = singleValue;
		this.serverValue = singleValue;
	}

	public boolean isSingleValue() {
		return this.clientValue.equals(this.serverValue)
				|| (this.clientValue != null && this.serverValue == null)
				|| (this.serverValue != null && this.clientValue == null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DslProperty<?> that = (DslProperty<?>) o;
		return Objects.equals(clientValue, that.clientValue)
				&& Objects.equals(serverValue, that.serverValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientValue, serverValue);
	}

	@Override
	public String toString() {
		return "DslProperty{" + "clientValue=" + clientValue + ", serverValue="
				+ serverValue + '}';
	}

	public final T getClientValue() {
		return clientValue;
	}

	public final T getServerValue() {
		return serverValue;
	}

}
