/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import org.springframework.util.StringUtils;

/**
 * Represents a configuration of a single stub. The stub can be described by
 * groupId:artifactId:classifier notation
 */
public class StubConfiguration {
	private static final String STUB_COLON_DELIMITER = ":";
	private static final String DEFAULT_VERSION = "+";
	public static final String DEFAULT_CLASSIFIER = "stubs";

	final String groupId;
	final String artifactId;
	final String version;
	final String classifier;

	public StubConfiguration(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = DEFAULT_CLASSIFIER;
	}

	public StubConfiguration(String groupId, String artifactId, String version,
			String classifier) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
	}

	public StubConfiguration(String stubPath, String defaultClassifier) {
		String[] parsedPath = parsedPathEmptyByDefault(stubPath, STUB_COLON_DELIMITER,
				defaultClassifier);
		this.groupId = parsedPath[0];
		this.artifactId = parsedPath[1];
		this.version = parsedPath[2];
		this.classifier = parsedPath[3];
	}

	public StubConfiguration(String stubPath) {
		String[] parsedPath = parsedPathEmptyByDefault(stubPath, STUB_COLON_DELIMITER,
				DEFAULT_CLASSIFIER);
		this.groupId = parsedPath[0];
		this.artifactId = parsedPath[1];
		this.version = parsedPath[2];
		this.classifier = parsedPath[3];
	}

	private String[] parsedPathEmptyByDefault(String path, String delimiter,
			String defaultClassifier) {
		String[] splitPath = path.split(delimiter);
		String stubsGroupId = "";
		String stubsArtifactId = "";
		String stubsVersion = "";
		String stubsClassifier = "";
		if (splitPath.length >= 2) {
			stubsGroupId = splitPath[0];
			stubsArtifactId = splitPath[1];
			stubsVersion = splitPath.length >= 3 ? splitPath[2] : DEFAULT_VERSION;
			stubsClassifier = splitPath.length == 4 ? splitPath[3] : defaultClassifier;
		}
		return new String[] { stubsGroupId, stubsArtifactId, stubsVersion,
				stubsClassifier };
	}

	private boolean isDefined() {
		return StringUtils.hasText(this.groupId) && StringUtils.hasText(this.artifactId);
	}

	public String toColonSeparatedDependencyNotation() {
		if (!isDefined()) {
			return "";
		}
		return StringUtils.arrayToDelimitedString(
				new String[] { this.groupId, this.artifactId, this.version, this.classifier },
				STUB_COLON_DELIMITER);
	}

	public boolean groupIdAndArtifactMatches(String ivyNotationAsString) {
		String[] parts = ivyNotationFrom(ivyNotationAsString);
		String groupId = parts[0];
		String artifactId = parts[1];
		if (groupId == null) {
			return this.artifactId.equals(artifactId);
		}
		return this.groupId.equals(groupId) && this.artifactId.equals(artifactId);
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getClassifier() {
		return this.classifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.artifactId == null) ? 0 : this.artifactId.hashCode());
		result = prime * result + ((this.groupId == null) ? 0 : this.groupId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StubConfiguration other = (StubConfiguration) obj;
		if (this.artifactId == null) {
			if (other.artifactId != null)
				return false;
		}
		else if (!this.artifactId.equals(other.artifactId))
			return false;
		if (this.groupId == null) {
			if (other.groupId != null)
				return false;
		}
		else if (!this.groupId.equals(other.groupId))
			return false;
		return true;
	}

	public boolean matchesIvyNotation(String ivyNotationAsString) {
		String[] strings = ivyNotationAsString.split(":");
		if (strings.length == 1) {
			return this.artifactId.equals(ivyNotationAsString);
		}
		else if (strings.length == 2) {
			return this.groupId.equals(strings[0]) && this.artifactId.equals(strings[1]);
		}
		else if (strings.length == 3) {
			return this.groupId.equals(strings[0]) && this.artifactId.equals(strings[1])
					&& (strings[2].equals(DEFAULT_VERSION) || this.version.equals(strings[2]));
		}
		return this.groupId.equals(strings[0]) && this.artifactId.equals(strings[1])
				&& (strings[2].equals(DEFAULT_VERSION) || this.version.equals(strings[2]))
				&& this.classifier.equals(strings[3]);
	}

	private String[] ivyNotationFrom(String ivyNotation) {
		String[] splitString = ivyNotation.split(":");
		if (splitString.length == 1) {
			// assuming that ivy notation represents artifactId only
			return new String[] { null, splitString[0] };
		}
		return new String[] { splitString[0], splitString[1] };
	}

	@Override
	public String toString() {
		return toColonSeparatedDependencyNotation();
	}
}