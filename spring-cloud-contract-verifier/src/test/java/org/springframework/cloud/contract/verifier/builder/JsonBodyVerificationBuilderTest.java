package org.springframework.cloud.contract.verifier.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor;

import static org.assertj.core.api.Assertions.assertThat;

class JsonBodyVerificationBuilderTest {

	@Test
	public void should_resolve_request_template_values_when_body_present() {
		// given
		Contract contract = contractWithRequest();
		JsonBodyVerificationBuilder builder = jsonBuilder(contract);
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("auth0", "{{{ request.headers.Authorization.0 }}}");
		responseBody.put("auth1", "{{{ request.headers.Authorization.[1] }}}");
		responseBody.put("param", "{{{ request.query.foo.1 }}}");
		responseBody.put("path", "{{{ request.path.1 }}}");

		// when
		Object converted = builder.addJsonResponseBodyCheck(new BlockBuilder(" "), responseBody, new BodyMatchers(),
				"\"{}\"", true);

		// then
		assertThat(converted).isInstanceOf(Map.class);
		Map<?, ?> convertedMap = (Map<?, ?>) converted;
		assertThat(convertedMap.get("auth0")).isEqualTo("alpha");
		assertThat(convertedMap.get("auth1")).isEqualTo("beta");
		assertThat(convertedMap.get("param")).isEqualTo("baz");
		assertThat(convertedMap.get("path")).isEqualTo("12");
	}

	@Test
	public void should_keep_template_entry_when_property_missing() {
		// given
		Contract contract = contractWithRequest();
		JsonBodyVerificationBuilder builder = jsonBuilder(contract);
		Map<String, Object> responseBody = new HashMap<>();
		String templateEntry = "{{{ request.headers.Missing.0 }}}";
		responseBody.put("missing", templateEntry);

		// when
		Object converted = builder.addJsonResponseBodyCheck(new BlockBuilder(" "), responseBody, new BodyMatchers(),
				"\"{}\"", true);

		// then
		Map<?, ?> convertedMap = (Map<?, ?>) converted;
		assertThat(convertedMap.get("missing")).isEqualTo(templateEntry);
	}

	private JsonBodyVerificationBuilder jsonBuilder(Contract contract) {
		HandlebarsTemplateProcessor templateProcessor = new HandlebarsTemplateProcessor();
		return new JsonBodyVerificationBuilder(false, templateProcessor, templateProcessor, contract, Optional.empty(),
				Function.identity());
	}

	private Contract contractWithRequest() {
		Contract contract = new Contract();
		contract.request(request -> {
			request.method("GET");
			request.url("/users/12", url -> url.queryParameters(query -> {
				query.parameter("foo", "bar");
				query.parameter("foo", "baz");
			}));
			request.headers(headers -> {
				headers.header("Authorization", "alpha");
				headers.header("Authorization", "beta");
			});
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("key", "value");
			request.body(requestBody);
		});
		return contract;
	}

}
