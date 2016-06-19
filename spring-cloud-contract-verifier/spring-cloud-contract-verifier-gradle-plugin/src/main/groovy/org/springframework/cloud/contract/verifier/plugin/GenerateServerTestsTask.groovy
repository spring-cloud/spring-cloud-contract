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

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.springframework.cloud.contract.verifier.ContractVerifierException
import org.springframework.cloud.contract.verifier.TestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * Task used to generate server side tests
 *
 * @since 1.0.0
 */
class GenerateServerTestsTask extends ConventionTask {

	@InputDirectory
	File contractsDslDir
	@OutputDirectory
	File generatedTestSourcesDir

	//TODO: How to deal with @Input*, @Output* and that domain object?
	ContractVerifierConfigProperties configProperties

	@TaskAction
	void generate() {
		project.logger.info("Spring Cloud Contract Verifier Plugin: Invoking test sources generation")

		project.sourceSets.test.groovy {
			project.logger.info("Registering ${getConfigProperties().generatedTestSourcesDir} as test source directory")
			srcDir getConfigProperties().generatedTestSourcesDir
		}

		try {
			//TODO: What with that? How to pass?
			TestGenerator generator = new TestGenerator(getConfigProperties())
			int generatedClasses = generator.generate()
			project.logger.info("Generated {} test classes", generatedClasses)
		} catch (ContractVerifierException e) {
			throw new GradleException("Spring Cloud Contract Verifier Plugin exception: ${e.message}", e)
		}
	}
}
