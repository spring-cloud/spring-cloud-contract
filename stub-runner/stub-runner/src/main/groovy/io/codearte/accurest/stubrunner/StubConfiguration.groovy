package io.codearte.accurest.stubrunner

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import io.codearte.accurest.stubrunner.util.StringUtils
/**
 * Represents a configuration of a single stub. The stub can be described
 * by groupId:artifactId:classifier notation
 */
@CompileStatic
@EqualsAndHashCode
public class StubConfiguration {
	private static final String STUB_COLON_DELIMITER = ":"

	final String groupId
	final String artifactId
	final String classifier

	public StubConfiguration(String groupId, String artifactId, String classifier) {
		this.groupId = groupId
		this.artifactId = artifactId
		this.classifier = classifier
	}

	public StubConfiguration(String stubPath, String defaultClassifier = "stubs") {
		String[] parsedPath = parsedPathEmptyByDefault(stubPath, STUB_COLON_DELIMITER, defaultClassifier)
		this.groupId = parsedPath[0]
		this.artifactId = parsedPath[1]
		this.classifier = parsedPath[2]
	}

	private List<String> parsedPathEmptyByDefault(String path, String delimiter, String defaultClassifier) {
		String[] splitPath = path.split(delimiter)
		String stubsGroupId = ""
		String stubsArtifactId = ""
		String stubsClassifier = ""
		if (splitPath.length >= 2) {
			stubsGroupId = splitPath[0]
			stubsArtifactId = splitPath[1]
			stubsClassifier = splitPath.length == 3 ? splitPath[2] : defaultClassifier
		}
		return [stubsGroupId, stubsArtifactId, stubsClassifier]
	}

	private boolean isDefined() {
		return StringUtils.hasText(groupId) && StringUtils.hasText(this.artifactId)
	}

	boolean hasClassifier() {
		return StringUtils.hasText(classifier)
	}

	String toColonSeparatedDependencyNotation() {
		if(!isDefined()) {
			return ""
		}
		return [groupId, artifactId, classifier].join(STUB_COLON_DELIMITER)
	}

	@CompileDynamic
	boolean matches(String ivyNotationAsString) {
		def (String groupId, String artifactId) = ivyNotationFrom(ivyNotationAsString)
		if (!groupId) {
			return this.artifactId == artifactId
		}
		return this.groupId == groupId && this.artifactId == artifactId
	}

	private String[] ivyNotationFrom(String ivyNotation) {
		String[] splitString = ivyNotation.split(":")
		if (splitString.length == 1) {
			// assuming that ivy notation represents artifactId only
			return [null, splitString[0]] as String[]
		}
		return [splitString[0], splitString[1]] as String[]
	}


	@Override
	public String toString() {
		return toColonSeparatedDependencyNotation()
	}
}