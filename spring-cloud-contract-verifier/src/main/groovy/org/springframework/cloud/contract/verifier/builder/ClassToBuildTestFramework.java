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

import java.util.Arrays;

import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class ClassToBuildTestFramework {

}

class JUnit4IgnoreImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	JUnit4IgnoreImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		this.blockBuilder.addLineWithEnding("import org.junit.Ignore");
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				&& this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(metadata -> metadata.isIgnored()
						|| metadata.getConvertedContractWithMetadata().stream()
								.anyMatch(SingleContractMetadata::isIgnored));
	}

}

class JUnit4IgnoreMethodAnnotation implements MethodAnnotations {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = { "@Ignore" };

	JUnit4IgnoreMethodAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<MethodAnnotations> apply(
			SingleContractMetadata singleContractMetadata) {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(meta -> meta.getOrder() != null);
	}

}

class JUnit4Imports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "org.junit.Test", "org.junit.Rule" };

	JUnit4Imports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT;
	}

}

class JUnit4MethodAnnotation implements MethodAnnotations {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = { "@Test" };

	JUnit4MethodAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT;
	}

	@Override
	public MethodVisitor<MethodAnnotations> apply(
			SingleContractMetadata singleContractMetadata) {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

}

class JUnit4OrderClassAnnotation implements ClassAnnotation {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = {
			"@FixMethodOrder(MethodSorters.NAME_ASCENDING)" };

	JUnit4OrderClassAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassAnnotation call() {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(meta -> meta.getOrder() != null);
	}

}

class JUnit4OrderImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "org.junit.FixMethodOrder",
			"org.junit.runners.MethodSorters" };

	JUnit4OrderImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(meta -> meta.getOrder() != null);
	}

}

class JUnit5IgnoreImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	JUnit5IgnoreImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		this.blockBuilder.addLineWithEnding("import org.junit.jupiter.api.Disabled");
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT5
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(metadata -> metadata.isIgnored()
								|| metadata.getConvertedContractWithMetadata().stream()
										.anyMatch(SingleContractMetadata::isIgnored));
	}

}

class JUnit5IgnoreMethodAnnotation implements MethodAnnotations {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = { "@Disabled" };

	JUnit5IgnoreMethodAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<MethodAnnotations> apply(
			SingleContractMetadata singleContractMetadata) {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT5
				&& this.generatedClassMetaData.isAnyIgnored();
	}

}

class JUnit5Imports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "org.junit.jupiter.api.Test", "org.junit.jupiter.api.extension.ExtendWith" };

	JUnit5Imports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT5;
	}

}

class JUnit5MethodAnnotation implements MethodAnnotations {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = { "@Test" };

	JUnit5MethodAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<MethodAnnotations> apply(
			SingleContractMetadata singleContractMetadata) {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT5;
	}

}

class JUnit5OrderClassAnnotation implements ClassAnnotation {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = {
			"@FixMethodOrder(MethodSorters.NAME_ASCENDING)" };

	JUnit5OrderClassAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassAnnotation call() {
		//Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		throw new UnsupportedOperationException("Not implemented yet in JUnit5 - https://github.com/junit-team/junit5/issues/48");
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT5
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(meta -> meta.getOrder() != null);
	}

}

class JUnit5OrderImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "" };

	JUnit5OrderImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
//		Arrays.stream(IMPORTS)
//				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		throw new UnsupportedOperationException("Not implemented yet in JUnit5 - https://github.com/junit-team/junit5/issues/48");
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT5
				&& this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(meta -> meta.getOrder() != null);
	}

}

class SpockImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "spock.lang.Specification" };

	SpockImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.SPOCK;
	}

}

class SpockIgnoreImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	SpockIgnoreImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		this.blockBuilder.addLineWithEnding("import spock.lang.Ignore");
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.SPOCK
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(metadata -> metadata.isIgnored()
								|| metadata.getConvertedContractWithMetadata().stream()
										.anyMatch(SingleContractMetadata::isIgnored));
	}

}

class SpockIgnoreMethodAnnotation implements MethodAnnotations {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = { "@Ignore" };

	SpockIgnoreMethodAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<MethodAnnotations> apply(
			SingleContractMetadata singleContractMetadata) {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.SPOCK
				&& this.generatedClassMetaData.isAnyIgnored();
	}

}

class SpockOrderClassAnnotation implements ClassAnnotation {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] ANNOTATIONS = {
			"@Stepwise" };

	SpockOrderClassAnnotation(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassAnnotation call() {
		Arrays.stream(ANNOTATIONS).forEach(this.blockBuilder::addIndented);
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.SPOCK
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(meta -> meta.getOrder() != null);
	}

}

class SpockOrderImports implements Imports {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] IMPORTS = { "spock.lang.Stepwise" };

	SpockOrderImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(IMPORTS)
				.forEach(s -> this.blockBuilder.addLineWithEnding("import " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.SPOCK
				&& this.generatedClassMetaData.listOfFiles.stream()
				.anyMatch(meta -> meta.getOrder() != null);
	}

}