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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;

public class StubsStubDownloaderTests {

	URL url = StubsStubDownloaderTests.class.getResource("/repository/mappings");

	@Test
	public void should_pick_stubs_from_a_given_location() {
		String path = url.getPath();
		StubRunnerOptions options = new StubRunnerOptionsBuilder().withStubRepositoryRoot("stubs://file://" + path)
				.build();
		StubsStubDownloader downloader = new StubsStubDownloader(options);

		Map.Entry<StubConfiguration, File> entry = downloader
				.downloadAndUnpackStubJar(new StubConfiguration("lv.spring.cloud:bye"));

		BDDAssertions.then(entry).isNotNull();
		BDDAssertions.then(entry.getValue()).exists();
		BDDAssertions.then(new File(entry.getValue(), "pl/spring/cloud/bye/pl_bye.json")).exists();
		BDDAssertions.then(new File(entry.getValue(), "lv/spring/cloud/bye/lv_bye.json")).exists();
	}

	@Test
	public void should_pick_stubs_from_a_given_location_for_a_find_producer_with_ga() {
		String path = url.getPath();
		StubRunnerOptions options = new StubRunnerOptionsBuilder().withStubRepositoryRoot("stubs://file://" + path)
				.withProperties(propsWithFindProducer()).build();
		StubsStubDownloader downloader = new StubsStubDownloader(options);

		Map.Entry<StubConfiguration, File> entry = downloader
				.downloadAndUnpackStubJar(new StubConfiguration("lv.spring.cloud:bye"));

		BDDAssertions.then(entry).isNotNull();
		File stub = new File(entry.getValue().getPath(), "lv/spring/cloud/bye/lv_bye.json");
		BDDAssertions.then(stub).exists();
	}

	@Test
	public void should_pick_stubs_from_a_given_location_for_a_find_producer_with_gav() {
		String path = url.getPath();
		StubRunnerOptions options = new StubRunnerOptionsBuilder().withStubRepositoryRoot("stubs://file://" + path)
				.withProperties(propsWithFindProducer()).build();
		StubsStubDownloader downloader = new StubsStubDownloader(options);

		Map.Entry<StubConfiguration, File> entry = downloader
				.downloadAndUnpackStubJar(new StubConfiguration("lv.spring:cloud:bye"));

		BDDAssertions.then(entry).isNotNull();
		File stub = new File(entry.getValue().getPath(), "lv/spring/cloud/bye/lv_bye.json");
		BDDAssertions.then(stub).exists();
	}

	private Map<String, String> propsWithFindProducer() {
		Map<String, String> map = new HashMap<>();
		map.put("stubs.find-producer", "true");
		return map;
	}

}
