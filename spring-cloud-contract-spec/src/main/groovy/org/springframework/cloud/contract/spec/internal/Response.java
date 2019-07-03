package org.springframework.cloud.contract.spec.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import groovy.lang.Delegate;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import org.springframework.cloud.contract.spec.util.RegexpUtils;

/**
 * Represents the response side of the HTTP communication
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.0.0
 */
public class Response extends Common implements RegexCreatingProperty<ServerDslProperty> {

	private DslProperty status;

	private DslProperty delay;

	private Headers headers;

	private Cookies cookies;

	private Body body;

	private boolean async;

	private ResponseBodyMatchers bodyMatchers;

	public Response() {
	}

	public Response(Response response) {
		this.status = response.getStatus();
		this.headers = response.getHeaders();
		this.cookies = response.getCookies();
		this.body = response.getBody();
	}

	/**
	 * Allows to set the HTTP status
	 */
	public void status(int status) {
		this.status = toDslProperty(status);
	}

	/**
	 * Allows to set the HTTP status
	 */
	public void status(DslProperty status) {
		this.status = toDslProperty(status);
	}

	/**
	 * Allows set an HTTP body
	 */
	public void body(Map<String, Object> body) {
		this.body = new Body(convertObjectsToDslProperties(body));
	}

	/**
	 * Allows set an HTTP body
	 */
	public void body(List body) {
		this.body = new Body(convertObjectsToDslProperties(body));
	}

	/**
	 * Allows set an HTTP body
	 */
	public void body(Object bodyAsValue) {
		if (bodyAsValue instanceof List) {
			body(DefaultGroovyMethods.asType((Collection) bodyAsValue, List.class));
		}
		else {
			this.body = new Body(bodyAsValue);
		}

	}

	/**
	 * Allows to set a fixed delay of the response in milliseconds
	 */
	public void fixedDelayMilliseconds(int timeInMilliseconds) {
		this.delay = toDslProperty(timeInMilliseconds);
	}

	/**
	 * Turns on the asynchronous mode for this contract. Used with MockMvc and the Servlet
	 * 3.0 features
	 */
	public void async() {
		this.async = true;
	}

	public void assertThatSidesMatch(OptionalProperty stubSide, Object testSide) {
		throw new IllegalStateException(
				"Optional can be used only in the test side of the response!");
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	public DslProperty value(ServerDslProperty server) {
		Object dynamicValue = server.getServerValue();
		Object concreteValue = server.getClientValue();
		if (dynamicValue instanceof RegexProperty && server.isSingleValue()) {
			return ((RegexProperty) dynamicValue).concreteClientDynamicProducer();
		}

		return new DslProperty(concreteValue, dynamicValue);
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	public DslProperty $(ServerDslProperty server) {
		return value(server);
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	public DslProperty value(Pattern server) {
		return value(new RegexProperty(server));
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	public DslProperty value(RegexProperty server) {
		return value(new ServerDslProperty(server));
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	public DslProperty $(RegexProperty server) {
		return value(server);
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	public DslProperty $(Pattern server) {
		return value(new RegexProperty(server));
	}

	@Override
	public RegexProperty regexProperty(Object object) {
		return new RegexProperty(object).concreteClientDynamicProducer();
	}

	/**
	 * Allows to reference entries from the request
	 */
	public FromRequest fromRequest() {
		return new FromRequest();
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	@Override
	public DslProperty value(ClientDslProperty client, ServerDslProperty server) {
		if (client.getClientValue() instanceof RegexProperty) {
			throw new IllegalStateException(
					"You can't have a regular expression for the response on the client side");
		}

		return super.value(client, server);
	}

	/**
	 * Allows to set a dynamic value for the given element
	 */
	@Override
	public DslProperty value(ServerDslProperty server, ClientDslProperty client) {
		if (client.getClientValue() instanceof RegexProperty) {
			throw new IllegalStateException(
					"You can't have a regular expression for the response on the client side");
		}

		return super.value(server, client);
	}

	public ServerPatternValueDslProperty getProperty() {
		return property;
	}

	public void setProperty(ServerPatternValueDslProperty property) {
		this.property = property;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public DslProperty getStatus() {
		return status;
	}

	public void setStatus(DslProperty status) {
		this.status = status;
	}

	public DslProperty getDelay() {
		return delay;
	}

	public void setDelay(DslProperty delay) {
		this.delay = delay;
	}

	public Headers getHeaders() {
		return headers;
	}

	public void setHeaders(Headers headers) {
		this.headers = headers;
	}

	public Cookies getCookies() {
		return cookies;
	}

	public void setCookies(Cookies cookies) {
		this.cookies = cookies;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public boolean getAsync() {
		return async;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public ResponseBodyMatchers getBodyMatchers() {
		return bodyMatchers;
	}

	public void setBodyMatchers(ResponseBodyMatchers bodyMatchers) {
		this.bodyMatchers = bodyMatchers;
	}

	@Delegate
	private ServerPatternValueDslProperty property = new ServerPatternValueDslProperty();

	@Delegate
	private HttpStatus httpStatus = new HttpStatus();

	private class ServerResponse extends Response {

		public ServerResponse(Response enclosing, Response request) {
			super(request);
		}

	}

	private class ClientResponse extends Response {

		public ClientResponse(Response enclosing, Response request) {
			super(request);
		}

	}

	private class ResponseHeaders extends Headers {

		@Override
		public DslProperty matching(final String value) {
			return $(p(notEscaped(Pattern.compile(
					RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*"))),
					c(value));
		}

	}

	private class ResponseCookies extends Cookies {

		@Override
		public DslProperty matching(final String value) {
			return $(p(
					regex(RegexpUtils.escapeSpecialRegexWithSingleEscape(value) + ".*")),
					c(value));
		}

	}

	private class ServerPatternValueDslProperty
			extends PatternValueDslProperty<ServerDslProperty> {

		@Override
		protected ServerDslProperty createProperty(Pattern pattern,
				Object generatedValue) {
			return new ServerDslProperty(pattern, generatedValue);
		}

	}

	@Override
	public ServerDslProperty anyAlphaUnicode() {
		return property.anyAlphaUnicode();
	}

	@Override
	public ServerDslProperty anyAlphaNumeric() {
		return property.anyAlphaNumeric();
	}

	@Override
	public ServerDslProperty anyNumber() {
		return property.anyNumber();
	}

	@Override
	public ServerDslProperty anyInteger() {
		return property.anyInteger();
	}

	@Override
	public ServerDslProperty anyPositiveInt() {
		return property.anyPositiveInt();
	}

	@Override
	public ServerDslProperty anyDouble() {
		return property.anyDouble();
	}

	@Override
	public ServerDslProperty anyHex() {
		return property.anyHex();
	}

	@Override
	public ServerDslProperty aBoolean() {
		return property.aBoolean();
	}

	@Override
	public ServerDslProperty anyIpAddress() {
		return property.anyIpAddress();
	}

	@Override
	public ServerDslProperty anyHostname() {
		return property.anyHostname();
	}

	@Override
	public ServerDslProperty anyEmail() {
		return property.anyEmail();
	}

	@Override
	public ServerDslProperty anyUrl() {
		return property.anyUrl();
	}

	@Override
	public ServerDslProperty anyHttpsUrl() {
		return property.anyHttpsUrl();
	}

	@Override
	public ServerDslProperty anyUuid() {
		return property.anyUuid();
	}

	@Override
	public ServerDslProperty anyDate() {
		return property.anyDate();
	}

	@Override
	public ServerDslProperty anyDateTime() {
		return property.anyDateTime();
	}

	@Override
	public ServerDslProperty anyTime() {
		return property.anyTime();
	}

	@Override
	public ServerDslProperty anyIso8601WithOffset() {
		return property.anyIso8601WithOffset();
	}

	@Override
	public ServerDslProperty anyNonBlankString() {
		return property.anyNonBlankString();
	}

	@Override
	public ServerDslProperty anyNonEmptyString() {
		return property.anyNonEmptyString();
	}

	@Override
	public ServerDslProperty anyOf(String... values) {
		return property.anyOf(values);
	}

	public int CONTINUE() {
		return httpStatus.CONTINUE();
	}

	public int SWITCHING_PROTOCOLS() {
		return httpStatus.SWITCHING_PROTOCOLS();
	}

	public int PROCESSING() {
		return httpStatus.PROCESSING();
	}

	public int CHECKPOINT() {
		return httpStatus.CHECKPOINT();
	}

	public int OK() {
		return httpStatus.OK();
	}

	public int CREATED() {
		return httpStatus.CREATED();
	}

	public int ACCEPTED() {
		return httpStatus.ACCEPTED();
	}

	public int NON_AUTHORITATIVE_INFORMATION() {
		return httpStatus.NON_AUTHORITATIVE_INFORMATION();
	}

	public int NO_CONTENT() {
		return httpStatus.NO_CONTENT();
	}

	public int RESET_CONTENT() {
		return httpStatus.RESET_CONTENT();
	}

	public int PARTIAL_CONTENT() {
		return httpStatus.PARTIAL_CONTENT();
	}

	public int MULTI_STATUS() {
		return httpStatus.MULTI_STATUS();
	}

	public int ALREADY_REPORTED() {
		return httpStatus.ALREADY_REPORTED();
	}

	public int IM_USED() {
		return httpStatus.IM_USED();
	}

	public int MULTIPLE_CHOICES() {
		return httpStatus.MULTIPLE_CHOICES();
	}

	public int MOVED_PERMANENTLY() {
		return httpStatus.MOVED_PERMANENTLY();
	}

	public int FOUND() {
		return httpStatus.FOUND();
	}

	@Deprecated
	public int MOVED_TEMPORARILY() {
		return httpStatus.MOVED_TEMPORARILY();
	}

	public int SEE_OTHER() {
		return httpStatus.SEE_OTHER();
	}

	public int NOT_MODIFIED() {
		return httpStatus.NOT_MODIFIED();
	}

	@Deprecated
	public int USE_PROXY() {
		return httpStatus.USE_PROXY();
	}

	public int TEMPORARY_REDIRECT() {
		return httpStatus.TEMPORARY_REDIRECT();
	}

	public int PERMANENT_REDIRECT() {
		return httpStatus.PERMANENT_REDIRECT();
	}

	public int BAD_REQUEST() {
		return httpStatus.BAD_REQUEST();
	}

	public int UNAUTHORIZED() {
		return httpStatus.UNAUTHORIZED();
	}

	public int PAYMENT_REQUIRED() {
		return httpStatus.PAYMENT_REQUIRED();
	}

	public int FORBIDDEN() {
		return httpStatus.FORBIDDEN();
	}

	public int NOT_FOUND() {
		return httpStatus.NOT_FOUND();
	}

	public int METHOD_NOT_ALLOWED() {
		return httpStatus.METHOD_NOT_ALLOWED();
	}

	public int NOT_ACCEPTABLE() {
		return httpStatus.NOT_ACCEPTABLE();
	}

	public int PROXY_AUTHENTICATION_REQUIRED() {
		return httpStatus.PROXY_AUTHENTICATION_REQUIRED();
	}

	public int REQUEST_TIMEOUT() {
		return httpStatus.REQUEST_TIMEOUT();
	}

	public int CONFLICT() {
		return httpStatus.CONFLICT();
	}

	public int GONE() {
		return httpStatus.GONE();
	}

	public int LENGTH_REQUIRED() {
		return httpStatus.LENGTH_REQUIRED();
	}

	public int PRECONDITION_FAILED() {
		return httpStatus.PRECONDITION_FAILED();
	}

	public int PAYLOAD_TOO_LARGE() {
		return httpStatus.PAYLOAD_TOO_LARGE();
	}

	@Deprecated
	public int REQUEST_ENTITY_TOO_LARGE() {
		return httpStatus.REQUEST_ENTITY_TOO_LARGE();
	}

	public int URI_TOO_LONG() {
		return httpStatus.URI_TOO_LONG();
	}

	@Deprecated
	public int REQUEST_URI_TOO_LONG() {
		return httpStatus.REQUEST_URI_TOO_LONG();
	}

	public int UNSUPPORTED_MEDIA_TYPE() {
		return httpStatus.UNSUPPORTED_MEDIA_TYPE();
	}

	public int REQUESTED_RANGE_NOT_SATISFIABLE() {
		return httpStatus.REQUESTED_RANGE_NOT_SATISFIABLE();
	}

	public int EXPECTATION_FAILED() {
		return httpStatus.EXPECTATION_FAILED();
	}

	public int I_AM_A_TEAPOT() {
		return httpStatus.I_AM_A_TEAPOT();
	}

	@Deprecated
	public int INSUFFICIENT_SPACE_ON_RESOURCE() {
		return httpStatus.INSUFFICIENT_SPACE_ON_RESOURCE();
	}

	@Deprecated
	public int METHOD_FAILURE() {
		return httpStatus.METHOD_FAILURE();
	}

	@Deprecated
	public int DESTINATION_LOCKED() {
		return httpStatus.DESTINATION_LOCKED();
	}

	public int UNPROCESSABLE_ENTITY() {
		return httpStatus.UNPROCESSABLE_ENTITY();
	}

	public int LOCKED() {
		return httpStatus.LOCKED();
	}

	public int FAILED_DEPENDENCY() {
		return httpStatus.FAILED_DEPENDENCY();
	}

	public int UPGRADE_REQUIRED() {
		return httpStatus.UPGRADE_REQUIRED();
	}

	public int PRECONDITION_REQUIRED() {
		return httpStatus.PRECONDITION_REQUIRED();
	}

	public int TOO_MANY_REQUESTS() {
		return httpStatus.TOO_MANY_REQUESTS();
	}

	public int REQUEST_HEADER_FIELDS_TOO_LARGE() {
		return httpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE();
	}

	public int UNAVAILABLE_FOR_LEGAL_REASONS() {
		return httpStatus.UNAVAILABLE_FOR_LEGAL_REASONS();
	}

	public int INTERNAL_SERVER_ERROR() {
		return httpStatus.INTERNAL_SERVER_ERROR();
	}

	public int NOT_IMPLEMENTED() {
		return httpStatus.NOT_IMPLEMENTED();
	}

	public int BAD_GATEWAY() {
		return httpStatus.BAD_GATEWAY();
	}

	public int SERVICE_UNAVAILABLE() {
		return httpStatus.SERVICE_UNAVAILABLE();
	}

	public int GATEWAY_TIMEOUT() {
		return httpStatus.GATEWAY_TIMEOUT();
	}

	public int HTTP_VERSION_NOT_SUPPORTED() {
		return httpStatus.HTTP_VERSION_NOT_SUPPORTED();
	}

	public int VARIANT_ALSO_NEGOTIATES() {
		return httpStatus.VARIANT_ALSO_NEGOTIATES();
	}

	public int INSUFFICIENT_STORAGE() {
		return httpStatus.INSUFFICIENT_STORAGE();
	}

	public int LOOP_DETECTED() {
		return httpStatus.LOOP_DETECTED();
	}

	public int BANDWIDTH_LIMIT_EXCEEDED() {
		return httpStatus.BANDWIDTH_LIMIT_EXCEEDED();
	}

	public int NOT_EXTENDED() {
		return httpStatus.NOT_EXTENDED();
	}

	public int NETWORK_AUTHENTICATION_REQUIRED() {
		return httpStatus.NETWORK_AUTHENTICATION_REQUIRED();
	}

}
