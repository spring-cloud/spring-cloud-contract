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
		then(AetherFactories.BASIC_REPOSITORY_CONNECTOR_FACTORY_FQN).isEqualTo(BasicRepositoryConnectorFactory.class.getName());
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