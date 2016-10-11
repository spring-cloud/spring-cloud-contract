package org.springframework.cloud.contract.stubrunner;

/**
 * Exception thrown when a stub was not found
 *
 * @author Marcin Grzejszczak
 * @since 1.0.1
 */
public class StubNotFoundException extends RuntimeException {

	public StubNotFoundException(String groupId, String artifactId) {
		super("Stub not found for groupid [" + groupId + "] and artifactid [" + artifactId + "]");
	}

	public StubNotFoundException(String ivyNotation) {
		super("Stub not found for stub with notation [" + ivyNotation + "]");
	}
}
