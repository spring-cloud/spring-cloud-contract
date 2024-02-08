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

import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.util.StringUtils;

class JavaClassMetaData implements ClassMetaData, DefaultClassMetadata {

	private final BlockBuilder blockBuilder;

	private final BaseClassProvider baseClassProvider = new BaseClassProvider();

	private final GeneratedClassMetaData generatedClassMetaData;

	JavaClassMetaData(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassMetaData modifier() {
		this.blockBuilder.append("public");
		return this;
	}

	@Override
	public ClassMetaData suffix() {
		String suffix = StringUtils.hasText(this.generatedClassMetaData.configProperties.getNameSuffixForTests())
				? this.generatedClassMetaData.configProperties.getNameSuffixForTests() : "Test";
		if (!this.blockBuilder.endsWith(suffix)) {
			this.blockBuilder.addAtTheEnd(suffix);
		}
		return this;
	}

	@Override
	public ClassMetaData setupLineEnding() {
		this.blockBuilder.setupLineEnding(";");
		return this;
	}

	@Override
	public ClassMetaData setupLabelPrefix() {
		this.blockBuilder.setupLabelPrefix("// ");
		return this;
	}

	@Override
	public GeneratedClassMetaData generatedClassMetaData() {
		return this.generatedClassMetaData;
	}

	@Override
	public BaseClassProvider baseClassProvider() {
		return this.baseClassProvider;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public ClassMetaData parentClass() {
		String baseClass = fqnBaseClass();
		if (StringUtils.hasText(baseClass)) {
			int lastIndexOf = baseClass.lastIndexOf(".");
			if (lastIndexOf > 0) {
				baseClass = baseClass.substring(lastIndexOf + 1);
			}
			blockBuilder().append("extends ").append(baseClass).append(" ");
		}
		return this;
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties.getTestFramework() == TestFramework.JUNIT
				|| this.generatedClassMetaData.configProperties.getTestFramework() == TestFramework.JUNIT5
				|| this.generatedClassMetaData.configProperties.getTestFramework() == TestFramework.TESTNG;
	}

}
