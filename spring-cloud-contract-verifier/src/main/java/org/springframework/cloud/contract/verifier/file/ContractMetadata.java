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

package org.springframework.cloud.contract.verifier.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.cloud.contract.spec.Contract;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Contains metadata for a particular file with a DSL.
 *
 * @author Jakub Kubrynski, codearte.io
 * @since 1.0.0
 */
public class ContractMetadata {

	/**
	 * Path to the file.
	 */
	private final Path path;

	/**
	 * Should the contract be ignored.
	 */
	private final boolean ignored;

	/**
	 * How many files are there in the folder.
	 */
	private final int groupSize;

	/**
	 * If scenario related will contain an order of execution.
	 */
	private final Integer order;

	/**
	 * The list of contracts for the given file.
	 */
	private final List<Contract> convertedContract = new ArrayList<>();

	/**
	 * Converted contracts with meta data information.
	 */
	private final Collection<SingleContractMetadata> convertedContractWithMetadata = new ArrayList<>();

	public ContractMetadata(Path path, boolean ignored, int groupSize, Integer order, Contract convertedContract) {
		this(path, ignored, groupSize, order, singletonList(convertedContract));
	}

	public ContractMetadata(Path path, boolean ignored, int groupSize, Integer order,
			Collection<Contract> convertedContract) {
		this.groupSize = groupSize;
		this.path = path;
		this.ignored = ignored;
		this.order = order;
		this.convertedContract.addAll(convertedContract);
		this.convertedContractWithMetadata.addAll(this.convertedContract.stream().filter(Objects::nonNull)
				.map(it -> new SingleContractMetadata(it, this)).collect(toList()));
	}

	public SingleContractMetadata forContract(Contract contract) {
		return this.convertedContractWithMetadata.stream().filter(it -> it.getContract().equals(contract)).findFirst()
				.orElse(null);
	}

	public boolean anyInProgress() {
		return this.convertedContract.stream().anyMatch(Contract::getInProgress);
	}

	public Path getPath() {
		return path;
	}

	public boolean getIgnored() {
		return ignored;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public int getGroupSize() {
		return groupSize;
	}

	public Integer getOrder() {
		return order;
	}

	public List<Contract> getConvertedContract() {
		return convertedContract;
	}

	public Collection<SingleContractMetadata> getConvertedContractWithMetadata() {
		return convertedContractWithMetadata;
	}

}
