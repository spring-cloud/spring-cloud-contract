package org.springframework.cloud.contract.verifier.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Yaml representation of a {@link org.springframework.cloud.contract.spec.Contract}
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
//TODO: Perform full conversion
public class YamlContract {
	public Request request = new Request();
	public Response response = new Response();

	static class Request {
		public String method;
		public String url;
		public Map<String, Object> headers = new HashMap<>();
		public Map<String, Object> body = new HashMap<>();
	}

	static class Response {
		public int status;
		public Map<String, Object> headers = new HashMap<>();
		public Map<String, Object> body = new HashMap<>();
	}
}