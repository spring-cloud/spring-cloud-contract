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
import java.nio.ByteBuffer;
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
import org.jetbrains.annotations.Nullable;

/**
 * {@link HttpVerifier} implementation that uses {@link OkHttpClient}.
 * Has an inbuilt support for GRPC.
 *
 * Warning! This API is experimental and can change in time.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class OkHttpHttpVerifier implements HttpVerifier {

	private final String url;

	public OkHttpHttpVerifier(String url) {
		this.url = url;
	}

	@Override
	public Response exchange(Request request) {
		String requestContentType = request.contentType();
		// TODO: Resolve protocol and scheme from contract?
		OkHttpClient client = new OkHttpClient.Builder()
				.protocols(protocols(requestContentType)).build();
		okhttp3.Request req = new okhttp3.Request.Builder()
				.url(this.url + (request.path().startsWith("/") ? request.path()
						: "/" + request.path()))
				.method(request.method().name(), requestBody(request, requestContentType))
				.headers(Headers.of(stringTyped(request.headers()))) // TODO: Add cookies
				.build();
		try (okhttp3.Response res = client.newCall(req).execute()) {
			return response(res);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private List<Protocol> protocols(String requestContentType) {
		if (this.url.startsWith("https")) {
			return Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1);
		}
		else if (isGrpc(requestContentType)) {
			return Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE);
		}
		return Collections.singletonList(Protocol.HTTP_1_1);
	}

	private Response response(okhttp3.Response res) throws IOException {
		byte[] responseBody = responseBody(res);
		// String contentType = res.headers().get("Content-Type");
		// TODO: Response body in the test / contract should already be properly encoded
		// if (contentType != null && isGrpc(contentType) && responseBody != null) {
		// responseBody = grpcResponseBody(responseBody);
		// }
		return Response.builder().body(responseBody).statusCode(res.code())
				.headers(withSingleHeader(res))
				// TODO: Add cookies
				// .cookies(res.headers().values("Set-Cookie").stream().map(s ->
				// s.split(";")).flatMap(Arrays::stream).collect(Collectors.toMap(o -> o.
				// , e -> e.getValue().get(0), (a,b) -> a, HashMap::new))))
				.build();
	}

	@Nullable
	private RequestBody requestBody(Request request, String requestContentType) {
		if (request.body() == null) {
			return null;
		}
		byte[] bodyArray = request.body().asByteArray();
		return RequestBody.create(MediaType.parse(requestContentType), bodyArray);
	}

	private boolean isGrpc(String contentType) {
		return contentType.startsWith("application/grpc");
	}

	// the encoded body should already have proper byte values
	// TODO: This should be removed?
	private byte[] grpcRequestBody(Request request) {
		byte[] bodyArray; // TODO: Add compression support
		byte compressedFlag = 0;
		byte[] message = request.body().asByteArray();
		byte[] messageLength = ByteBuffer.allocate(4).putInt(message.length).array();
		bodyArray = ByteBuffer.allocate(1 + messageLength.length + message.length)
				.put(compressedFlag).put(messageLength).put(message).array();
		return bodyArray;
	}

	// TODO: This should be removed?
	private byte[] grpcResponseBody(byte[] responseBody) {
		// 5 value = 4th index
		// 1 for compression, 4 for message size
		int actualPayloadSize = responseBody.length - 5;
		byte[] destination = new byte[actualPayloadSize];
		System.arraycopy(responseBody, 5, destination, 0, actualPayloadSize);
		responseBody = destination;
		return responseBody;
	}

	@Nullable
	private byte[] responseBody(okhttp3.Response res) throws IOException {
		return res.body() != null ? res.body().bytes() : null;
	}

	private Map<String, Object> withSingleHeader(okhttp3.Response res) {
		return res.headers().toMultimap().entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey, e -> e.getValue().get(0), (a, b) -> a, HashMap::new));
	}

	private Map<String, String> stringTyped(Map<String, Object> headers) {
		return headers.entrySet().stream().collect(
				Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
	}

}
