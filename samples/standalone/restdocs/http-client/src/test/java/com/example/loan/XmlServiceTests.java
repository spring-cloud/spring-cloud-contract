package com.example.loan;

import java.net.URI;
import java.nio.charset.Charset;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.assertj.core.api.BDDAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(properties="service.port=${wiremock.server.port}")
@AutoConfigureWireMock(port=0)
public class XmlServiceTests {

	@Value("classpath:META-INF/com.example/http-server-restdocs/0.0.1-SNAPSHOT/mappings/should_return_empty_content.json")
	private Resource empty;

	@Value("classpath:META-INF/com.example/http-server-restdocs/0.0.1-SNAPSHOT/mappings/should_return_full_content.json")
	private Resource full;

	@Autowired
	private WireMockServer server;

	@Test
	public void shouldSuccessfullyReturnFullResponse() throws Exception {
		server.addStubMapping(StubMapping.buildFrom(StreamUtils.copyToString(
				full.getInputStream(), Charset.forName("UTF-8"))));

		ResponseEntity<XmlResponseBody> responseEntity = new RestTemplate().exchange(
				RequestEntity.post(URI.create("http://localhost:" + server.port() + "/xmlfraud"))
						.contentType(MediaType.valueOf("application/xml;charset=UTF-8"))
						.body(new XmlRequestBody("foo")), XmlResponseBody.class);

		BDDAssertions.then(responseEntity.getStatusCodeValue()).isEqualTo(200);
		BDDAssertions.then(responseEntity.getBody().status).isEqualTo("FULL");
	}

	@Test
	public void shouldSuccessfullyReturnEmptyResponse() throws Exception {
		server.addStubMapping(StubMapping.buildFrom(StreamUtils.copyToString(
				empty.getInputStream(), Charset.forName("UTF-8"))));

		ResponseEntity<XmlResponseBody> responseEntity = new RestTemplate().exchange(
				RequestEntity.post(URI.create("http://localhost:" + server.port() + "/xmlfraud"))
						.contentType(MediaType.valueOf("application/xml;charset=UTF-8"))
						.body(new XmlRequestBody("")), XmlResponseBody.class);

		BDDAssertions.then(responseEntity.getStatusCodeValue()).isEqualTo(200);
		BDDAssertions.then(responseEntity.getBody().status).isEqualTo("EMPTY");
	}

}

class XmlRequestBody {
	public String name;

	public XmlRequestBody(String name) {
		this.name = name;
	}
}

class XmlResponseBody {
	public String status;

	public XmlResponseBody(String status) {
		this.status = status;
	}

	public XmlResponseBody() {
	}
}