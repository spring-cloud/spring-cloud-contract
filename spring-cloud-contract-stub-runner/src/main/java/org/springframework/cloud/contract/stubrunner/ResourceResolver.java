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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Uses {@code META-INF/spring.factories} to read {@link ProtocolResolver} list
 * that gets added to {@link DefaultResourceLoader}. Each implementor of a new
 * {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}, if
 * one uses a new protocol, should register their own {@link ProtocolResolver} so
 * that Stub Runner can convert a {@link String} version of a URI to a {@link Resource}.
 *
 * IMPORTANT! Internal tool. Do not use.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
public class ResourceResolver {

	private static final Log log = LogFactory.getLog(ResourceResolver.class);
	private static final List<ProtocolResolver> RESOLVERS = new ArrayList<>();
	private static final DefaultResourceLoader LOADER = new DefaultResourceLoader();

	static {
		RESOLVERS.addAll(
				SpringFactoriesLoader.loadFactories(StubDownloaderBuilder.class, null)
		);
		RESOLVERS.addAll(new StubDownloaderBuilderProvider().defaultStubDownloaderBuilders());
		for (ProtocolResolver resolver : RESOLVERS) {
			LOADER.addProtocolResolver(resolver);
		}
	}

	/**
	 * @param url - string url
	 * @return corresponding {@link Resource}
	 */
	public static Resource resource(String url) {
		try {
			return LOADER.getResource(url);
		} catch (Exception e) {
			log.error("Exception occurred while trying to read the resource [" + url + "]", e);
			return null;
		}
	}
}
