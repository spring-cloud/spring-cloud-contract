/*
 *  Copyright 2013-2018 the original author or authors.
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
import java.util.Map;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Marcin Grzejszczak
 */
public class PactStubDownloaderBuilderTests {

	@Test public void isProtocolAccepted() {
	}

	@Test public void build() {
	}

	@Test public void resolve() {
		StubRunnerOptions options = new StubRunnerOptionsBuilder().build();
		PactStubDownloader downloader = new PactStubDownloader(options);

		Map.Entry<StubConfiguration, File> entry = downloader
				.downloadAndUnpackStubJar(new StubConfiguration("a:b:c:0.1"));

		BDDAssertions.then(entry).isNotNull();
	}
}