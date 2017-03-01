package org.springframework.cloud.contract.verifier.dsl;

import java.net.URI;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

/**
 * @author Marcin Grzejszczak
 */
class TestClient {
	ResponseEntity<String> call(int port) {
		return new TestRestTemplate().exchange(
				RequestEntity.post(URI.create("http://localhost:" + port + "/api/v1/xxxx?foo=bar&foo=bar2"))
						.header("Authorization", "secret")
						.header("Authorization", "secret2")
						.body("{\"foo\":\"bar\",\"baz\":5}"), String.class);
	}
}
