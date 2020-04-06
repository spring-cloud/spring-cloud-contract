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

package org.springframework.cloud.contract.maven.verifier;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.incremental.IncrementalBuildHelper;
import org.apache.maven.shared.utils.io.DirectoryScanner;

final class ChangeDetector {

	private ChangeDetector() {
	}

	static boolean inputFilesChangeDetected(File contractsDirectory,
			MojoExecution mojoExecution, MavenSession session)
			throws MojoExecutionException {

		IncrementalBuildHelper incrementalBuildHelper = new IncrementalBuildHelper(
				mojoExecution, session);

		DirectoryScanner scanner = incrementalBuildHelper.getDirectoryScanner();
		scanner.setBasedir(contractsDirectory);
		scanner.scan();
		boolean changeDetected = incrementalBuildHelper.inputFileTreeChanged(scanner);
		if (scanner.getIncludedFiles().length == 0) {
			// at least one input file must exist to consider changed/unchanged
			// return true to skip incremental build and make visible no input file at all
			return true;
		}
		return changeDetected;
	}

}
