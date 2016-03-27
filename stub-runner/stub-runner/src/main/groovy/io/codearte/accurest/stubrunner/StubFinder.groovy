package io.codearte.accurest.stubrunner

interface StubFinder {
	/**
	 * For the given groupId and artifactId tries to find the matching
	 * URL of the running stub.
	 *
	 * @param groupId - might be null. In that case a search only via artifactId takes place
	 * @return URL of a running stub or null if not found
	 */
	URL findStubUrl(String groupId, String artifactId)

	/**
	 * For the given Ivy notation {@code groupId:artifactId} tries to find the matching
	 * URL of the running stub. You can also pass only {@code artifactId}.
	 *
	 * @param ivyNotation - Ivy representation of the Maven artifact
	 * @return URL of a running stub or null if not found
	 */
	URL findStubUrl(String ivyNotation)

	/**
	 * Returns all running stubs
	 */
	RunningStubs findAllRunningStubs()
}