/*
 *  Copyright 2013-2016 the original author or authors.
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

import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Builder for a {@link StubDownloader}. Can't allow direct usage
 * of {@link StubDownloader} cause in order to register instances
 * of this interface in {@link org.springframework.core.io.support.SpringFactoriesLoader}
 * one needs a default constructor whereas the {@link StubDownloader}
 * instances need to be constructed from stub related options.
 *
 * Since {@code 2.0.0} extends {@link ProtocolResolver}. Implementations have
 * to tell Spring how to parse the repository root String into a resource.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public interface StubDownloaderBuilder extends ProtocolResolver {

	/**
	 * @return {@link StubDownloader} instance of {@code null} if current parameters don't allow building the instance
	 */
	StubDownloader build(StubRunnerOptions stubRunnerOptions);

	@Override default Resource resolve(String location, ResourceLoader resourceLoader) {
		return null;
	}
}