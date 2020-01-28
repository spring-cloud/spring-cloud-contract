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
 * Represents a matching strategy for a JSON.
 *
 * @since 1.0.0
 */
public class MatchingStrategy extends DslProperty {

	private Type type;

	private JSONCompareMode jsonCompareMode;

	public MatchingStrategy(Object value, Type type) {
		this(value, type, null);
	}

	public MatchingStrategy(Object value, Type type, JSONCompareMode jsonCompareMode) {
		super(value);
		this.type = type;
		this.jsonCompareMode = jsonCompareMode;
	}

	public MatchingStrategy(DslProperty value, Type type) {
		this(value, type, null);
	}

	public MatchingStrategy(DslProperty value, Type type,
			JSONCompareMode jsonCompareMode) {
		super(value.getClientValue(), value.getServerValue());
		this.type = type;
		this.jsonCompareMode = jsonCompareMode;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public JSONCompareMode getJsonCompareMode() {
		return jsonCompareMode;
	}

	public void setJsonCompareMode(JSONCompareMode jsonCompareMode) {
		this.jsonCompareMode = jsonCompareMode;
	}

	@Override
	public String toString() {
		return "MatchingStrategy{" + "type=" + type + ", jsonCompareMode="
				+ jsonCompareMode + '}';
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
		MatchingStrategy that = (MatchingStrategy) o;
		return type == that.type && jsonCompareMode == that.jsonCompareMode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), type, jsonCompareMode);
	}

	public enum Type {

		/**
		 * Equality check.
		 */
		EQUAL_TO("equalTo"),

		/**
		 * Contains check.
		 */
		CONTAINS("containing"),

		/**
		 * Matching check.
		 */
		MATCHING("matching"),

		/**
		 * Not matching check.
		 */
		NOT_MATCHING("notMatching"),

		/**
		 * Equal to JSON check.
		 */
		EQUAL_TO_JSON("equalToJson"),

		/**
		 * Equal to XML check.
		 */
		EQUAL_TO_XML("equalToXml"),

		/**
		 * Absence check.
		 */
		ABSENT("absent"),

		/**
		 * Binary equality check.
		 */
		BINARY_EQUAL_TO("binaryEqualTo");

		private final String name;

		Type(String name) {
			this.name = name;
		}

		public final String getName() {
			return name;
		}

	}

}
