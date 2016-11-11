package org.springframework.cloud.contract.wiremock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=WiremockTestsApplication.class, properties="app.baseUrl=http://localhost:${wiremock.server.port}", webEnvironment=WebEnvironment.NONE)
@DirtiesContext
@AutoConfigureWireMock(port=0, files={"classpath:/nonexistent/", "classpath:/root/"}, stubs="file:src/test/resources/io.stubs/mappings")
public class AutoConfigureWireMockStubsAndMultipleFilesApplicationTests {

	@Autowired
	private Service service;

	@Test
	public void contextLoads() throws Exception {
		assertThat(this.service.go()).isEqualTo("{\"message\":\"Hello Root\"}");
	}

}
