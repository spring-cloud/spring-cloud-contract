package org.springframework.cloud.contract.wiremock.restdocs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.Parameters;

import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(MockitoJUnitRunner.class)
public class WireMockSnippetTests {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	Operation operation;
	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();
	private File outputFolder;

	@Before
	public void setup() throws IOException {
		this.outputFolder = this.tmp.newFolder();
	}

	@Test
	public void should_maintain_the_response_status_when_generating_stub()
			throws Exception {
		WireMockSnippet snippet = new WireMockSnippet();
		RestDocumentationContext context = new RestDocumentationContext(this.getClass(),
				"method", this.outputFolder);
		given(this.operation.getName()).willReturn("foo");
		given(this.operation.getAttributes().get(anyString())).willReturn(null);
		given(this.operation.getAttributes()
				.get(RestDocumentationContext.class.getName())).willReturn(context);
		given(this.operation.getRequest()).willReturn(request());
		given(this.operation.getResponse()).willReturn(response());

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/foo.json");
		assertThat(stub).exists();
		StubMapping stubMapping = StubMapping
				.buildFrom(new String(Files.readAllBytes(stub.toPath())));
		assertThat(stubMapping.getResponse().getStatus())
				.isEqualTo(HttpStatus.ACCEPTED.value());
	}

	@Test
	public void should_use_equal_to_json_pattern_for_body_when_request_content_type_is_json_when_generating_stub()
			throws Exception {
		WireMockSnippet snippet = new WireMockSnippet();
		RestDocumentationContext context = new RestDocumentationContext(this.getClass(),
				"method", this.outputFolder);
		given(this.operation.getName()).willReturn("foo");
		given(this.operation.getAttributes().get(anyString())).willReturn(null);
		given(this.operation.getAttributes()
				.get(RestDocumentationContext.class.getName())).willReturn(context);
		given(this.operation.getRequest()).willReturn(requestPostWithJsonContentType());
		given(this.operation.getResponse()).willReturn(response());

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/foo.json");
		assertThat(stub).exists();
		StubMapping stubMapping = StubMapping
				.buildFrom(new String(Files.readAllBytes(stub.toPath())));
		assertThat(stubMapping.getRequest().getBodyPatterns().get(0))
				.isInstanceOf(EqualToJsonPattern.class);
		assertThat(stubMapping.getRequest().getBodyPatterns().get(0).getValue())
				.isEqualTo("{\"name\": \"12\"}");
	}

	private OperationResponse response() {
		return new OperationResponse() {

			@Override
			public HttpStatus getStatus() {
				return HttpStatus.ACCEPTED;
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}

			@Override
			public byte[] getContent() {
				return new byte[0];
			}

			@Override
			public String getContentAsString() {
				return null;
			}
		};
	}

	private OperationRequest request() {
		return new OperationRequest() {

			@Override
			public byte[] getContent() {
				return new byte[0];
			}

			@Override
			public String getContentAsString() {
				return null;
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}

			@Override
			public HttpMethod getMethod() {
				return HttpMethod.GET;
			}

			@Override
			public Parameters getParameters() {
				return null;
			}

			@Override
			public Collection<OperationRequestPart> getParts() {
				return null;
			}

			@Override
			public URI getUri() {
				return URI.create("http://foo/bar");
			}
		};
	}

	private OperationRequest requestPostWithJsonContentType() {
		return new OperationRequest() {

			@Override
			public byte[] getContent() {
				String content = "{\"name\": \"12\"}";
				return content.getBytes(Charset.forName("UTF-8"));
			}

			@Override
			public String getContentAsString() {
				return "{\"name\": \"12\"}";
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
				return httpHeaders;
			}

			@Override
			public HttpMethod getMethod() {
				return HttpMethod.POST;
			}

			@Override
			public Parameters getParameters() {
				return null;
			}

			@Override
			public Collection<OperationRequestPart> getParts() {
				return null;
			}

			@Override
			public URI getUri() {
				return URI.create("http://foo/bar");
			}
		};
	}
}