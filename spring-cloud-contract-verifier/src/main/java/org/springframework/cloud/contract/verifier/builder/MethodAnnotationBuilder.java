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

class MethodAnnotationBuilder {

	private final SingleMethodBuilder singleMethodBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	MethodAnnotationBuilder(SingleMethodBuilder singleMethodBuilder) {
		this.singleMethodBuilder = singleMethodBuilder;
		this.builder = singleMethodBuilder.blockBuilder;
		this.metaData = singleMethodBuilder.generatedClassMetaData;
	}

	MethodAnnotationBuilder jUnit4() {
		this.singleMethodBuilder.methodAnnotation(new JUnit4MethodAnnotation(this.builder, this.metaData),
				new JUnit4IgnoreMethodAnnotation(this.builder, this.metaData));
		return this;
	}

	MethodAnnotationBuilder jUnit5() {
		this.singleMethodBuilder.methodAnnotation(new JUnit5MethodAnnotation(this.builder, this.metaData),
				new JUnit5IgnoreMethodAnnotation(this.builder, this.metaData));
		return this;
	}

	MethodAnnotationBuilder testNG() {
		this.singleMethodBuilder.methodAnnotation(new TestNGMethodAnnotation(this.builder, this.metaData));
		return this;
	}

	MethodAnnotationBuilder spock() {
		this.singleMethodBuilder.methodAnnotation(new SpockIgnoreMethodAnnotation(this.builder, this.metaData));
		return this;
	}

	SingleMethodBuilder build() {
		return this.singleMethodBuilder;
	}

}
