package org.springframework.cloud.contract.wiremock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={WiremockTestsApplication.class,
		AutoConfigureWireMockConfigurationCustomizerTests.Config.class},
		properties="app.baseUrl=http://localhost:${wiremock.server.port}",
		webEnvironment=WebEnvironment.NONE)
@DirtiesContext
@AutoConfigureWireMock(port=0, stubs="file:src/test/resources/io.stubs/mappings")
public class AutoConfigureWireMockConfigurationCustomizerTests {

	@Autowired
	private Service service;
	@Autowired
	private Config config;

	@Test
	public void contextLoads() throws Exception {
		assertThat(this.service.go()).isEqualTo("Hello World");
		assertThat(this.config.isExecuted()).isTrue();
	}

	@Configuration
	protected static class Config {

		boolean executed = false;

		// tag::customizer_1[]
		@Bean WireMockConfigurationCustomizer optionsCustomizer() {
			return new WireMockConfigurationCustomizer() {
				@Override public void customize(WireMockConfiguration options) {
					// end::customizer_1[]
					assertThat(options.portNumber()).isGreaterThan(0);
					Config.this.executed = true;
					// tag::customizer_2[]
				}
			};
		}
		// end::customizer_2[]

		public boolean isExecuted() {
			return executed;
		}
	}
}
