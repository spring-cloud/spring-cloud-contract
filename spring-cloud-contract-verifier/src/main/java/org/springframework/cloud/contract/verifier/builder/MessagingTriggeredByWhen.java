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

package org.springframework.cloud.contract.verifier.builder;

import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class MessagingTriggeredByWhen implements When {

	private final BlockBuilder blockBuilder;

	MessagingTriggeredByWhen(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented(metadata.getContract().getInput().getTriggeredBy().getExecutionCommand())
			.addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getTriggeredBy() != null;
	}

}
