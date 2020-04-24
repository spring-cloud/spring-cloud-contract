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

import java.util.Map;
import java.util.Objects;

/**
 * Represents a property that has name and content. Used together with multipart requests.
 *
 * @since 1.0.0
 */
public class NamedProperty {

	private static final String NAME = "name";

	private static final String CONTENT = "content";

	private static final String CONTENT_TYPE = "contentType";

	private DslProperty name;

	private DslProperty value;

	private DslProperty contentType;

	public NamedProperty(DslProperty name, DslProperty value) {
		this.name = name;
		this.value = value;
		this.contentType = null;
	}

	public NamedProperty(DslProperty name, DslProperty value, DslProperty contentType) {
		this.name = name;
		this.value = value;
		this.contentType = contentType;
	}

	public NamedProperty(Map<String, DslProperty> namedMap) {
		this(asDslProperty(value(namedMap, NAME)),
				asDslProperty(value(namedMap, CONTENT)),
				asDslProperty(value(namedMap, CONTENT_TYPE)));
	}

	private static DslProperty value(Map<String, DslProperty> namedMap, String key) {
		if (namedMap == null) {
			return null;
		}

		return namedMap.get(key);
	}

	public static DslProperty asDslProperty(Object o) {
		if (o == null) {
			return null;
		}

		if (o instanceof DslProperty) {
			return ((DslProperty) (o));
		}

		return new DslProperty(o);
	}

	public DslProperty getName() {
		return name;
	}

	public void setName(DslProperty name) {
		this.name = name;
	}

	public DslProperty getValue() {
		return value;
	}

	public void setValue(DslProperty value) {
		this.value = value;
	}

	public DslProperty getContentType() {
		return contentType;
	}

	public void setContentType(DslProperty contentType) {
		this.contentType = contentType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		NamedProperty that = (NamedProperty) o;
		return Objects.equals(name, that.name) && Objects.equals(value, that.value)
				&& Objects.equals(contentType, that.contentType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value, contentType);
	}

	@Override
	public String toString() {
		return "NamedProperty{" + "name=" + name + ", value=" + value + ", contentType="
				+ contentType + '}';
	}

}
