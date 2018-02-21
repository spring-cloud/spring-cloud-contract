package org.springframework.cloud.contract.wiremock;

import io.restassured.specification.RequestSpecification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class WiremockServerRestAssuredApplicationTests {

	@Autowired
	private RequestSpecification documentationSpec;

	@LocalServerPort
	private int port;

	@Test
	public void contextLoads() throws Exception {
		given()
				.port(this.port)
		.when()
				.get("/resource")
		.then()
				.assertThat()
				.statusCode(is(200))
				.content(equalTo("Hello World"));
	}

	@Test
	public void statusIsMaintained() throws Exception {
		given(this.documentationSpec.port(this.port))
				.filter(document("status"))
		.when()
				.get("/status")
		.then()
				.assertThat()
				.statusCode(is(202))
				.content(equalTo("Hello World"));
	}

	@Configuration
	@EnableWebMvc
	@RestController
	@ImportAutoConfiguration(DispatcherServletAutoConfiguration.class)
	protected static class TestConfiguration {

		@ResponseBody
		@RequestMapping("/resource")
		public String resource() {
			return "Hello World";
		}

		@ResponseBody
		@RequestMapping("/status")
		public ResponseEntity<String> status() {
			return ResponseEntity.status(HttpStatus.ACCEPTED_202).body("Hello World");
		}

		@Bean
		TomcatServletWebServerFactory tomcatServletWebServerFactory() {
			return new TomcatServletWebServerFactory(0);
		}

	}

}
