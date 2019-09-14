/*
 * Copyright 2013-2019 the original author or authors.
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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

/**
 * Adds a label, proper indents and line endings for the body of a method.
 */
interface BodyMethodVisitor {

	/**
	 * Adds a starting body method block. E.g. //given: together with all indents
	 * @param blockBuilder
	 * @param label
	 * @return
	 */
	default BlockBuilder startBodyBlock(BlockBuilder blockBuilder, String label) {
		return blockBuilder.addIndentation().appendWithLabelPrefix(label).addEmptyLine()
				.startBlock();
	}

	/**
	 * Picks matching elements, visits them and applies indents.
	 * @param blockBuilder
	 * @param methodVisitors
	 * @param singleContractMetadata
	 */
	default void indentedBodyBlock(BlockBuilder blockBuilder,
			List<? extends MethodVisitor> methodVisitors,
			SingleContractMetadata singleContractMetadata) {
		List<MethodVisitor> visitors = filterVisitors(methodVisitors,
				singleContractMetadata);
		if (visitors.isEmpty()) {
			blockBuilder.addEndingIfNotPresent().addEmptyLine();
			blockBuilder.endBlock();
			return;
		}
		blockBuilder.addEmptyLine().indent();
		applyVisitors(blockBuilder, singleContractMetadata, visitors);
		endIndentedBodyBlock(blockBuilder);
	}

	/**
	 * Picks matching visitors.
	 * @param methodVisitors
	 * @param singleContractMetadata
	 * @return
	 */
	default List<MethodVisitor> filterVisitors(
			List<? extends MethodVisitor> methodVisitors,
			SingleContractMetadata singleContractMetadata) {
		return methodVisitors.stream()
				.filter(given -> given.accept(singleContractMetadata))
				.collect(Collectors.toList());
	}

	/**
	 * Picks matching elements, visits them. Doesn't apply indents. Useful for the //
	 * then: block where there is no method chaining.
	 * @param blockBuilder
	 * @param methodVisitors
	 * @param singleContractMetadata
	 */
	default void bodyBlock(BlockBuilder blockBuilder,
			List<? extends MethodVisitor> methodVisitors,
			SingleContractMetadata singleContractMetadata) {
		List<MethodVisitor> visitors = filterVisitors(methodVisitors,
				singleContractMetadata);
		if (visitors.isEmpty()) {
			blockBuilder.addEndingIfNotPresent().addEmptyLine();
			return;
		}
		applyVisitorsWithEnding(blockBuilder, singleContractMetadata, visitors);
		endBodyBlock(blockBuilder);
	}

	/**
	 * Executes logic for all the matching visitors.
	 * @param blockBuilder
	 * @param singleContractMetadata
	 * @param visitors
	 */
	default void applyVisitors(BlockBuilder blockBuilder,
			SingleContractMetadata singleContractMetadata, List<MethodVisitor> visitors) {
		Iterator<MethodVisitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			MethodVisitor visitor = iterator.next();
			visitor.apply(singleContractMetadata);
			if (iterator.hasNext()) {
				blockBuilder.addEmptyLine();
			}
		}
		blockBuilder.addEndingIfNotPresent();
	}

	/**
	 * Executes logic for all the matching visitors.
	 * @param blockBuilder
	 * @param singleContractMetadata
	 * @param visitors
	 */
	default void applyVisitorsWithEnding(BlockBuilder blockBuilder,
			SingleContractMetadata singleContractMetadata, List<MethodVisitor> visitors) {
		Iterator<MethodVisitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			MethodVisitor visitor = iterator.next();
			visitor.apply(singleContractMetadata);
			blockBuilder.addEndingIfNotPresent();
			if (iterator.hasNext()) {
				blockBuilder.addEmptyLine();
			}
		}
	}

	default void endIndentedBodyBlock(BlockBuilder blockBuilder) {
		blockBuilder.addEndingIfNotPresent().unindent().endBlock();
	}

	default void endBodyBlock(BlockBuilder blockBuilder) {
		blockBuilder.addEndingIfNotPresent().endBlock();
	}

}
