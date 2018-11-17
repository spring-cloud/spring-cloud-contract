/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.nio.charset.Charset

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
	final Charset charset
	final Class type

	FromFileProperty(File file, Class type) {
		this(file, type, Charset.defaultCharset())
	}

	FromFileProperty(File file, Class type, Charset charset) {
		this.file = file
		this.type = type
		this.charset = charset
	}

	boolean isString() {
		return this.type == String
	}

	boolean isByte() {
		return this.type == byte[]
	}

	public <T> T asType(Class<T> clazz) {
		switch (clazz) {
			case String:
				return (T) new String(file.bytes, this.charset)
			case byte[]:
				return (T) file.bytes
			default:
				throw new UnsupportedOperationException("${clazz} is not supported")
		}
	}
}
