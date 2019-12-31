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

package org.springframework.cloud.contract.spec.internal

import java.nio.charset.Charset

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a property that will become a File content
 *
 * @since 2.1.0
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class FromFileProperty implements Serializable {

	final File file
	final String charset
	final Class type

	FromFileProperty(File file, Class type) {
		this(file, type, Charset.defaultCharset())
	}

	FromFileProperty(File file, Class type, Charset charset) {
		this.file = file
		this.type = type
		this.charset = charset.toString()
	}

	boolean isString() {
		return this.type == String
	}

	boolean isByte() {
		return this.type == byte[]
	}

	String asString() {
		return new String(this.file.bytes, this.charset)
	}

	String fileName() {
		return this.file.getName()
	}

	byte[] asBytes() {
		return this.file.bytes
	}

	boolean isJson() {
		return this.fileName().endsWith(".json")
	}

	boolean isXml() {
		return this.fileName().endsWith(".xml")
	}

	@Override
	String toString() {
		return asString()
	}
}
