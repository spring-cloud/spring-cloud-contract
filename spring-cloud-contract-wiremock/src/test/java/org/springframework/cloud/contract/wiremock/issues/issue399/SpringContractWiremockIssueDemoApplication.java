package org.springframework.cloud.contract.wiremock.issues.issue399;

import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpringContractWiremockIssueDemoApplication {

	private static final int SOME_NOT_LOWEST_PRECEDENCE = Ordered.LOWEST_PRECEDENCE - 1;

	@Bean
	@Order(SOME_NOT_LOWEST_PRECEDENCE)
	@Profile("bug")
	public RestTemplateCustomizer someOrderedInterceptorCustomizer() {
		return new RestTemplateCustomizer() {
			@Override public void customize(RestTemplate restTemplate) {
				ClientHttpRequestInterceptor emptyInterceptor = new ClientHttpRequestInterceptor() {
					@Override public ClientHttpResponse intercept(HttpRequest request,
							byte[] body, ClientHttpRequestExecution execution)
							throws IOException {
						return execution.execute(request, body);
					}
				};
				restTemplate.getInterceptors().add(emptyInterceptor);
			}
		};
	}

	@Bean
	public RestTemplateCustomizer someNotOrderedInterceptorCustomizer() {
		return new RestTemplateCustomizer() {
			@Override public void customize(RestTemplate restTemplate) {
				ClientHttpRequestInterceptor emptyInterceptor = new ClientHttpRequestInterceptor() {
					@Override public ClientHttpResponse intercept(HttpRequest request,
							byte[] body, ClientHttpRequestExecution execution)
							throws IOException {
						return execution.execute(request, body);
					}
				};
				restTemplate.getInterceptors().add(emptyInterceptor);
			}
		};
	}

	@Bean
	@Order
	public RestTemplateCustomizer someLowestPrecedenceOrderedInterceptorCustomizer() {
		return new RestTemplateCustomizer() {
			@Override public void customize(RestTemplate restTemplate) {
				ClientHttpRequestInterceptor emptyInterceptor = new ClientHttpRequestInterceptor() {
					@Override public ClientHttpResponse intercept(HttpRequest request,
							byte[] body, ClientHttpRequestExecution execution)
							throws IOException {
						return execution.execute(request, body);
					}
				};
				restTemplate.getInterceptors().add(emptyInterceptor);
			}
		};
	}
}

class RestTemplateClient {

	private final RestTemplate restTemplate;

	public RestTemplateClient(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public String get() {

		return restTemplate.getForObject("/some-url", String.class);
	}
}