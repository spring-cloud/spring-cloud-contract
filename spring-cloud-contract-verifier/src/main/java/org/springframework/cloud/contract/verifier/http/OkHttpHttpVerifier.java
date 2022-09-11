/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.http;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.RequestBody;

/**
 * {@link HttpVerifier} implementation that uses {@link OkHttpClient}. Has an inbuilt
 * support for GRPC.
 *
 * Warning! This API is experimental and can change in time.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class OkHttpHttpVerifier implements HttpVerifier {

	private final String hostAndPort;

	/**
	 * @param hostAndPort - don't pass the scheme, it will be resolved from
	 * {@link Request#scheme()}. E.g. pass {@code localhost:1234}.
	 */
	public OkHttpHttpVerifier(String hostAndPort) {
		this.hostAndPort = hostAndPort;
	}

	@Override
	public Response exchange(Request request) {
		String requestContentType = request.contentType();
		OkHttpClient client = new OkHttpClient.Builder().protocols(toProtocol(request.protocol().toString())).build();
		Map<String, String> headers = stringTyped(request.headers());
		if (!request.cookies().isEmpty()) {
			headers.put("Set-Cookie", request.cookies().entrySet().stream()
					.map(e -> e.getKey() + "=" + e.getValue().toString()).collect(Collectors.joining(";")));
		}
		okhttp3.Request req = new okhttp3.Request.Builder().url(url(request))
				.method(request.method().name(), requestBody(request, requestContentType)).headers(Headers.of(headers))
				.build();
		try (okhttp3.Response res = client.newCall(req).execute()) {
			return response(res);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private String url(Request request) {
		String url = request.scheme().name().toLowerCase() + ":" + this.hostAndPort
				+ (request.path().startsWith("/") ? request.path() : "/" + request.path());
		if (!request.queryParams().isEmpty()) {
			return url + "?" + request.queryParams().stream().map(e -> e.getKey() + "=" + e.getValue())
					.collect(Collectors.joining("&"));
		}
		return url;
	}

	private List<Protocol> toProtocol(String string) {
		try {
			Protocol protocol = Protocol.get(string);
			switch (protocol) {
				case HTTP_1_0:
				case HTTP_2:
				case QUIC:
					return Arrays.asList(protocol, Protocol.HTTP_1_1);
				case HTTP_1_1:
					return Collections.singletonList(Protocol.HTTP_1_1);
				case H2_PRIOR_KNOWLEDGE:
					return Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE);
			}
			return Collections.emptyList();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private Response response(okhttp3.Response res) throws IOException {
		byte[] responseBody = responseBody(res);
		return Response.builder().body(responseBody).statusCode(res.code()).headers(withSingleHeader(res)).cookies(
				res.headers().values("Set-Cookie").stream().flatMap(s -> Arrays.stream(s.split(";"))).map(s -> {
					String[] singleCookie = s.split("=");
					return new AbstractMap.SimpleEntry<>(singleCookie[0],
							singleCookie.length > 1 ? singleCookie[1] : "");
				}).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue,
						(a, b) -> a, HashMap::new)))
				.build();
	}

	private RequestBody requestBody(Request request, String requestContentType) {
		if (request.body() == null) {
			return null;
		}
		byte[] bodyArray = request.body().asByteArray();
		return RequestBody.create(MediaType.parse(requestContentType), bodyArray);
	}

	private byte[] responseBody(okhttp3.Response res) throws IOException {
		return res.body() != null ? res.body().bytes() : null;
	}

	private Map<String, Object> withSingleHeader(okhttp3.Response res) {
		return res.headers().toMultimap().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0), (a, b) -> a, HashMap::new));
	}

	private Map<String, String> stringTyped(Map<String, Object> headers) {
		return headers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
	}

}
