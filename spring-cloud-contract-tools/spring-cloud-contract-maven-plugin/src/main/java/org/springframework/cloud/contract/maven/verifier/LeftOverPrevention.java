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

package org.springframework.cloud.contract.maven.verifier;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.incremental.IncrementalBuildHelper;
import org.apache.maven.shared.incremental.IncrementalBuildHelperRequest;

/**
 * Prevents the following scenario:
 *
 * 1. Contract is added
 *
 * 2. Derived file is generated
 *
 * 3. Contract is deleted
 *
 * 4. Derived file still exists
 */
class LeftOverPrevention {

	private final IncrementalBuildHelper incrementalBuildHelper;

	private final File generatedDirectory;

	LeftOverPrevention(File generatedDirectory, MojoExecution mojoExecution, MavenSession session)
			throws MojoExecutionException {
		this.generatedDirectory = generatedDirectory;
		this.incrementalBuildHelper = new IncrementalBuildHelper(mojoExecution, session);
		this.incrementalBuildHelper
				.beforeRebuildExecution(new IncrementalBuildHelperRequest().outputDirectory(generatedDirectory));
	}

	void deleteLeftOvers() throws MojoExecutionException {
		if (generatedDirectory.exists()) {
			incrementalBuildHelper
					.afterRebuildExecution(new IncrementalBuildHelperRequest().outputDirectory(generatedDirectory));
		}
	}

}
