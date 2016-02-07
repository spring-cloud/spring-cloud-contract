package io.codearte.accurest.config

/**
 * @author Jakub Kubrynski
 */
enum TestMode {
	/**
	 * Uses Spring's MockMvc
	 */
	MOCKMVC,

	/**
	 * Uses direct HTTP invocations
	 */
	EXPLICIT,

	/**
	 * Uses JAX-RS client
	 */
	JAXRSCLIENT
}