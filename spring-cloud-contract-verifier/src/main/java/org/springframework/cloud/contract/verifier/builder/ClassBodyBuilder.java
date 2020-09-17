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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the body of the class. Sets fields, methods.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class ClassBodyBuilder {

	private List<Field> fields = new LinkedList<>();

	private SingleMethodBuilder methodBuilder;

	final BlockBuilder blockBuilder;

	final GeneratedClassMetaData generatedClassMetaData;

	private ClassBodyBuilder(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	static ClassBodyBuilder builder(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData) {
		return new ClassBodyBuilder(blockBuilder, generatedClassMetaData);
	}

	FieldBuilder field() {
		return new FieldBuilder(this);
	}

	ClassBodyBuilder field(Field field) {
		this.fields.add(field);
		return this;
	}

	ClassBodyBuilder methodBuilder(SingleMethodBuilder methodBuilder) {
		this.methodBuilder = methodBuilder;
		return this;
	}

	/**
	 * Mutates the {@link BlockBuilder} to generate methods and fields.
	 * @return block builder with contents of built methods
	 */
	BlockBuilder build() {
		this.blockBuilder.inBraces(() -> {
			// @Rule ...
			visit(this.fields);
			// new line if fields added
			this.methodBuilder.build();
		});
		return this.blockBuilder;
	}

	void visit(List<? extends Visitor> list) {
		List<? extends Visitor> visitors = list.stream().filter(Acceptor::accept).collect(Collectors.toList());
		Iterator<? extends Visitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			Visitor visitor = iterator.next();
			visitor.call();
			this.blockBuilder.addEndingIfNotPresent();
			if (iterator.hasNext()) {
				this.blockBuilder.addEmptyLine();
			}
		}
	}

}
