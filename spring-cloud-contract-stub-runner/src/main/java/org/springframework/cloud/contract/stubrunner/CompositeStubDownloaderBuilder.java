/*
 * Copyright 2013-2017 the original author or authors.
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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class CompositeStubDownloaderBuilder implements StubDownloaderBuilder {

	private final List<StubDownloaderBuilder> builders;

	CompositeStubDownloaderBuilder(List<StubDownloaderBuilder> builders) {
		this.builders = builders;
	}

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (this.builders == null) {
			return null;
		}
		return new CompositeStubDownloader(this.builders, stubRunnerOptions);
	}
}

class CompositeStubDownloader implements StubDownloader {

	private static final Log log = LogFactory.getLog(CompositeStubDownloader.class);

	private final List<StubDownloaderBuilder> builders;
	private final StubRunnerOptions stubRunnerOptions;

	CompositeStubDownloader(List<StubDownloaderBuilder> builders,
			StubRunnerOptions stubRunnerOptions) {
		this.builders = builders;
		this.stubRunnerOptions = stubRunnerOptions;
		if (log.isDebugEnabled()) {
			log.debug("Registered following stub downloaders " + this.builders
					.stream()
			.map(b -> b.getClass().getName())
			.collect(Collectors.toList()));
		}
	}

	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		for (StubDownloaderBuilder builder : this.builders) {
			StubDownloader downloader = builder.build(this.stubRunnerOptions);
			if (downloader == null) {
				continue;
			}
			if (log.isDebugEnabled()) {
				log.debug("Found a matching stub downloader [" + downloader.getClass().getName() + "]");
			}
			Map.Entry<StubConfiguration, File> entry = downloader
					.downloadAndUnpackStubJar(stubConfiguration);
			if (entry != null) {
				if (log.isDebugEnabled()) {
					log.debug("Found a matching entry [" + entry + "] by stub downloader [" + downloader.getClass().getName() + "]");
				}
				return entry;
			} else {
				log.warn("Stub Downloader [" + downloader.getClass().getName() + "] "
						+ "failed to find an entry for [" + stubConfiguration.toColonSeparatedDependencyNotation() + "]. "
						+ "Will proceed to the next one");
			}
		}
		return null;
	}
}
