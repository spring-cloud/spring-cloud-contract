package org.springframework.cloud.contract.wiremock.issues.issue399;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWireMock(port = 0)
public class NotOrderedCustomizerTest {

	@Value("${wiremock.server.port}") Integer port;
	@Autowired RestTemplateBuilder restTemplateBuilder;

	@Test 
	public void should_not_fail_when_ordered_customizer_added_interceptor_to_rest_template() {
		stubFor(get(urlEqualTo("/some-url"))
				.willReturn(aResponse().withStatus(200).withBody("Yeah!")));
		RestTemplateClient client = new RestTemplateClient(
				restTemplateBuilder.rootUri("http://localhost:" + port));

		String body = client.get();

		BDDAssertions.then(body).isEqualTo("Yeah!");
	}
}