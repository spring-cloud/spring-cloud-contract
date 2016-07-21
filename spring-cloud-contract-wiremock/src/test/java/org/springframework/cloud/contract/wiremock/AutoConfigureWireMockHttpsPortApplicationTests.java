package org.springframework.cloud.contract.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WiremockTestsApplication.class, properties = "app.baseUrl=https://localhost:${wiremock.server.https-port}", webEnvironment = WebEnvironment.NONE)
@DirtiesContext
@AutoConfigureWireMock(httpsPort = 9999)
public class AutoConfigureWireMockHttpsPortApplicationTests {

	@Autowired
	private Service service;

	@Test
	public void contextLoads() throws Exception {
		stubFor(get(urlEqualTo("/resource")).willReturn(aResponse()
				.withHeader("Content-Type", "text/plain").withBody("Hello World!")));
		assertThat(this.service.go()).isEqualTo("Hello World!");
	}

}
