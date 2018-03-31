package org.springframework.cloud.contract.wiremock.restdocs;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.ScenarioMappingBuilder;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.MultipartValuePatternBuilder;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

class BasicMappingBuilder implements ScenarioMappingBuilder {

	private RequestPatternBuilder requestPatternBuilder;
	private ResponseDefinitionBuilder responseDefBuilder;
	private Integer priority;
	private String scenarioName;
	private String requiredScenarioState;
	private String newScenarioState;
	private UUID id = UUID.randomUUID();
	private String name;
	private boolean isPersistent = false;
	private Map<String, Parameters> postServeActions = new LinkedHashMap<>();

	BasicMappingBuilder(RequestMethod method, UrlPattern urlPattern) {
		this.requestPatternBuilder = new RequestPatternBuilder(method, urlPattern);
	}

	BasicMappingBuilder(ValueMatcher<Request> requestMatcher) {
		this.requestPatternBuilder = new RequestPatternBuilder(requestMatcher);
	}

	BasicMappingBuilder(String customRequestMatcherName, Parameters parameters) {
		this.requestPatternBuilder = new RequestPatternBuilder(customRequestMatcherName,
				parameters);
	}

	@Override
	public BasicMappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
		this.responseDefBuilder = responseDefBuilder;
		return this;
	}

	@Override public BasicMappingBuilder atPriority(Integer priority) {
		this.priority = priority;
		return this;
	}

	@Override
	public BasicMappingBuilder withHeader(String key, StringValuePattern headerPattern) {
		this.requestPatternBuilder.withHeader(key, headerPattern);
		return this;
	}

	@Override public BasicMappingBuilder withCookie(String name,
			StringValuePattern cookieValuePattern) {
		this.requestPatternBuilder.withCookie(name, cookieValuePattern);
		return this;
	}

	@Override public BasicMappingBuilder withQueryParam(String key,
			StringValuePattern queryParamPattern) {
		this.requestPatternBuilder.withQueryParam(key, queryParamPattern);
		return this;
	}

	@Override public ScenarioMappingBuilder withQueryParams(
			Map<String, StringValuePattern> queryParams) {
		for (Map.Entry<String, StringValuePattern> entry : queryParams.entrySet()) {
			this.requestPatternBuilder.withQueryParam(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override public ScenarioMappingBuilder withRequestBody(
			ContentPattern<?> bodyPattern) {
		this.requestPatternBuilder.withRequestBody(bodyPattern);
		return this;
	}

	@Override public ScenarioMappingBuilder withMultipartRequestBody(
			MultipartValuePatternBuilder multipartPatternBuilder) {
		this.requestPatternBuilder.withRequestBodyPart(multipartPatternBuilder.build());
		return this;
	}

	@Override public BasicMappingBuilder inScenario(String scenarioName) {
		this.scenarioName = scenarioName;
		return this;
	}

	@Override public BasicMappingBuilder whenScenarioStateIs(String stateName) {
		this.requiredScenarioState = stateName;
		return this;
	}

	@Override public BasicMappingBuilder willSetStateTo(String stateName) {
		this.newScenarioState = stateName;
		return this;
	}

	@Override public BasicMappingBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	@Override public BasicMappingBuilder withName(String name) {
		this.name = name;
		return this;
	}

	@Override public ScenarioMappingBuilder persistent() {
		this.isPersistent = true;
		return this;
	}

	@Override public BasicMappingBuilder withBasicAuth(String username, String password) {
		this.requestPatternBuilder
				.withBasicAuth(new BasicCredentials(username, password));
		return this;
	}

	@Override public <P> BasicMappingBuilder withPostServeAction(String extensionName,
			P parameters) {
		Parameters params = parameters instanceof Parameters ?
				(Parameters) parameters :
				Parameters.of(parameters);
		this.postServeActions.put(extensionName, params);
		return this;
	}

	@Override public StubMapping build() {
		if (this.scenarioName == null && (this.requiredScenarioState != null
				|| this.newScenarioState != null)) {
			throw new IllegalStateException(
					"Scenario name must be specified to require or set a new scenario state");
		}
		RequestPattern requestPattern = this.requestPatternBuilder.build();
		ResponseDefinition response = (this.responseDefBuilder != null ?
				this.responseDefBuilder :
				aResponse()).build();
		StubMapping mapping = new StubMapping(requestPattern, response);
		mapping.setPriority(this.priority);
		mapping.setScenarioName(this.scenarioName);
		mapping.setRequiredScenarioState(this.requiredScenarioState);
		mapping.setNewScenarioState(this.newScenarioState);
		mapping.setUuid(this.id);
		mapping.setName(this.name);
		mapping.setPersistent(this.isPersistent);
		mapping.setPostServeActions(
				this.postServeActions.isEmpty() ? null : this.postServeActions);
		return mapping;
	}

}
