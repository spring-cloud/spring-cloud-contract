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

package org.springframework.cloud.contract.verifier.spec.pact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.com.dius.pact.core.model.ProviderState;

import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;
import org.springframework.lang.NonNull;

public class PactMetaData implements SpringCloudContractMetadata {

	/**
	 * Key under which this metadata entry can be found in contract's metadata.
	 */
	public static final String METADATA_KEY = "pact";

	/**
	 * Metadata for adding {@link ProviderState} in Pact contract.
	 */
	private List<ProviderStateMetadata> providerStates = new ArrayList<>();

	public List<ProviderStateMetadata> getProviderStates() {
		return this.providerStates;
	}

	public void setProviderStates(List<ProviderStateMetadata> providerStates) {
		this.providerStates = providerStates;
	}

	@NonNull
	public static PactMetaData fromMetadata(Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, PactMetaData.METADATA_KEY, new PactMetaData());
	}

	@Override
	public String key() {
		return METADATA_KEY;
	}

	@Override
	public String description() {
		return "Metadata for converting Contract to Pact";
	}

	/**
	 * {@link ProviderState} metadata.
	 */
	public static class ProviderStateMetadata {

		/**
		 * If set, will be added to PACT provider states.
		 */
		private String name;

		private Map<String, Object> params;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Map<String, Object> getParams() {
			return this.params;
		}

		public void setParams(Map<String, Object> params) {
			this.params = params;
		}

	}
}
