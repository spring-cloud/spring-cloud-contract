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

package org.springframework.cloud.contract.stubrunner;

import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Jan-Niklas Pieninck
 */
public class AetherFactoriesTests {

	@Test
	public void should_match_fqn_of_basic_repository_connector_factory() {
		then(AetherFactories.BASIC_REPOSITORY_CONNECTOR_FACTORY_FQN)
			.isEqualTo(BasicRepositoryConnectorFactory.class.getName());
	}

	@Test
	public void should_match_fqn_of_file_transporter_factory() {
		then(AetherFactories.FILE_TRANSPORTER_FACTORY_FQN).isEqualTo(FileTransporterFactory.class.getName());
	}

	@Test
	public void should_match_fqn_of_http_transporter_factory() {
		then(AetherFactories.HTTP_TRANSPORTER_FACTORY_FQN).isEqualTo(HttpTransporterFactory.class.getName());
	}

}
