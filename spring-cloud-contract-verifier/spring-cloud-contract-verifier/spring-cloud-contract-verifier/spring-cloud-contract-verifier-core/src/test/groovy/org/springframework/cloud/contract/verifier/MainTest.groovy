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

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

class MainTest {
	public static void main(String[] args) {
		ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
				contractsDslDir: new File('/some/path/dsl'),
				generatedTestSourcesDir: new File('/tmp/contracts'),
				targetFramework: TestFramework.SPOCK, testMode: TestMode.MOCKMVC, basePackageForTests: 'io.test',
				staticImports: ['com.package.Test.*'], imports: ['org.package.Test'], excludedFiles: ["**/other"])
		println new TestGenerator(properties).generate()
	}
}
