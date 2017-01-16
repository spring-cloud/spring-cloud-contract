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

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.util.StreamUtils;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * Represents a single JSON file that was found in the folder with potential WireMock
 * stubs
 */
class WiremockMappingDescriptor {

	final File descriptor;

	public WiremockMappingDescriptor(File mappingDescriptor) {
		this.descriptor = mappingDescriptor;
	}

	public StubMapping getMapping() {
		try {
			return StubMapping.buildFrom(StreamUtils.copyToString(
					new FileInputStream(this.descriptor), Charset.forName("UTF-8")));
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot read file", e);
		}
	}

	@Override
	public String toString() {
		return "WiremockMappingDescriptor [descriptor=" + this.descriptor + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.descriptor == null) ? 0 : this.descriptor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WiremockMappingDescriptor other = (WiremockMappingDescriptor) obj;
		if (this.descriptor == null) {
			if (other.descriptor != null)
				return false;
		}
		else if (!this.descriptor.equals(other.descriptor))
			return false;
		return true;
	}
}
