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
 * Matching type with corresponding values.
 */
public class MatchingTypeValue {

	private MatchingType type;

	/**
	 * Value to check.
	 */
	private Object value;

	/**
	 * Min occurrence when matching by type.
	 */
	private Integer minTypeOccurrence;

	/**
	 * Max occurrence when matching by type.
	 */
	private Integer maxTypeOccurrence;

	MatchingTypeValue() {
	}

	MatchingTypeValue(MatchingType type) {
		this.type = type;
	}

	MatchingTypeValue(MatchingType type, Object value) {
		this.type = type;
		this.value = value;
	}

	MatchingTypeValue(MatchingType type, Object value, Integer minTypeOccurrence) {
		this.type = type;
		this.value = value;
		this.minTypeOccurrence = minTypeOccurrence;
	}

	MatchingTypeValue(MatchingType type, Object value, Integer minTypeOccurrence,
			Integer maxTypeOccurrence) {
		this.type = type;
		this.value = value;
		this.minTypeOccurrence = minTypeOccurrence;
		this.maxTypeOccurrence = maxTypeOccurrence;
	}

	public MatchingType getType() {
		return type;
	}

	public void setType(MatchingType type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Integer getMinTypeOccurrence() {
		return minTypeOccurrence;
	}

	public void setMinTypeOccurrence(Integer minTypeOccurrence) {
		this.minTypeOccurrence = minTypeOccurrence;
	}

	public Integer getMaxTypeOccurrence() {
		return maxTypeOccurrence;
	}

	public void setMaxTypeOccurrence(Integer maxTypeOccurrence) {
		this.maxTypeOccurrence = maxTypeOccurrence;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MatchingTypeValue value1 = (MatchingTypeValue) o;
		return type == value1.type && Objects.equals(value, value1.value)
				&& Objects.equals(minTypeOccurrence, value1.minTypeOccurrence)
				&& Objects.equals(maxTypeOccurrence, value1.maxTypeOccurrence);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, value, minTypeOccurrence, maxTypeOccurrence);
	}

	@Override
	public String toString() {
		return "MatchingTypeValue{" + "type=" + type + ", value=" + value
				+ ", minTypeOccurrence=" + minTypeOccurrence + ", maxTypeOccurrence="
				+ maxTypeOccurrence + '}';
	}

}
