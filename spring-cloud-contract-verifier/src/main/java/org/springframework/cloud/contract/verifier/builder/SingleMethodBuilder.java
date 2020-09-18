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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

/**
 * Builds a single method body. Must be executed per contract.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class SingleMethodBuilder {

	private static final Log log = LogFactory.getLog(SingleMethodBuilder.class);

	private List<MethodAnnotations> methodAnnotations = new LinkedList<>();

	private List<MethodMetadata> methodMetadata = new LinkedList<>();

	private List<MethodPreProcessor> methodPreProcessors = new LinkedList<>();

	private List<MethodPostProcessor> methodPostProcessors = new LinkedList<>();

	private List<Given> givens = new LinkedList<>();

	private List<When> whens = new LinkedList<>();

	private List<Then> thens = new LinkedList<>();

	final GeneratedClassMetaData generatedClassMetaData;

	final BlockBuilder blockBuilder;

	private SingleMethodBuilder(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	static SingleMethodBuilder builder(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData) {
		return new SingleMethodBuilder(blockBuilder, generatedClassMetaData);
	}

	SingleMethodBuilder methodAnnotation(MethodAnnotations methodAnnotations) {
		this.methodAnnotations.add(methodAnnotations);
		return this;
	}

	MethodAnnotationBuilder methodAnnotation() {
		return new MethodAnnotationBuilder(this);
	}

	SingleMethodBuilder methodAnnotation(MethodAnnotations... methodAnnotations) {
		this.methodAnnotations.addAll(Arrays.asList(methodAnnotations));
		return this;
	}

	MethodMetadataBuilder methodMetadata() {
		return new MethodMetadataBuilder(this);
	}

	SingleMethodBuilder methodMetadata(MethodMetadata... methodMetadata) {
		this.methodMetadata.addAll(Arrays.asList(methodMetadata));
		return this;
	}

	SingleMethodBuilder restAssured() {
		return given(new JavaRestAssuredGiven(this.blockBuilder, this.generatedClassMetaData))
				.methodPreProcessor(new InProgressContractMethodPreProcessor())
				.given(new SpockRestAssuredGiven(this.blockBuilder, this.generatedClassMetaData))
				.when(new JavaRestAssuredWhen(this.blockBuilder, this.generatedClassMetaData))
				.when(new SpockRestAssuredWhen(this.blockBuilder, this.generatedClassMetaData))
				.then(new JavaRestAssuredThen(this.blockBuilder, this.generatedClassMetaData))
				.then(new SpockRestAssuredThen(this.blockBuilder, this.generatedClassMetaData))
				.methodPostProcessor(new TemplateUpdatingMethodPostProcessor(this.blockBuilder));
	}

	SingleMethodBuilder customMode() {
		return given(new CustomModeGiven(this.blockBuilder, this.generatedClassMetaData, CustomModeBodyParser.INSTANCE))
				.methodPreProcessor(new InProgressContractMethodPreProcessor())
				.when(new CustomModeWhen(this.blockBuilder, this.generatedClassMetaData))
				.then(new CustomModeThen(this.blockBuilder, this.generatedClassMetaData, CustomModeBodyParser.INSTANCE,
						ComparisonBuilder.JAVA_HTTP_INSTANCE))
				.methodPostProcessor(new TemplateUpdatingMethodPostProcessor(this.blockBuilder));
	}

	SingleMethodBuilder jaxRs() {
		return methodPreProcessor(new InProgressContractMethodPreProcessor())
				.given(new JaxRsGiven(this.generatedClassMetaData))
				.when(new JavaJaxRsWhen(this.blockBuilder, this.generatedClassMetaData))
				.when(new SpockJaxRsWhen(this.blockBuilder, this.generatedClassMetaData))
				.then(new JavaJaxRsThen(this.blockBuilder, this.generatedClassMetaData))
				.then(new SpockJaxRsThen(this.blockBuilder, this.generatedClassMetaData))
				.methodPostProcessor(new TemplateUpdatingMethodPostProcessor(this.blockBuilder));
	}

	SingleMethodBuilder messaging() {
		// @formatter:off
		return methodPreProcessor(new InProgressContractMethodPreProcessor())
				.given(new JavaMessagingGiven(this.blockBuilder, this.generatedClassMetaData))
				.given(new SpockMessagingGiven(this.blockBuilder, this.generatedClassMetaData))
				.when(new MessagingWhen(this.blockBuilder, this.generatedClassMetaData))
				.then(new JavaMessagingWithBodyThen(this.blockBuilder,
						this.generatedClassMetaData))
				.then(new SpockMessagingWithBodyThen(this.blockBuilder,
						this.generatedClassMetaData))
				.then(new SpockMessagingEmptyThen(this.blockBuilder,
						this.generatedClassMetaData))
				.methodPostProcessor(new TemplateUpdatingMethodPostProcessor(this.blockBuilder));
		// @formatter:on
	}

	SingleMethodBuilder given(Given... given) {
		this.givens.addAll(Arrays.asList(given));
		return this;
	}

	SingleMethodBuilder when(When... when) {
		this.whens.addAll(Arrays.asList(when));
		return this;
	}

	SingleMethodBuilder then(Then... then) {
		this.thens.addAll(Arrays.asList(then));
		return this;
	}

	SingleMethodBuilder methodPreProcessor(MethodPreProcessor methodPreProcessor) {
		this.methodPreProcessors.add(methodPreProcessor);
		return this;
	}

	SingleMethodBuilder methodPostProcessor(MethodPostProcessor methodPostProcessor) {
		this.methodPostProcessors.add(methodPostProcessor);
		return this;
	}

	/**
	 * Mutates the {@link BlockBuilder} to generate a methodBuilder
	 * @return block builder with contents of a single methodBuilder
	 */
	BlockBuilder build() {
		MethodMetadata methodMetadatum = pickMetadatum();
		// \n
		this.blockBuilder.addEmptyLine();
		this.generatedClassMetaData.toSingleContractMetadata().forEach(metaData -> {
			boolean stopProcessing = shouldStopProcessing(metaData);
			if (stopProcessing) {
				if (log.isDebugEnabled()) {
					log.debug("The method for meta data [" + metaData
							+ "] will not be processed further. At least one method pre-processor declared that this method should be skipped.");
				}
				return;
			}
			// @Test
			if (visit(this.methodAnnotations, metaData, false)) {
				this.blockBuilder.addEmptyLine();
			}
			// @formatter:off
			// public void validate_foo()
			this.blockBuilder.append(methodMetadatum::modifier)
					.appendWithSpace(methodMetadatum::returnType)
					.appendWithSpace(() -> methodMetadatum.name(metaData))
					.append("() throws Exception ");
			// (space) {
			this.blockBuilder.inBraces(() -> {
				// (indent) given
				if (visit(this.givens, metaData)) {
					this.blockBuilder.addEmptyLine();
				}
				// (indent) when
				visit(this.whens, metaData);
				this.blockBuilder.addEmptyLine();
				// (indent) then
				visit(this.thens, metaData);
			});
			this.blockBuilder.addEmptyLine();
			this.methodPostProcessors
				.stream()
				.filter(m -> m.accept(metaData))
				.forEach(m -> m.apply(metaData));
			// }
		});
		// @formatter:on
		return this.blockBuilder;
	}

	private boolean shouldStopProcessing(SingleContractMetadata metaData) {
		List<MethodPreProcessor> matchingPreProcessors = this.methodPreProcessors.stream()
				.filter(m -> m.accept(metaData)).collect(Collectors.toCollection(LinkedList::new));
		matchingPreProcessors.forEach(m -> m.apply(metaData));
		return matchingPreProcessors.stream().anyMatch(m -> !m.shouldContinue());
	}

	private MethodMetadata pickMetadatum() {
		return this.methodMetadata.stream().filter(Acceptor::accept).findFirst()
				.orElseThrow(() -> new IllegalStateException("No matching method metadata found"));
	}

	private boolean visit(List<? extends MethodVisitor> list, SingleContractMetadata metaData) {
		return visit(list, metaData, true);
	}

	private boolean visit(List<? extends MethodVisitor> list, SingleContractMetadata metaData, boolean addLineEnding) {
		List<? extends MethodVisitor> visitors = list.stream().filter(o -> o.accept(metaData))
				.collect(Collectors.toList());
		Iterator<? extends MethodVisitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			MethodVisitor visitor = iterator.next();
			visitor.apply(metaData);
			if (addLineEnding) {
				this.blockBuilder.addEndingIfNotPresent();
			}
			if (iterator.hasNext()) {
				this.blockBuilder.addEmptyLine();
			}
		}
		return !visitors.isEmpty();
	}

}
