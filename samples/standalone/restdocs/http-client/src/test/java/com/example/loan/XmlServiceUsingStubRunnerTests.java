package com.example.loan;

import java.net.URI;
import java.nio.charset.Charset;

import com.example.loan.model.Client;
import com.example.loan.model.LoanApplication;
import com.example.loan.model.LoanApplicationResult;
import com.example.loan.model.LoanApplicationStatus;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.assertj.core.api.BDDAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureStubRunner(ids = "com.example:http-server-restdocs")
public class XmlServiceUsingStubRunnerTests {

	@Value("${stubrunner.runningstubs.http-server-restdocs.port}") int port;

	@Test
	public void shouldSuccessfullyReturnFullResponse() throws Exception {
		ResponseEntity<XmlResponseBody> responseEntity = new RestTemplate().exchange(
				RequestEntity.post(URI.create("http://localhost:" + this.port + "/xmlfraud"))
						.contentType(MediaType.valueOf("application/xml;charset=UTF-8"))
						.body(new XmlRequestBody("foo")), XmlResponseBody.class);

		BDDAssertions.then(responseEntity.getStatusCodeValue()).isEqualTo(200);
		BDDAssertions.then(responseEntity.getBody().status).isEqualTo("FULL");
	}

	@Test
	public void shouldSuccessfullyReturnEmptyResponse() throws Exception {
		ResponseEntity<XmlResponseBody> responseEntity = new RestTemplate().exchange(
				RequestEntity.post(URI.create("http://localhost:" + this.port + "/xmlfraud"))
						.contentType(MediaType.valueOf("application/xml;charset=UTF-8"))
						.body(new XmlRequestBody("")), XmlResponseBody.class);

		BDDAssertions.then(responseEntity.getStatusCodeValue()).isEqualTo(200);
		BDDAssertions.then(responseEntity.getBody().status).isEqualTo("EMPTY");
	}

}
