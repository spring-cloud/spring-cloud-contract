/*
 * Copyright 2013-2023 the original author or authors.
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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a part of a multipart request.
 *
 * @since 4.0.3
 */
public class Part {

	private final DslProperty filename;

	private final DslProperty value;

	private final DslProperty contentType;

	private final DslProperty contentTransferEncoding;

	public Part(Map<String, Object> properties) {
		this(value(properties, "filename"), value(properties, "value"), value(properties, "contentType"),
				value(properties, "contentTransferEncoding"));
	}

	public Part(Object value) {
		this(value, null);
	}

	public Part(Object value, Object contentType) {
		this(value, contentType, null);
	}

	public Part(Object filename, Object value, Object contentType) {
		this(filename, value, contentType, null);
	}

	public Part(Object filename, Object value, Object contentType, Object contentTransferEncoding) {
		this.filename = new DslProperty<>(ContractUtils.CLIENT_VALUE.apply(filename),
				ContractUtils.SERVER_VALUE.apply(filename));
		this.value = new DslProperty<>(ContractUtils.CLIENT_VALUE.apply(value),
				ContractUtils.SERVER_VALUE.apply(value));
		this.contentType = new DslProperty<>(ContractUtils.CLIENT_VALUE.apply(contentType),
				ContractUtils.SERVER_VALUE.apply(contentType));
		this.contentTransferEncoding = new DslProperty<>(ContractUtils.CLIENT_VALUE.apply(contentTransferEncoding),
				ContractUtils.SERVER_VALUE.apply(contentTransferEncoding));
	}

	private static Object value(Map<String, Object> map, String key) {
		return Optional.ofNullable(map).orElse(Collections.emptyMap()).get(key);
	}

	public DslProperty getFilename() {
		return filename;
	}

	public DslProperty getValue() {
		return value;
	}

	public DslProperty getContentType() {
		return contentType;
	}

	public DslProperty getContentTransferEncoding() {
		return contentTransferEncoding;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Part part = (Part) o;
		return Objects.equals(filename, part.filename) && Objects.equals(value, part.value)
				&& Objects.equals(contentType, part.contentType)
				&& Objects.equals(contentTransferEncoding, part.contentTransferEncoding);
	}

	@Override
	public int hashCode() {
		return Objects.hash(filename, value, contentType, contentTransferEncoding);
	}

	@Override
	public String toString() {
		return "Part{" + "filename=" + filename + ", value=" + value + ", contentType=" + contentType
				+ ", contentTransferEncoding=" + contentTransferEncoding + '}';
	}

}
