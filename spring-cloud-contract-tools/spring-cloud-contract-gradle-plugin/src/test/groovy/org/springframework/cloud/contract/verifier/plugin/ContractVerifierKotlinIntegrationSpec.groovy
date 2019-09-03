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

package org.springframework.cloud.contract.verifier.plugin

import java.nio.file.Files
import java.nio.file.Path

import static java.nio.charset.StandardCharsets.UTF_8

abstract class ContractVerifierKotlinIntegrationSpec extends ContractVerifierIntegrationSpec {
	public static final String SPOCK = "testFramework.set(TestFramework.SPOCK)"
	public static final String JUNIT = "testFramework.set(TestFramework.JUNIT)"

	@Override
	protected File getBuildFile() {
		return new File(testProjectDir, 'build.gradle.kts')
	}

	@Override
	protected void switchToJunitTestFramework(String from, String to) {
		Path path = buildFile.toPath()
		String content = new StringBuilder(new String(Files.readAllBytes(path), UTF_8)).replaceAll(SPOCK, JUNIT)
																					   .replaceAll(from, to)
		Files.write(path, content.getBytes(UTF_8))
	}
}
