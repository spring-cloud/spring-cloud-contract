package org.springframework.cloud.contract.wiremock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=WiremockTestsApplication.class, properties="app.baseUrl=http://localhost:${wiremock.server.port}", webEnvironment=WebEnvironment.NONE)
@DirtiesContext
// resource from a Initilizr stubs jar
@AutoConfigureWireMock(port=0, files="classpath*:META-INF/io.spring.initializr/initializr-web/0.4.0.BUILD-SNAPSHOT")
public class AutoConfigureWireMockFilesApplicationWithUrlResourceTests {

	@Autowired
	private Service service;

	@Test
	public void contextLoads() throws Exception {
		assertThat(this.service.pom()).contains("<artifactId>spring-boot-starter-parent</artifactId>");
	}

}
