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
import org.junit.Test;

/**
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 */
public class HttpStatusTests {

	@Test
	public void CONTINUE() {
		BDDAssertions.then(HttpStatus.CONTINUE).isEqualTo(100);
		BDDAssertions.then(new HttpStatus().CONTINUE()).isEqualTo(HttpStatus.CONTINUE);
	}

	@Test
	public void SWITCHING_PROTOCOLS() {
		BDDAssertions.then(HttpStatus.SWITCHING_PROTOCOLS).isEqualTo(101);
		BDDAssertions.then(new HttpStatus().SWITCHING_PROTOCOLS())
				.isEqualTo(HttpStatus.SWITCHING_PROTOCOLS);
	}

	@Test
	public void PROCESSING() {
		BDDAssertions.then(HttpStatus.PROCESSING).isEqualTo(102);
		BDDAssertions.then(new HttpStatus().PROCESSING())
				.isEqualTo(HttpStatus.PROCESSING);
	}

	@Test
	public void CHECKPOINT() {
		BDDAssertions.then(HttpStatus.CHECKPOINT).isEqualTo(103);
		BDDAssertions.then(new HttpStatus().CHECKPOINT())
				.isEqualTo(HttpStatus.CHECKPOINT);
	}

	@Test
	public void OK() {
		BDDAssertions.then(HttpStatus.OK).isEqualTo(200);
		BDDAssertions.then(new HttpStatus().OK()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void CREATED() {
		BDDAssertions.then(HttpStatus.CREATED).isEqualTo(201);
		BDDAssertions.then(new HttpStatus().CREATED()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	public void ACCEPTED() {
		BDDAssertions.then(HttpStatus.ACCEPTED).isEqualTo(202);
		BDDAssertions.then(new HttpStatus().ACCEPTED()).isEqualTo(HttpStatus.ACCEPTED);
	}

	@Test
	public void NON_AUTHORITATIVE_INFORMATION() {
		BDDAssertions.then(HttpStatus.NON_AUTHORITATIVE_INFORMATION).isEqualTo(203);
		BDDAssertions.then(new HttpStatus().NON_AUTHORITATIVE_INFORMATION())
				.isEqualTo(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
	}

	@Test
	public void NO_CONTENT() {
		BDDAssertions.then(HttpStatus.NO_CONTENT).isEqualTo(204);
		BDDAssertions.then(new HttpStatus().NO_CONTENT())
				.isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void RESET_CONTENT() {
		BDDAssertions.then(HttpStatus.RESET_CONTENT).isEqualTo(205);
		BDDAssertions.then(new HttpStatus().RESET_CONTENT())
				.isEqualTo(HttpStatus.RESET_CONTENT);
	}

	@Test
	public void PARTIAL_CONTENT() {
		BDDAssertions.then(HttpStatus.PARTIAL_CONTENT).isEqualTo(206);
		BDDAssertions.then(new HttpStatus().PARTIAL_CONTENT())
				.isEqualTo(HttpStatus.PARTIAL_CONTENT);
	}

	@Test
	public void MULTI_STATUS() {
		BDDAssertions.then(HttpStatus.MULTI_STATUS).isEqualTo(207);
		BDDAssertions.then(new HttpStatus().MULTI_STATUS())
				.isEqualTo(HttpStatus.MULTI_STATUS);
	}

	@Test
	public void ALREADY_REPORTED() {
		BDDAssertions.then(HttpStatus.ALREADY_REPORTED).isEqualTo(208);
		BDDAssertions.then(new HttpStatus().ALREADY_REPORTED())
				.isEqualTo(HttpStatus.ALREADY_REPORTED);
	}

	@Test
	public void IM_USED() {
		BDDAssertions.then(HttpStatus.IM_USED).isEqualTo(226);
		BDDAssertions.then(new HttpStatus().IM_USED()).isEqualTo(HttpStatus.IM_USED);
	}

	@Test
	public void MULTIPLE_CHOICES() {
		BDDAssertions.then(HttpStatus.MULTIPLE_CHOICES).isEqualTo(300);
		BDDAssertions.then(new HttpStatus().MULTIPLE_CHOICES())
				.isEqualTo(HttpStatus.MULTIPLE_CHOICES);
	}

	@Test
	public void MOVED_PERMANENTLY() {
		BDDAssertions.then(HttpStatus.MOVED_PERMANENTLY).isEqualTo(301);
		BDDAssertions.then(new HttpStatus().MOVED_PERMANENTLY())
				.isEqualTo(HttpStatus.MOVED_PERMANENTLY);
	}

	@Test
	public void FOUND() {
		BDDAssertions.then(HttpStatus.FOUND).isEqualTo(302);
		BDDAssertions.then(new HttpStatus().FOUND()).isEqualTo(HttpStatus.FOUND);
	}

	@Test
	public void MOVED_TEMPORARILY() {
		BDDAssertions.then(HttpStatus.MOVED_TEMPORARILY).isEqualTo(302);
		BDDAssertions.then(new HttpStatus().MOVED_TEMPORARILY())
				.isEqualTo(HttpStatus.MOVED_TEMPORARILY);
	}

	@Test
	public void SEE_OTHER() {
		BDDAssertions.then(HttpStatus.SEE_OTHER).isEqualTo(303);
		BDDAssertions.then(new HttpStatus().SEE_OTHER()).isEqualTo(HttpStatus.SEE_OTHER);
	}

	@Test
	public void NOT_MODIFIED() {
		BDDAssertions.then(HttpStatus.NOT_MODIFIED).isEqualTo(304);
		BDDAssertions.then(new HttpStatus().NOT_MODIFIED())
				.isEqualTo(HttpStatus.NOT_MODIFIED);
	}

	@Test
	public void USE_PROXY() {
		BDDAssertions.then(HttpStatus.USE_PROXY).isEqualTo(305);
		BDDAssertions.then(new HttpStatus().USE_PROXY()).isEqualTo(HttpStatus.USE_PROXY);
	}

	@Test
	public void TEMPORARY_REDIRECT() {
		BDDAssertions.then(HttpStatus.TEMPORARY_REDIRECT).isEqualTo(307);
		BDDAssertions.then(new HttpStatus().TEMPORARY_REDIRECT())
				.isEqualTo(HttpStatus.TEMPORARY_REDIRECT);
	}

	@Test
	public void PERMANENT_REDIRECT() {
		BDDAssertions.then(HttpStatus.PERMANENT_REDIRECT).isEqualTo(308);
		BDDAssertions.then(new HttpStatus().PERMANENT_REDIRECT())
				.isEqualTo(HttpStatus.PERMANENT_REDIRECT);
	}

	@Test
	public void BAD_REQUEST() {
		BDDAssertions.then(HttpStatus.BAD_REQUEST).isEqualTo(400);
		BDDAssertions.then(new HttpStatus().BAD_REQUEST())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void UNAUTHORIZED() {
		BDDAssertions.then(HttpStatus.UNAUTHORIZED).isEqualTo(401);
		BDDAssertions.then(new HttpStatus().UNAUTHORIZED())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void PAYMENT_REQUIRED() {
		BDDAssertions.then(HttpStatus.PAYMENT_REQUIRED).isEqualTo(402);
		BDDAssertions.then(new HttpStatus().PAYMENT_REQUIRED())
				.isEqualTo(HttpStatus.PAYMENT_REQUIRED);
	}

	@Test
	public void FORBIDDEN() {
		BDDAssertions.then(HttpStatus.FORBIDDEN).isEqualTo(403);
		BDDAssertions.then(new HttpStatus().FORBIDDEN()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void NOT_FOUND() {
		BDDAssertions.then(HttpStatus.NOT_FOUND).isEqualTo(404);
		BDDAssertions.then(new HttpStatus().NOT_FOUND()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void METHOD_NOT_ALLOWED() {
		BDDAssertions.then(HttpStatus.METHOD_NOT_ALLOWED).isEqualTo(405);
		BDDAssertions.then(new HttpStatus().METHOD_NOT_ALLOWED())
				.isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	public void NOT_ACCEPTABLE() {
		BDDAssertions.then(HttpStatus.NOT_ACCEPTABLE).isEqualTo(406);
		BDDAssertions.then(new HttpStatus().NOT_ACCEPTABLE())
				.isEqualTo(HttpStatus.NOT_ACCEPTABLE);
	}

	@Test
	public void PROXY_AUTHENTICATION_REQUIRED() {
		BDDAssertions.then(HttpStatus.PROXY_AUTHENTICATION_REQUIRED).isEqualTo(407);
		BDDAssertions.then(new HttpStatus().PROXY_AUTHENTICATION_REQUIRED())
				.isEqualTo(HttpStatus.PROXY_AUTHENTICATION_REQUIRED);
	}

	@Test
	public void REQUEST_TIMEOUT() {
		BDDAssertions.then(HttpStatus.REQUEST_TIMEOUT).isEqualTo(408);
		BDDAssertions.then(new HttpStatus().REQUEST_TIMEOUT())
				.isEqualTo(HttpStatus.REQUEST_TIMEOUT);
	}

	@Test
	public void CONFLICT() {
		BDDAssertions.then(HttpStatus.CONFLICT).isEqualTo(409);
		BDDAssertions.then(new HttpStatus().CONFLICT()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	public void GONE() {
		BDDAssertions.then(HttpStatus.GONE).isEqualTo(410);
		BDDAssertions.then(new HttpStatus().GONE()).isEqualTo(HttpStatus.GONE);
	}

	@Test
	public void LENGTH_REQUIRED() {
		BDDAssertions.then(HttpStatus.LENGTH_REQUIRED).isEqualTo(411);
		BDDAssertions.then(new HttpStatus().LENGTH_REQUIRED())
				.isEqualTo(HttpStatus.LENGTH_REQUIRED);
	}

	@Test
	public void PRECONDITION_FAILED() {
		BDDAssertions.then(HttpStatus.PRECONDITION_FAILED).isEqualTo(412);
		BDDAssertions.then(new HttpStatus().PRECONDITION_FAILED())
				.isEqualTo(HttpStatus.PRECONDITION_FAILED);
	}

	@Test
	public void PAYLOAD_TOO_LARGE() {
		BDDAssertions.then(HttpStatus.PAYLOAD_TOO_LARGE).isEqualTo(413);
		BDDAssertions.then(new HttpStatus().PAYLOAD_TOO_LARGE())
				.isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
	}

	@Test
	public void REQUEST_ENTITY_TOO_LARGE() {
		BDDAssertions.then(HttpStatus.REQUEST_ENTITY_TOO_LARGE).isEqualTo(413);
		BDDAssertions.then(new HttpStatus().REQUEST_ENTITY_TOO_LARGE())
				.isEqualTo(HttpStatus.REQUEST_ENTITY_TOO_LARGE);
	}

	@Test
	public void URI_TOO_LONG() {
		BDDAssertions.then(HttpStatus.URI_TOO_LONG).isEqualTo(414);
		BDDAssertions.then(new HttpStatus().URI_TOO_LONG())
				.isEqualTo(HttpStatus.URI_TOO_LONG);
	}

	@Test
	public void REQUEST_URI_TOO_LONG() {
		BDDAssertions.then(HttpStatus.REQUEST_URI_TOO_LONG).isEqualTo(414);
		BDDAssertions.then(new HttpStatus().REQUEST_URI_TOO_LONG())
				.isEqualTo(HttpStatus.REQUEST_URI_TOO_LONG);
	}

	@Test
	public void UNSUPPORTED_MEDIA_TYPE() {
		BDDAssertions.then(HttpStatus.UNSUPPORTED_MEDIA_TYPE).isEqualTo(415);
		BDDAssertions.then(new HttpStatus().UNSUPPORTED_MEDIA_TYPE())
				.isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	@Test
	public void REQUESTED_RANGE_NOT_SATISFIABLE() {
		BDDAssertions.then(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).isEqualTo(416);
		BDDAssertions.then(new HttpStatus().REQUESTED_RANGE_NOT_SATISFIABLE())
				.isEqualTo(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
	}

	@Test
	public void EXPECTATION_FAILED() {
		BDDAssertions.then(HttpStatus.EXPECTATION_FAILED).isEqualTo(417);
		BDDAssertions.then(new HttpStatus().EXPECTATION_FAILED())
				.isEqualTo(HttpStatus.EXPECTATION_FAILED);
	}

	@Test
	public void I_AM_A_TEAPOT() {
		BDDAssertions.then(HttpStatus.I_AM_A_TEAPOT).isEqualTo(418);
		BDDAssertions.then(new HttpStatus().I_AM_A_TEAPOT())
				.isEqualTo(HttpStatus.I_AM_A_TEAPOT);
	}

	@Test
	public void INSUFFICIENT_SPACE_ON_RESOURCE() {
		BDDAssertions.then(HttpStatus.INSUFFICIENT_SPACE_ON_RESOURCE).isEqualTo(419);
		BDDAssertions.then(new HttpStatus().INSUFFICIENT_SPACE_ON_RESOURCE())
				.isEqualTo(HttpStatus.INSUFFICIENT_SPACE_ON_RESOURCE);
	}

	@Test
	public void METHOD_FAILURE() {
		BDDAssertions.then(HttpStatus.METHOD_FAILURE).isEqualTo(420);
		BDDAssertions.then(new HttpStatus().METHOD_FAILURE())
				.isEqualTo(HttpStatus.METHOD_FAILURE);
	}

	@Test
	public void DESTINATION_LOCKED() {
		BDDAssertions.then(HttpStatus.DESTINATION_LOCKED).isEqualTo(421);
		BDDAssertions.then(new HttpStatus().DESTINATION_LOCKED())
				.isEqualTo(HttpStatus.DESTINATION_LOCKED);
	}

	@Test
	public void UNPROCESSABLE_ENTITY() {
		BDDAssertions.then(HttpStatus.UNPROCESSABLE_ENTITY).isEqualTo(422);
		BDDAssertions.then(new HttpStatus().UNPROCESSABLE_ENTITY())
				.isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void LOCKED() {
		BDDAssertions.then(HttpStatus.LOCKED).isEqualTo(423);
		BDDAssertions.then(new HttpStatus().LOCKED()).isEqualTo(HttpStatus.LOCKED);
	}

	@Test
	public void FAILED_DEPENDENCY() {
		BDDAssertions.then(HttpStatus.FAILED_DEPENDENCY).isEqualTo(424);
		BDDAssertions.then(new HttpStatus().FAILED_DEPENDENCY())
				.isEqualTo(HttpStatus.FAILED_DEPENDENCY);
	}

	@Test
	public void UPGRADE_REQUIRED() {
		BDDAssertions.then(HttpStatus.UPGRADE_REQUIRED).isEqualTo(426);
		BDDAssertions.then(new HttpStatus().UPGRADE_REQUIRED())
				.isEqualTo(HttpStatus.UPGRADE_REQUIRED);
	}

	@Test
	public void PRECONDITION_REQUIRED() {
		BDDAssertions.then(HttpStatus.PRECONDITION_REQUIRED).isEqualTo(428);
		BDDAssertions.then(new HttpStatus().PRECONDITION_REQUIRED())
				.isEqualTo(HttpStatus.PRECONDITION_REQUIRED);
	}

	@Test
	public void TOO_MANY_REQUESTS() {
		BDDAssertions.then(HttpStatus.TOO_MANY_REQUESTS).isEqualTo(429);
		BDDAssertions.then(new HttpStatus().TOO_MANY_REQUESTS())
				.isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	public void REQUEST_HEADER_FIELDS_TOO_LARGE() {
		BDDAssertions.then(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE).isEqualTo(431);
		BDDAssertions.then(new HttpStatus().REQUEST_HEADER_FIELDS_TOO_LARGE())
				.isEqualTo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
	}

	@Test
	public void UNAVAILABLE_FOR_LEGAL_REASONS() {
		BDDAssertions.then(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).isEqualTo(451);
		BDDAssertions.then(new HttpStatus().UNAVAILABLE_FOR_LEGAL_REASONS())
				.isEqualTo(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
	}

	@Test
	public void INTERNAL_SERVER_ERROR() {
		BDDAssertions.then(HttpStatus.INTERNAL_SERVER_ERROR).isEqualTo(500);
		BDDAssertions.then(new HttpStatus().INTERNAL_SERVER_ERROR())
				.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	public void NOT_IMPLEMENTED() {
		BDDAssertions.then(HttpStatus.NOT_IMPLEMENTED).isEqualTo(501);
		BDDAssertions.then(new HttpStatus().NOT_IMPLEMENTED())
				.isEqualTo(HttpStatus.NOT_IMPLEMENTED);
	}

	@Test
	public void BAD_GATEWAY() {
		BDDAssertions.then(HttpStatus.BAD_GATEWAY).isEqualTo(502);
		BDDAssertions.then(new HttpStatus().BAD_GATEWAY())
				.isEqualTo(HttpStatus.BAD_GATEWAY);
	}

	@Test
	public void SERVICE_UNAVAILABLE() {
		BDDAssertions.then(HttpStatus.SERVICE_UNAVAILABLE).isEqualTo(503);
		BDDAssertions.then(new HttpStatus().SERVICE_UNAVAILABLE())
				.isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	public void GATEWAY_TIMEOUT() {
		BDDAssertions.then(HttpStatus.GATEWAY_TIMEOUT).isEqualTo(504);
		BDDAssertions.then(new HttpStatus().GATEWAY_TIMEOUT())
				.isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
	}

	@Test
	public void HTTP_VERSION_NOT_SUPPORTED() {
		BDDAssertions.then(HttpStatus.HTTP_VERSION_NOT_SUPPORTED).isEqualTo(505);
		BDDAssertions.then(new HttpStatus().HTTP_VERSION_NOT_SUPPORTED())
				.isEqualTo(HttpStatus.HTTP_VERSION_NOT_SUPPORTED);
	}

	@Test
	public void VARIANT_ALSO_NEGOTIATES() {
		BDDAssertions.then(HttpStatus.VARIANT_ALSO_NEGOTIATES).isEqualTo(506);
		BDDAssertions.then(new HttpStatus().VARIANT_ALSO_NEGOTIATES())
				.isEqualTo(HttpStatus.VARIANT_ALSO_NEGOTIATES);
	}

	@Test
	public void INSUFFICIENT_STORAGE() {
		BDDAssertions.then(HttpStatus.INSUFFICIENT_STORAGE).isEqualTo(507);
		BDDAssertions.then(new HttpStatus().INSUFFICIENT_STORAGE())
				.isEqualTo(HttpStatus.INSUFFICIENT_STORAGE);
	}

	@Test
	public void LOOP_DETECTED() {
		BDDAssertions.then(HttpStatus.LOOP_DETECTED).isEqualTo(508);
		BDDAssertions.then(new HttpStatus().LOOP_DETECTED())
				.isEqualTo(HttpStatus.LOOP_DETECTED);
	}

	@Test
	public void BANDWIDTH_LIMIT_EXCEEDED() {
		BDDAssertions.then(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED).isEqualTo(509);
		BDDAssertions.then(new HttpStatus().BANDWIDTH_LIMIT_EXCEEDED())
				.isEqualTo(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
	}

	@Test
	public void NOT_EXTENDED() {
		BDDAssertions.then(HttpStatus.NOT_EXTENDED).isEqualTo(510);
		BDDAssertions.then(new HttpStatus().NOT_EXTENDED())
				.isEqualTo(HttpStatus.NOT_EXTENDED);
	}

	@Test
	public void NETWORK_AUTHENTICATION_REQUIRED() {
		BDDAssertions.then(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED).isEqualTo(511);
		BDDAssertions.then(new HttpStatus().NETWORK_AUTHENTICATION_REQUIRED())
				.isEqualTo(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED);
	}

}
