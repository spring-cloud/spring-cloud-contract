/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl;

import java.util.Map;

import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;
import org.springframework.lang.NonNull;

/**
 * Metadata representation of the Contract Verifier.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class ContractVerifierMetadata implements SpringCloudContractMetadata {

	/**
	 * Metadata entry in the contract.
	 */
	public static final String METADATA_KEY = "verifier";

	public ContractVerifierMetadata(String tool) {
		this.tool = tool;
	}

	public ContractVerifierMetadata() {
	}

	private String tool;

	public String getTool() {
		return this.tool;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	@NonNull
	public static ContractVerifierMetadata fromMetadata(Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, METADATA_KEY,
				new ContractVerifierMetadata());
	}

	@Override
	public String key() {
		return METADATA_KEY;
	}

	@Override
	public String description() {
		return "Metadata entries used by the framework";
	}

}
