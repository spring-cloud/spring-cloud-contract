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

import java.util.Arrays;
import java.util.Map;

import org.springframework.cloud.contract.verifier.util.MetadataUtil;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Metadata representation of the Contract Verifier's HTTP communication.
 *
 * Warning! This API is experimental and can change in time.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public class ContractVerifierHttpMetadata implements SpringCloudContractMetadata {

	/**
	 * Metadata entry in the contract.
	 */
	public static final String METADATA_KEY = "verifierHttp";

	/**
	 * Scheme used for HTTP communication.
	 */
	private Scheme scheme = Scheme.HTTP;

	/**
	 * Protocol used for HTTP communication.
	 */
	private Protocol protocol = Protocol.HTTP_1_1;

	@NonNull
	public static ContractVerifierHttpMetadata fromMetadata(
			Map<String, Object> metadata) {
		return MetadataUtil.fromMetadata(metadata, METADATA_KEY,
				new ContractVerifierHttpMetadata());
	}

	@Override
	public String key() {
		return METADATA_KEY;
	}

	@Override
	public String description() {
		return "Metadata entries used by the framework";
	}

	public Scheme getScheme() {
		return scheme;
	}

	public void setScheme(Scheme scheme) {
		this.scheme = scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = Scheme.fromString(scheme);
	}

	public Protocol getProtocol() {
		return this.protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = Protocol.fromString(protocol);
	}

	/**
	 * HTTP communication scheme.
	 */
	public enum Scheme {

		/**
		 * HTTP scheme.
		 */
		HTTP,

		/**
		 * HTTPS scheme.
		 */
		HTTPS;

		/**
		 * Builds an enum from string.
		 */
		@Nullable
		public static Scheme fromString(String scheme) {
			return Arrays.stream(values()).filter(p -> p.name().equalsIgnoreCase(scheme))
					.findFirst().orElse(null);
		}

	}

	/**
	 * Taken from OKHttp's Protocol {@link okhttp3.Protocol}.
	 */
	public enum Protocol {

		/**
		 * An obsolete plaintext framing that does not use persistent sockets by default.
		 */
		HTTP_1_0("http/1.0"),

		/**
		 * A plaintext framing that includes persistent connections.
		 *
		 * This version of OkHttp implements [RFC 7230][rfc_7230], and tracks revisions to
		 * that spec.
		 *
		 * [rfc_7230]: https://tools.ietf.org/html/rfc7230
		 */
		HTTP_1_1("http/1.1"),

		/**
		 * The IETF's binary-framed protocol that includes header compression,
		 * multiplexing multiple requests on the same socket, and server-push. HTTP/1.1
		 * semantics are layered on HTTP/2.
		 *
		 * HTTP/2 requires deployments of HTTP/2 that use TLS 1.2 support
		 * [CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256], present in Java 8+ and
		 * Android 5+. Servers that enforce this may send an exception message including
		 * the string `INADEQUATE_SECURITY`.
		 */
		HTTP_2("h2"),

		/**
		 * Cleartext HTTP/2 with no "upgrade" round trip. This option requires the client
		 * to have prior knowledge that the server supports cleartext HTTP/2.
		 *
		 * See also [Starting HTTP/2 with Prior Knowledge][rfc_7540_34].
		 *
		 * [rfc_7540_34]: https://tools.ietf.org/html/rfc7540.section-3.4
		 */
		H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),

		/**
		 * QUIC (Quick UDP Internet Connection) is a new multiplexed and secure transport
		 * atop UDP, designed from the ground up and optimized for HTTP/2 semantics.
		 * HTTP/1.1 semantics are layered on HTTP/2.
		 *
		 * QUIC is not natively supported by OkHttp, but provided to allow a theoretical
		 * interceptor that provides support.
		 */
		QUIC("quic");

		private final String protocol;

		Protocol(String protocol) {
			this.protocol = protocol;
		}

		@Override
		public String toString() {
			return this.protocol;
		}

		/**
		 * Builds an enum from string.
		 */
		@Nullable
		public static Protocol fromString(String protocol) {
			return Arrays.stream(values())
					.filter(p -> p.protocol.equalsIgnoreCase(protocol)).findFirst()
					.orElse(null);
		}

	}

}
