package org.springframework.cloud.contract.spec.internal;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;

/**
 * @author Marcin Grzejszczak
 */
public class HttpStatusTests {

	@Test public void CONTINUE() {
		BDDAssertions.then(new HttpStatus().CONTINUE()).isEqualTo(100);
	}

	@Test public void SWITCHING_PROTOCOLS() {
		BDDAssertions.then(new HttpStatus().SWITCHING_PROTOCOLS()).isEqualTo(101);
	}

	@Test public void PROCESSING() {
		BDDAssertions.then(new HttpStatus().PROCESSING()).isEqualTo(102);
	}

	@Test public void CHECKPOINT() {
		BDDAssertions.then(new HttpStatus().CHECKPOINT()).isEqualTo(103);
	}

	@Test public void OK() {
		BDDAssertions.then(new HttpStatus().OK()).isEqualTo(200);
	}

	@Test public void CREATED() {
		BDDAssertions.then(new HttpStatus().CREATED()).isEqualTo(201);
	}

	@Test public void ACCEPTED() {
		BDDAssertions.then(new HttpStatus().ACCEPTED()).isEqualTo(202);
	}

	@Test public void NON_AUTHORITATIVE_INFORMATION() {
		BDDAssertions.then(new HttpStatus().NON_AUTHORITATIVE_INFORMATION()).isEqualTo(203);
	}

	@Test public void NO_CONTENT() {
		BDDAssertions.then(new HttpStatus().NO_CONTENT()).isEqualTo(204);
	}

	@Test public void RESET_CONTENT() {
		BDDAssertions.then(new HttpStatus().RESET_CONTENT()).isEqualTo(205);
	}

	@Test public void PARTIAL_CONTENT() {
		BDDAssertions.then(new HttpStatus().PARTIAL_CONTENT()).isEqualTo(206);
	}

	@Test public void MULTI_STATUS() {
		BDDAssertions.then(new HttpStatus().MULTI_STATUS()).isEqualTo(207);
	}

	@Test public void ALREADY_REPORTED() {
		BDDAssertions.then(new HttpStatus().ALREADY_REPORTED()).isEqualTo(208);
	}

	@Test public void IM_USED() {
		BDDAssertions.then(new HttpStatus().IM_USED()).isEqualTo(226);
	}

	@Test public void MULTIPLE_CHOICES() {
		BDDAssertions.then(new HttpStatus().MULTIPLE_CHOICES()).isEqualTo(300);
	}

	@Test public void MOVED_PERMANENTLY() {
		BDDAssertions.then(new HttpStatus().MOVED_PERMANENTLY()).isEqualTo(301);
	}

	@Test public void FOUND() {
		BDDAssertions.then(new HttpStatus().FOUND()).isEqualTo(302);
	}

	@Test public void MOVED_TEMPORARILY() {
		BDDAssertions.then(new HttpStatus().MOVED_TEMPORARILY()).isEqualTo(302);
	}

	@Test public void SEE_OTHER() {
		BDDAssertions.then(new HttpStatus().SEE_OTHER()).isEqualTo(303);
	}

	@Test public void NOT_MODIFIED() {
		BDDAssertions.then(new HttpStatus().NOT_MODIFIED()).isEqualTo(304);
	}

	@Test public void USE_PROXY() {
		BDDAssertions.then(new HttpStatus().USE_PROXY()).isEqualTo(305);
	}

	@Test public void TEMPORARY_REDIRECT() {
		BDDAssertions.then(new HttpStatus().TEMPORARY_REDIRECT()).isEqualTo(307);
	}

	@Test public void PERMANENT_REDIRECT() {
		BDDAssertions.then(new HttpStatus().PERMANENT_REDIRECT()).isEqualTo(308);
	}

	@Test public void BAD_REQUEST() {
		BDDAssertions.then(new HttpStatus().BAD_REQUEST()).isEqualTo(400);
	}

	@Test public void UNAUTHORIZED() {
		BDDAssertions.then(new HttpStatus().UNAUTHORIZED()).isEqualTo(401);
	}

	@Test public void PAYMENT_REQUIRED() {
		BDDAssertions.then(new HttpStatus().PAYMENT_REQUIRED()).isEqualTo(402);
	}

	@Test public void FORBIDDEN() {
		BDDAssertions.then(new HttpStatus().FORBIDDEN()).isEqualTo(403);
	}

	@Test public void NOT_FOUND() {
		BDDAssertions.then(new HttpStatus().NOT_FOUND()).isEqualTo(404);
	}

	@Test public void METHOD_NOT_ALLOWED() {
		BDDAssertions.then(new HttpStatus().METHOD_NOT_ALLOWED()).isEqualTo(405);
	}

	@Test public void NOT_ACCEPTABLE() {
		BDDAssertions.then(new HttpStatus().NOT_ACCEPTABLE()).isEqualTo(406);
	}

	@Test public void PROXY_AUTHENTICATION_REQUIRED() {
		BDDAssertions.then(new HttpStatus().PROXY_AUTHENTICATION_REQUIRED()).isEqualTo(407);
	}

	@Test public void REQUEST_TIMEOUT() {
		BDDAssertions.then(new HttpStatus().REQUEST_TIMEOUT()).isEqualTo(408);
	}

	@Test public void CONFLICT() {
		BDDAssertions.then(new HttpStatus().CONFLICT()).isEqualTo(409);
	}

	@Test public void GONE() {
		BDDAssertions.then(new HttpStatus().GONE()).isEqualTo(410);
	}

	@Test public void LENGTH_REQUIRED() {
		BDDAssertions.then(new HttpStatus().LENGTH_REQUIRED()).isEqualTo(411);
	}

	@Test public void PRECONDITION_FAILED() {
		BDDAssertions.then(new HttpStatus().PRECONDITION_FAILED()).isEqualTo(412);
	}

	@Test public void PAYLOAD_TOO_LARGE() {
		BDDAssertions.then(new HttpStatus().PAYLOAD_TOO_LARGE()).isEqualTo(413);
	}

	@Test public void REQUEST_ENTITY_TOO_LARGE() {
		BDDAssertions.then(new HttpStatus().REQUEST_ENTITY_TOO_LARGE()).isEqualTo(413);
	}

	@Test public void URI_TOO_LONG() {
		BDDAssertions.then(new HttpStatus().URI_TOO_LONG()).isEqualTo(414);
	}

	@Test public void REQUEST_URI_TOO_LONG() {
		BDDAssertions.then(new HttpStatus().REQUEST_URI_TOO_LONG()).isEqualTo(414);
	}

	@Test public void UNSUPPORTED_MEDIA_TYPE() {
		BDDAssertions.then(new HttpStatus().UNSUPPORTED_MEDIA_TYPE()).isEqualTo(415);
	}

	@Test public void REQUESTED_RANGE_NOT_SATISFIABLE() {
		BDDAssertions.then(new HttpStatus().REQUESTED_RANGE_NOT_SATISFIABLE()).isEqualTo(416);
	}

	@Test public void EXPECTATION_FAILED() {
		BDDAssertions.then(new HttpStatus().EXPECTATION_FAILED()).isEqualTo(417);
	}

	@Test public void I_AM_A_TEAPOT() {
		BDDAssertions.then(new HttpStatus().I_AM_A_TEAPOT()).isEqualTo(418);
	}

	@Test public void INSUFFICIENT_SPACE_ON_RESOURCE() {
		BDDAssertions.then(new HttpStatus().INSUFFICIENT_SPACE_ON_RESOURCE()).isEqualTo(419);
	}

	@Test public void METHOD_FAILURE() {
		BDDAssertions.then(new HttpStatus().METHOD_FAILURE()).isEqualTo(420);
	}

	@Test public void DESTINATION_LOCKED() {
		BDDAssertions.then(new HttpStatus().DESTINATION_LOCKED()).isEqualTo(421);
	}

	@Test public void UNPROCESSABLE_ENTITY() {
		BDDAssertions.then(new HttpStatus().UNPROCESSABLE_ENTITY()).isEqualTo(422);
	}

	@Test public void LOCKED() {
		BDDAssertions.then(new HttpStatus().LOCKED()).isEqualTo(423);
	}

	@Test public void FAILED_DEPENDENCY() {
		BDDAssertions.then(new HttpStatus().FAILED_DEPENDENCY()).isEqualTo(424);
	}

	@Test public void UPGRADE_REQUIRED() {
		BDDAssertions.then(new HttpStatus().UPGRADE_REQUIRED()).isEqualTo(426);
	}

	@Test public void PRECONDITION_REQUIRED() {
		BDDAssertions.then(new HttpStatus().PRECONDITION_REQUIRED()).isEqualTo(428);
	}

	@Test public void TOO_MANY_REQUESTS() {
		BDDAssertions.then(new HttpStatus().TOO_MANY_REQUESTS()).isEqualTo(429);
	}

	@Test public void REQUEST_HEADER_FIELDS_TOO_LARGE() {
		BDDAssertions.then(new HttpStatus().REQUEST_HEADER_FIELDS_TOO_LARGE()).isEqualTo(431);
	}

	@Test public void UNAVAILABLE_FOR_LEGAL_REASONS() {
		BDDAssertions.then(new HttpStatus().UNAVAILABLE_FOR_LEGAL_REASONS()).isEqualTo(451);
	}

	@Test public void INTERNAL_SERVER_ERROR() {
		BDDAssertions.then(new HttpStatus().INTERNAL_SERVER_ERROR()).isEqualTo(500);
	}

	@Test public void NOT_IMPLEMENTED() {
		BDDAssertions.then(new HttpStatus().NOT_IMPLEMENTED()).isEqualTo(501);
	}

	@Test public void BAD_GATEWAY() {
		BDDAssertions.then(new HttpStatus().BAD_GATEWAY()).isEqualTo(502);
	}

	@Test public void SERVICE_UNAVAILABLE() {
		BDDAssertions.then(new HttpStatus().SERVICE_UNAVAILABLE()).isEqualTo(503);
	}

	@Test public void GATEWAY_TIMEOUT() {
		BDDAssertions.then(new HttpStatus().GATEWAY_TIMEOUT()).isEqualTo(504);
	}

	@Test public void HTTP_VERSION_NOT_SUPPORTED() {
		BDDAssertions.then(new HttpStatus().HTTP_VERSION_NOT_SUPPORTED()).isEqualTo(505);
	}

	@Test public void VARIANT_ALSO_NEGOTIATES() {
		BDDAssertions.then(new HttpStatus().VARIANT_ALSO_NEGOTIATES()).isEqualTo(506);
	}

	@Test public void INSUFFICIENT_STORAGE() {
		BDDAssertions.then(new HttpStatus().INSUFFICIENT_STORAGE()).isEqualTo(507);
	}

	@Test public void LOOP_DETECTED() {
		BDDAssertions.then(new HttpStatus().LOOP_DETECTED()).isEqualTo(508);
	}

	@Test public void BANDWIDTH_LIMIT_EXCEEDED() {
		BDDAssertions.then(new HttpStatus().BANDWIDTH_LIMIT_EXCEEDED()).isEqualTo(509);
	}

	@Test public void NOT_EXTENDED() {
		BDDAssertions.then(new HttpStatus().NOT_EXTENDED()).isEqualTo(510);
	}

	@Test public void NETWORK_AUTHENTICATION_REQUIRED() {
		BDDAssertions.then(new HttpStatus().NETWORK_AUTHENTICATION_REQUIRED()).isEqualTo(511);
	}
}