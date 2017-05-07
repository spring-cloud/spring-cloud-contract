package org.springframework.cloud.contract.wiremock.restdocs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.snippet.TemplatedSnippet;
import org.springframework.restdocs.templates.TemplateEngine;
import org.springframework.util.StringUtils;

/**
 * A {@link org.springframework.restdocs.snippet.Snippet} that documents the Spring Cloud Contract Groovy DSL.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.4
 */
public class ContractDslSnippet extends TemplatedSnippet {

	private static final String CONTRACTS_FOLDER = "contracts";
	private static final String SNIPPET_NAME = "dsl-contract";

	private Map<String, Object> model = new HashMap<>();

	/**
	 * Creates a new {@code ContractDslSnippet} with no additional attributes.
	 */
	protected ContractDslSnippet() {
		this(null);
	}

	/**
	 * Creates a new {@code ContractDslSnippet} with the given additional
	 * {@code attributes} that will be included in the model during template rendering.
	 *
	 * @param attributes The additional attributes
	 */
	protected ContractDslSnippet(Map<String, Object> attributes) {
		super(SNIPPET_NAME, attributes);
	}

	@Override
	protected Map<String, Object> createModel(Operation operation) {
		return this.model;
	}

	@Override public void document(Operation operation) throws IOException {
		TemplateEngine templateEngine = (TemplateEngine) operation.getAttributes().get(TemplateEngine.class.getName());
		String renderedContract = templateEngine.compileTemplate("default-dsl-contract-only")
				.render(createModelForContract(operation));
		this.model.put("contract", renderedContract);
		storeDslContract(operation, renderedContract);
		super.document(operation);
	}

	private void insertResponseModel(Operation operation, Map<String, Object> model) {
		OperationResponse response = operation.getResponse();
		model.put("response_status", response.getStatus().value());
		model.put("response_body_present", response.getContent().length > 0);
		model.put("response_body", response.getContentAsString());
		HttpHeaders headers = response.getHeaders();
		model.put("response_headers_present", !headers.isEmpty());
		model.put("response_headers", headers.toSingleValueMap().entrySet());
		@SuppressWarnings("unchecked") Set<String> jsonPaths = (Set<String>) operation.getAttributes()
				.get("contract.jsonPaths");
		model.put("response_json_paths_present", jsonPaths != null && !jsonPaths.isEmpty());
		model.put("response_json_paths", jsonPaths(jsonPaths));
	}

	private Set<JsonPaths> jsonPaths(Set<String> jsonPaths) {
		Set<JsonPaths> paths = new HashSet<>();
		if (jsonPaths == null) {
			return paths;
		}
		for (String s : jsonPaths) {
			paths.add(new JsonPaths(s));
		}
		return paths;
	}

	private void insertRequestModel(Operation operation, Map<String, Object> model) {
		OperationRequest request = operation.getRequest();
		model.put("request_method", request.getMethod());
		model.put("request_url", prepareRequestUrl(request.getUri()));
		model.put("request_body_present", request.getContent().length > 0);
		model.put("request_body", request.getContentAsString());
		Map<String, String> headers = request.getHeaders().toSingleValueMap();
		filterHeaders(headers);
		model.put("request_headers_present", !headers.isEmpty());
		model.put("request_headers", headers.entrySet());
	}

	private void filterHeaders(Map<String, String> headers) {
		if (headers.containsKey(HttpHeaders.HOST)) {
			headers.remove(HttpHeaders.HOST);
		}
	}

	private String prepareRequestUrl(URI uri) {
		String path = uri.getRawPath();
		String query = uri.getRawQuery();

		if (StringUtils.hasText(query)) {
			path = path + "?" + query;
		}

		return path;
	}

	private Map<String, Object> createModelForContract(Operation operation) {
		Map<String, Object> modelForContract = new HashMap<>();
		insertRequestModel(operation, modelForContract);
		insertResponseModel(operation, modelForContract);
		return modelForContract;
	}

	private void storeDslContract(Operation operation, String content)
			throws IOException {
		RestDocumentationContext context = (RestDocumentationContext) operation
				.getAttributes().get(RestDocumentationContext.class.getName());
		File output = new File(context.getOutputDirectory(),
				CONTRACTS_FOLDER + "/" + operation.getName() + ".groovy");
		output.getParentFile().mkdirs();
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(output))) {
			writer.append(content);
		}
	}
}

class JsonPaths {
	private final String jsonPath;

	JsonPaths(String jsonPath) {
		this.jsonPath = jsonPath;
	}

	public String getJsonPath() {
		return this.jsonPath;
	}
}