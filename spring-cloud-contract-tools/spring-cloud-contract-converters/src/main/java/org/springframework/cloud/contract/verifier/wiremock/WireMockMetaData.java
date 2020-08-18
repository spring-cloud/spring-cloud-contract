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

package org.springframework.cloud.contract.verifier.wiremock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;

public class WireMockMetaData implements SpringCloudContractMetadata {

	/**
	 * Key under which this metadata entry can be found in contract's metadata.
	 */
	public static final String METADATA_KEY = "wiremock";

	/**
	 * Applicable classes for Stub Mapping.
	 */
	static final List<Class> APPLICABLE_CLASSES = Arrays.asList(String.class, StubMapping.class, Map.class);

	/**
	 * {@link StubMapping} represented by one of the classes in
	 * {@link #APPLICABLE_CLASSES}.
	 */
	private Object stubMapping;

	public Object getStubMapping() {
		return stubMapping;
	}

	public void setStubMapping(Object stubMapping) {
		this.stubMapping = stubMapping;
	}

	public static WireMockMetaData fromMetadata(Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, WireMockMetaData.METADATA_KEY, new WireMockMetaData());
	}

	@Override
	public String key() {
		return METADATA_KEY;
	}

	@Override
	public String description() {
		return "Metadata for extending WireMock stubs.\n\nStubMapping can be " + "one of the following classes "
				+ APPLICABLE_CLASSES.stream().map(Class::getSimpleName).collect(Collectors.toList()) + ". Please check "
				+ "the http://wiremock.org/docs/stubbing/ for more information about the StubMapping class properties.";
	}

	@Override
	public List<Class> additionalClassesToLookAt() {
		return Collections.singletonList(StubMapping.class);
	}

}
