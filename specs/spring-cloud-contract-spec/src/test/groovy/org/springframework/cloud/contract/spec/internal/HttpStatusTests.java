/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.spec.internal;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
public class HttpStatusTests {

	@Test
	public void CONTINUE() {
		BDDAssertions.then(HttpStatus.CONTINUE).isEqualTo(100);
	}

	@Test
	public void SWITCHING_PROTOCOLS() {
		BDDAssertions.then(HttpStatus.SWITCHING_PROTOCOLS).isEqualTo(101);
	}

	@Test
	public void PROCESSING() {
		BDDAssertions.then(HttpStatus.PROCESSING).isEqualTo(102);
	}

	@Test
	public void CHECKPOINT() {
		BDDAssertions.then(HttpStatus.CHECKPOINT).isEqualTo(103);
	}

	@Test
	public void OK() {
		BDDAssertions.then(HttpStatus.OK).isEqualTo(200);
	}

	@Test
	public void CREATED() {
		BDDAssertions.then(HttpStatus.CREATED).isEqualTo(201);
	}

	@Test
	public void ACCEPTED() {
		BDDAssertions.then(HttpStatus.ACCEPTED).isEqualTo(202);
	}

	@Test
	public void NON_AUTHORITATIVE_INFORMATION() {
		BDDAssertions.then(HttpStatus.NON_AUTHORITATIVE_INFORMATION).isEqualTo(203);
	}

	@Test
	public void NO_CONTENT() {
		BDDAssertions.then(HttpStatus.NO_CONTENT).isEqualTo(204);
	}

	@Test
	public void RESET_CONTENT() {
		BDDAssertions.then(HttpStatus.RESET_CONTENT).isEqualTo(205);
	}

	@Test
	public void PARTIAL_CONTENT() {
		BDDAssertions.then(HttpStatus.PARTIAL_CONTENT).isEqualTo(206);
	}

	@Test
	public void MULTI_STATUS() {
		BDDAssertions.then(HttpStatus.MULTI_STATUS).isEqualTo(207);
	}

	@Test
	public void ALREADY_REPORTED() {
		BDDAssertions.then(HttpStatus.ALREADY_REPORTED).isEqualTo(208);
	}

	@Test
	public void IM_USED() {
		BDDAssertions.then(HttpStatus.IM_USED).isEqualTo(226);
	}

	@Test
	public void MULTIPLE_CHOICES() {
		BDDAssertions.then(HttpStatus.MULTIPLE_CHOICES).isEqualTo(300);
	}

	@Test
	public void MOVED_PERMANENTLY() {
		BDDAssertions.then(HttpStatus.MOVED_PERMANENTLY).isEqualTo(301);
	}

	@Test
	public void FOUND() {
		BDDAssertions.then(HttpStatus.FOUND).isEqualTo(302);
	}

	@Test
	public void SEE_OTHER() {
		BDDAssertions.then(HttpStatus.SEE_OTHER).isEqualTo(303);
	}

	@Test
	public void NOT_MODIFIED() {
		BDDAssertions.then(HttpStatus.NOT_MODIFIED).isEqualTo(304);
	}

	@Test
	public void TEMPORARY_REDIRECT() {
		BDDAssertions.then(HttpStatus.TEMPORARY_REDIRECT).isEqualTo(307);
	}

	@Test
	public void PERMANENT_REDIRECT() {
		BDDAssertions.then(HttpStatus.PERMANENT_REDIRECT).isEqualTo(308);
	}

	@Test
	public void BAD_REQUEST() {
		BDDAssertions.then(HttpStatus.BAD_REQUEST).isEqualTo(400);
	}

	@Test
	public void UNAUTHORIZED() {
		BDDAssertions.then(HttpStatus.UNAUTHORIZED).isEqualTo(401);
	}

	@Test
	public void PAYMENT_REQUIRED() {
		BDDAssertions.then(HttpStatus.PAYMENT_REQUIRED).isEqualTo(402);
	}

	@Test
	public void FORBIDDEN() {
		BDDAssertions.then(HttpStatus.FORBIDDEN).isEqualTo(403);
	}

	@Test
	public void NOT_FOUND() {
		BDDAssertions.then(HttpStatus.NOT_FOUND).isEqualTo(404);
	}

	@Test
	public void METHOD_NOT_ALLOWED() {
		BDDAssertions.then(HttpStatus.METHOD_NOT_ALLOWED).isEqualTo(405);
	}

	@Test
	public void NOT_ACCEPTABLE() {
		BDDAssertions.then(HttpStatus.NOT_ACCEPTABLE).isEqualTo(406);
	}

	@Test
	public void PROXY_AUTHENTICATION_REQUIRED() {
		BDDAssertions.then(HttpStatus.PROXY_AUTHENTICATION_REQUIRED).isEqualTo(407);
	}

	@Test
	public void REQUEST_TIMEOUT() {
		BDDAssertions.then(HttpStatus.REQUEST_TIMEOUT).isEqualTo(408);
	}

	@Test
	public void CONFLICT() {
		BDDAssertions.then(HttpStatus.CONFLICT).isEqualTo(409);
	}

	@Test
	public void GONE() {
		BDDAssertions.then(HttpStatus.GONE).isEqualTo(410);
	}

	@Test
	public void LENGTH_REQUIRED() {
		BDDAssertions.then(HttpStatus.LENGTH_REQUIRED).isEqualTo(411);
	}

	@Test
	public void PRECONDITION_FAILED() {
		BDDAssertions.then(HttpStatus.PRECONDITION_FAILED).isEqualTo(412);
	}

	@Test
	public void PAYLOAD_TOO_LARGE() {
		BDDAssertions.then(HttpStatus.PAYLOAD_TOO_LARGE).isEqualTo(413);
	}

	@Test
	public void URI_TOO_LONG() {
		BDDAssertions.then(HttpStatus.URI_TOO_LONG).isEqualTo(414);
	}

	@Test
	public void UNSUPPORTED_MEDIA_TYPE() {
		BDDAssertions.then(HttpStatus.UNSUPPORTED_MEDIA_TYPE).isEqualTo(415);
	}

	@Test
	public void REQUESTED_RANGE_NOT_SATISFIABLE() {
		BDDAssertions.then(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).isEqualTo(416);
	}

	@Test
	public void EXPECTATION_FAILED() {
		BDDAssertions.then(HttpStatus.EXPECTATION_FAILED).isEqualTo(417);
	}

	@Test
	public void I_AM_A_TEAPOT() {
		BDDAssertions.then(HttpStatus.I_AM_A_TEAPOT).isEqualTo(418);
	}

	@Test
	public void UNPROCESSABLE_ENTITY() {
		BDDAssertions.then(HttpStatus.UNPROCESSABLE_ENTITY).isEqualTo(422);
	}

	@Test
	public void LOCKED() {
		BDDAssertions.then(HttpStatus.LOCKED).isEqualTo(423);
	}

	@Test
	public void FAILED_DEPENDENCY() {
		BDDAssertions.then(HttpStatus.FAILED_DEPENDENCY).isEqualTo(424);
	}

	@Test
	public void UPGRADE_REQUIRED() {
		BDDAssertions.then(HttpStatus.UPGRADE_REQUIRED).isEqualTo(426);
	}

	@Test
	public void PRECONDITION_REQUIRED() {
		BDDAssertions.then(HttpStatus.PRECONDITION_REQUIRED).isEqualTo(428);
	}

	@Test
	public void TOO_MANY_REQUESTS() {
		BDDAssertions.then(HttpStatus.TOO_MANY_REQUESTS).isEqualTo(429);
	}

	@Test
	public void REQUEST_HEADER_FIELDS_TOO_LARGE() {
		BDDAssertions.then(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE).isEqualTo(431);
	}

	@Test
	public void UNAVAILABLE_FOR_LEGAL_REASONS() {
		BDDAssertions.then(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).isEqualTo(451);
	}

	@Test
	public void INTERNAL_SERVER_ERROR() {
		BDDAssertions.then(HttpStatus.INTERNAL_SERVER_ERROR).isEqualTo(500);
	}

	@Test
	public void NOT_IMPLEMENTED() {
		BDDAssertions.then(HttpStatus.NOT_IMPLEMENTED).isEqualTo(501);
	}

	@Test
	public void BAD_GATEWAY() {
		BDDAssertions.then(HttpStatus.BAD_GATEWAY).isEqualTo(502);
	}

	@Test
	public void SERVICE_UNAVAILABLE() {
		BDDAssertions.then(HttpStatus.SERVICE_UNAVAILABLE).isEqualTo(503);
	}

	@Test
	public void GATEWAY_TIMEOUT() {
		BDDAssertions.then(HttpStatus.GATEWAY_TIMEOUT).isEqualTo(504);
	}

	@Test
	public void HTTP_VERSION_NOT_SUPPORTED() {
		BDDAssertions.then(HttpStatus.HTTP_VERSION_NOT_SUPPORTED).isEqualTo(505);
	}

	@Test
	public void VARIANT_ALSO_NEGOTIATES() {
		BDDAssertions.then(HttpStatus.VARIANT_ALSO_NEGOTIATES).isEqualTo(506);
	}

	@Test
	public void INSUFFICIENT_STORAGE() {
		BDDAssertions.then(HttpStatus.INSUFFICIENT_STORAGE).isEqualTo(507);
	}

	@Test
	public void LOOP_DETECTED() {
		BDDAssertions.then(HttpStatus.LOOP_DETECTED).isEqualTo(508);
	}

	@Test
	public void BANDWIDTH_LIMIT_EXCEEDED() {
		BDDAssertions.then(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED).isEqualTo(509);
	}

	@Test
	public void NOT_EXTENDED() {
		BDDAssertions.then(HttpStatus.NOT_EXTENDED).isEqualTo(510);
	}

	@Test
	public void NETWORK_AUTHENTICATION_REQUIRED() {
		BDDAssertions.then(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED).isEqualTo(511);
	}

}
