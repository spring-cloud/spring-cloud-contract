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

class MetaDataBuilder {

	private final GeneratedTestClassBuilder parentBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	MetaDataBuilder(GeneratedTestClassBuilder generatedTestClassBuilder) {
		this.parentBuilder = generatedTestClassBuilder;
		this.builder = generatedTestClassBuilder.blockBuilder;
		this.metaData = generatedTestClassBuilder.generatedClassMetaData;
	}

	MetaDataBuilder java() {
		this.parentBuilder.metaData(new JavaClassMetaData(this.builder, this.metaData));
		return this;
	}

	MetaDataBuilder groovy() {
		this.parentBuilder.metaData(new GroovyClassMetaData(this.builder, this.metaData));
		return this;
	}

	MetaDataBuilder kotlin() {
		this.parentBuilder.metaData(new KotlinClassMetaData(this.builder, this.metaData));
		return this;
	}

	GeneratedTestClassBuilder build() {
		return this.parentBuilder;
	}

}
