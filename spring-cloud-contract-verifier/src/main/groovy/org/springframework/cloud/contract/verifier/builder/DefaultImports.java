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

import org.springframework.util.StringUtils;

class DefaultImports implements Imports, DefaultBaseClassProvider {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final BaseClassProvider baseClassProvider;

	DefaultImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.baseClassProvider = new BaseClassProvider();
	}

	@Override
	public Imports call() {
		String fqnBaseClass = fqnBaseClass();
		if (StringUtils.hasText(fqnBaseClass)) {
			this.blockBuilder.addLineWithEnding("import " + fqnBaseClass);
		}
		return this;
	}

	@Override
	public boolean accept() {
		return true;
	}

	@Override
	public GeneratedClassMetaData generatedClassMetaData() {
		return this.generatedClassMetaData;
	}

	@Override
	public BaseClassProvider baseClassProvider() {
		return this.baseClassProvider;
	}

}
