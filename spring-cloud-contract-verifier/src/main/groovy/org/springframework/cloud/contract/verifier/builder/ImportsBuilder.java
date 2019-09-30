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

class ImportsBuilder {

	private final GeneratedTestClassBuilder parentBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	ImportsBuilder(GeneratedTestClassBuilder generatedTestClassBuilder) {
		this.parentBuilder = generatedTestClassBuilder;
		this.builder = generatedTestClassBuilder.blockBuilder;
		this.metaData = generatedTestClassBuilder.generatedClassMetaData;
	}

	ImportsBuilder defaultImports() {
		this.parentBuilder.imports(new DefaultImports(builder, metaData));
		this.parentBuilder.staticImports(
				new DefaultGroovyStaticImports(builder, metaData),
				new DefaultJavaStaticImports(builder, metaData),
				new DefaultKotlinStaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder custom() {
		this.parentBuilder.imports(new CustomImports(builder, metaData));
		this.parentBuilder.staticImports(new CustomGroovyStaticImports(builder, metaData),
				new CustomJavaStaticImports(builder, metaData),
				new CustomKotlinStaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder json() {
		this.parentBuilder.imports(new JsonPathImports(builder, metaData));
		this.parentBuilder.staticImports(
				new DefaultJsonGroovyStaticImports(builder, metaData),
				new DefaultJsonJavaStaticImports(builder, metaData),
				new DefaultJsonKotlinStaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder xml() {
		this.parentBuilder.imports(new XmlImports(builder, metaData));
		return this;
	}

	ImportsBuilder jUnit4() {
		this.parentBuilder.imports(new JUnit4Imports(builder, metaData),
				new JUnit4IgnoreImports(builder, metaData),
				new JUnit4OrderImports(builder, metaData));
		return this;
	}

	ImportsBuilder jUnit5() {
		this.parentBuilder.imports(new JUnit5Imports(builder, metaData),
				new JUnit5IgnoreImports(builder, metaData),
				new JUnit5OrderImports(builder, metaData));
		return this;
	}

	ImportsBuilder testNG() {
		this.parentBuilder.imports(new TestNGImports(builder, metaData));
		return this;
	}

	ImportsBuilder spock() {
		this.parentBuilder.imports(new SpockImports(builder, metaData),
				new SpockIgnoreImports(builder, metaData),
				new SpockOrderImports(builder, metaData));
		return this;
	}

	ImportsBuilder messaging() {
		this.parentBuilder.imports(new MessagingImports(builder, metaData));
		this.parentBuilder.staticImports(
				new MessagingGroovyStaticImports(builder, metaData),
				new MessagingJavaStaticImports(builder, metaData),
				new MessagingKotlinStaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder restAssured() {
		this.parentBuilder.imports(new MockMvcRestAssuredImports(builder, metaData),
				new ExplicitRestAssuredImports(builder, metaData),
				new WebTestClientRestAssuredImports(builder, metaData));
		this.parentBuilder.staticImports(
				new MockMvcRestAssuredGroovyStaticImports(builder, metaData),
				new MockMvcRestAssuredJavaStaticImports(builder, metaData),
				new MockMvcRestAssuredKotlinStaticImports(builder, metaData),
				new ExplicitRestAssuredGroovyStaticImports(builder, metaData),
				new ExplicitRestAssuredJavaStaticImports(builder, metaData),
				new ExplicitRestAssuredKotlinStaticImports(builder, metaData),
				new WebTestClientRestAssured3GroovyStaticImports(builder, metaData),
				new WebTestClientRestAssured3JavaStaticImports(builder, metaData),
				new WebTestClientRestAssured3KotlinStaticImports(builder, metaData));
		return this;
	}

	ImportsBuilder jaxRs() {
		this.parentBuilder.imports(new JaxRsImports(builder, metaData));
		this.parentBuilder.staticImports(new JaxRsGroovyStaticImports(builder, metaData),
				new JaxRsJavaStaticImports(builder, metaData),
				new JaxRsKotlinStaticImports(builder, metaData));
		return this;
	}

	GeneratedTestClassBuilder build() {
		return this.parentBuilder;
	}

}
