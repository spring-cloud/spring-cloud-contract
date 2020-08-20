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

class ClassAnnotationsBuilder {

	private final GeneratedTestClassBuilder parentBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	ClassAnnotationsBuilder(GeneratedTestClassBuilder generatedTestClassBuilder) {
		this.parentBuilder = generatedTestClassBuilder;
		this.builder = generatedTestClassBuilder.blockBuilder;
		this.metaData = generatedTestClassBuilder.generatedClassMetaData;
	}

	ClassAnnotationsBuilder defaultAnnotations() {
		this.parentBuilder.classAnnotations(new SuppressWarningsClassAnnotation(builder));
		return this;
	}

	ClassAnnotationsBuilder jUnit4() {
		this.parentBuilder
				.classAnnotations(new JUnit4OrderClassAnnotation(builder, metaData));
		return this;
	}

	ClassAnnotationsBuilder jUnit5() {
		this.parentBuilder
				.classAnnotations(new JUnit5OrderClassAnnotation(builder, metaData));
		return this;
	}

	ClassAnnotationsBuilder spock() {
		this.parentBuilder
				.classAnnotations(new SpockOrderClassAnnotation(builder, metaData));
		return this;
	}

	GeneratedTestClassBuilder build() {
		return this.parentBuilder;
	}

}
