package org.springframework.cloud.contract.verifier.converter

/**
 * Yaml representation of a {@link org.springframework.cloud.contract.spec.Contract}
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
//TODO: Perform full conversion
class YamlContract {
	public Request request = new Request()
	public Response response = new Response()

	static class Request {
		public String method
		public String url
		public Map<String, Object> headers = [:]
		public Map<String, Object> body = [:]
	}

	static class Response {
		public int status
		public Map<String, Object> headers = [:]
		public Map<String, Object> body = [:]
	}
}