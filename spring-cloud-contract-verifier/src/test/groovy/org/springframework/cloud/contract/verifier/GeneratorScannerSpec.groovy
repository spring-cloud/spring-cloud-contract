/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier

import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import spock.lang.Specification

class GeneratorScannerSpec extends Specification {

	private SingleTestGenerator classGenerator = Mock(SingleTestGenerator)

	def "should find all .json files and generate 6 classes for them"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.contractsDslDir = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("org.springframework.cloud.contract.verifier")
		then:
			6 * classGenerator.buildClass(_, _, _, _) >> "qwerty"
	}

	def "should create class with full package"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(targetFramework: TestFramework.SPOCK)
			properties.contractsDslDir = new File(this.getClass().getResource("/directory/with/stubs/package").toURI())
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("org.springframework.cloud.contract.verifier")
		then:
			1 * classGenerator.buildClass(_, 'exceptionsSpec', 'org.springframework.cloud.contract.verifier', _) >> "spec"
			1 * classGenerator.buildClass(_, 'exceptionsSpec', 'org.springframework.cloud.contract.verifier.v1', _) >> "spec1"
			1 * classGenerator.buildClass(_, 'exceptionsSpec', 'org.springframework.cloud.contract.verifier.v2', _) >> "spec2"
	}

}
