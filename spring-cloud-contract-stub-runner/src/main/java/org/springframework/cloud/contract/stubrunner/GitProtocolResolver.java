/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.springframework.cloud.contract.stubrunner.util.StringUtils;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Git version of a {@link ProtocolResolver}. Registered in {@code META-INF/spring.factories}
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class GitProtocolResolver implements ProtocolResolver {

	private static final String GIT_PROTOCOL = "git";

	@Override public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (!StringUtils.hasText(location) || !location.startsWith(GIT_PROTOCOL)) {
			return null;
		}
		return new GitResource(location);
	}
}

/**
 * Primitive version of a Git {@link Resource}
 */
class GitResource extends AbstractResource {

	private final String rawLocation;

	GitResource(String location) {
		this.rawLocation = location;
	}

	@Override public String getDescription() {
		return this.rawLocation;
	}

	@Override public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override public URI getURI() throws IOException {
		return URI.create(this.rawLocation);
	}
}
