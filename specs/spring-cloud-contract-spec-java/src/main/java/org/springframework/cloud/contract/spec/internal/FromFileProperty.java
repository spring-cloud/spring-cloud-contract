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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * Represents a property that will become a File content.
 *
 * @since 2.1.0
 */
public class FromFileProperty implements Serializable {

	private final File file;

	private final String charset;

	private final Class type;

	public FromFileProperty(File file, Class type) {
		this(file, type, Charset.defaultCharset());
	}

	public FromFileProperty(File file, Class type, Charset charset) {
		this.file = file;
		this.type = type;
		this.charset = charset.toString();
	}

	public boolean isString() {
		return String.class.equals(this.type);
	}

	public boolean isByte() {
		return byte[].class.equals(this.type) || Byte[].class.equals(this.type);
	}

	public String asString() {
		try {
			return new String(asBytes(), this.charset);
		}
		catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public String fileName() {
		return this.file.getName();
	}

	public byte[] asBytes() {
		try {
			return Files.readAllBytes(this.file.toPath());
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public String toString() {
		return asString();
	}

	public final File getFile() {
		return file;
	}

	public final String getCharset() {
		return charset;
	}

	public final Class getType() {
		return type;
	}

}
