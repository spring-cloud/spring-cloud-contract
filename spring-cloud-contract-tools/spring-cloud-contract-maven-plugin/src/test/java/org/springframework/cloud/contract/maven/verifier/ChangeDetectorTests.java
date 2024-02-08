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
import java.util.Arrays;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

class ChangeDetectorTests {

	@Test
	void should_mark_that_input_files_have_changed_when_goal_clean_was_called() throws MojoExecutionException {
		MavenSession session = BDDMockito.mock(MavenSession.class);
		given(session.getGoals()).willReturn(Arrays.asList("clean", "install"));

		then(ChangeDetector.inputFilesChangeDetected(new File("."), null, session)).isTrue();
	}

}
