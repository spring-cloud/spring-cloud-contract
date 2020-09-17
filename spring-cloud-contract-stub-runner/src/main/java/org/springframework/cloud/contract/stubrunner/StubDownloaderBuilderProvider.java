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

package org.springframework.cloud.contract.stubrunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Provider for {@link StubDownloaderBuilder}. It can also pick a default downloader if
 * none is provided
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class StubDownloaderBuilderProvider {

	private final List<StubDownloaderBuilder> builders = new ArrayList<>();

	public StubDownloaderBuilderProvider() {
		this.builders.addAll(SpringFactoriesLoader.loadFactories(StubDownloaderBuilder.class, null));
	}

	StubDownloaderBuilderProvider(List<StubDownloaderBuilder> builders) {
		this.builders.addAll(builders);
	}

	/**
	 * @param stubRunnerOptions options of Stub Runner
	 * @param additionalBuilders - optional array of {@link StubDownloaderBuilder}s to
	 * append to the list of builders
	 * @return composite {@link StubDownloader} that iterates over a list of stub
	 * downloaders
	 */
	public StubDownloader get(StubRunnerOptions stubRunnerOptions, StubDownloaderBuilder... additionalBuilders) {
		List<StubDownloaderBuilder> builders = this.builders;
		if (additionalBuilders != null) {
			builders.addAll(Arrays.asList(additionalBuilders));
		}
		List<StubDownloaderBuilder> defaultBuilders = defaultStubDownloaderBuilders();
		builders.addAll(defaultBuilders);
		return new CompositeStubDownloader(builders, stubRunnerOptions);
	}

	List<StubDownloaderBuilder> defaultStubDownloaderBuilders() {
		return Arrays.asList(new ScmStubDownloaderBuilder(), new ClasspathStubProvider(), new FileStubDownloader(),
				new AetherStubDownloaderBuilder());
	}

}
