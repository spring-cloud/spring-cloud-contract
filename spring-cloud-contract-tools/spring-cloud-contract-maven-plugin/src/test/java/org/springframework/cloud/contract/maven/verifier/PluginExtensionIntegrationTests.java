/*
 * Copyright 2013-present the original author or authors.
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

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.junit.jupiter.api.Test;

class PluginExtensionIntegrationTests extends AbstractProjectIntegrationTests {

	@Test
	@InjectMojo(goal = "convert", pom = "pom.xml")
	@Basedir("src/test/projects/plugin-extension")
	void shouldRunPluginExtensionProject(ConvertMojo mojo) throws Exception {
		runConvertMojo(mojo);
	}

}
