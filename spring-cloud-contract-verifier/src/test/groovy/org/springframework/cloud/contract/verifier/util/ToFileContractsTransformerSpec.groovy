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

package org.springframework.cloud.contract.verifier.util

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import org.springframework.cloud.contract.verifier.converter.YamlContractConverter

/**
 * @author Marcin Grzejszczak
 * @since
 */
class ToFileContractsTransformerSpec extends Specification {

	@Rule
	TemporaryFolder tmp = new TemporaryFolder()
	File folder

	def setup() {
		folder = tmp.newFolder()
	}

	def "should store contracts as files"() {
		given:
			File input = new File("src/test/resources/dsl")
			String fqn = YamlContractConverter.name
		when:
			List<File> files = new ToFileContractsTransformer().storeContractsAsFiles(input.absolutePath, fqn, folder.absolutePath)
		then:
			files.size() == 1
			files.get(0).name.endsWith(".yml")
	}
}
