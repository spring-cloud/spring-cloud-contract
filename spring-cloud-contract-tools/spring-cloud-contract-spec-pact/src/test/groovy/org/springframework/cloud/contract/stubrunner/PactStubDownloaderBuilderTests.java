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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactSource;
import au.com.dius.pact.model.RequestResponsePact;
import au.com.dius.pact.provider.junit.loader.PactLoader;
import au.com.dius.pact.provider.junit.sysprops.ValueResolver;
import org.assertj.core.api.BDDAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

/**
 * @author Marcin Grzejszczak
 */
public class PactStubDownloaderBuilderTests {

	@Test
	public void should_retrieve_pacts_from_broker() {
		StubRunnerOptions options = new StubRunnerOptionsBuilder()
				.withProperties(props())
				.build();
		PactStubDownloader downloader = new PactStubDownloader(options) {
			@NotNull @Override PactLoader pactBrokerLoader(ValueResolver resolver,
					List<String> tags) {
				return new PactLoader() {
					@Override public List<Pact> load(String providerName) {
						return Collections.singletonList(
								new RequestResponsePact(null, null,
										Collections.emptyList()));
					}
					@Override public PactSource getPactSource() {
						return null;
					}
				};
			}
		};

		Map.Entry<StubConfiguration, File> entry = downloader
				.downloadAndUnpackStubJar(new StubConfiguration("group:bobby:+:classifier"));

		BDDAssertions.then(entry).isNotNull();
	}

	Map<String, String> props() {
		Map<String, String> map = new HashMap<>();
//		map.put("pactbroker.host", "localhost");
//		map.put("pactbroker.port", String.valueOf(this.port));
//		map.put("pactbroker.host", "test.pact.dius.com.au");
//		map.put("pactbroker.port", "443");
//		map.put("pactbroker.protocol", "https");
//		map.put("pactbroker.auth.scheme", "Basic");
//		map.put("pactbroker.auth.username", "dXfltyFMgNOFZAxr8io9wJ37iUpY42M");
//		map.put("pactbroker.auth.password", "O5AIZWxelWbLvqMd8PkAVycBJh2Psyg1");
		return map;
	}

	//	@After
	//	public void tearDown() {
	//		SnapshotRecordResult recording = WireMock.stopRecording();
	//		List<StubMapping> mappings = recording.getStubMappings();
	//		storeMappings(mappings);
	//	}

//	private void recordFromBroker() {
//		WireMock.startRecording(WireMock.recordSpec()
//				.forTarget("https://test.pact.dius.com.au")
//				.extractTextBodiesOver(9999999L)
//				.extractBinaryBodiesOver(9999999L)
//				.makeStubsPersistent(false));
//	}

	//	private void storeMappings(List<StubMapping> mappings) {
	//		try {
	//			File proxiedStubs = new File("target/stubs");
	//			proxiedStubs.mkdirs();
	//			for (StubMapping mapping : mappings) {
	//				File stub = new File(proxiedStubs, "foo" + ".json");
	//				stub.createNewFile();
	//				Files.write(stub.toPath(), mapping.toString().getBytes());
	//			}
	//		} catch (Exception e) {
	//			throw new RuntimeException(e);
	//		}
	//	}
}