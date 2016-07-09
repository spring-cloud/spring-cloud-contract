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

package org.springframework.cloud.contract.stubrunner

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.springframework.cloud.contract.stubrunner.util.StringUtils

/**
 * Represents a configuration of a single stub. The stub can be described
 * by groupId:artifactId:classifier notation
 */
@CompileStatic
@EqualsAndHashCode
public class StubConfiguration {
	private static final String STUB_COLON_DELIMITER = ":"
	private static final String DEFAULT_VERSION = "+"
	private static final String DEFAULT_CLASSIFIER = "stubs"

	final String groupId
	final String artifactId
	final String version
	final String classifier

	public StubConfiguration(String groupId, String artifactId, String version) {
		this.groupId = groupId
		this.artifactId = artifactId
		this.version = version
		this.classifier = DEFAULT_CLASSIFIER
	}

	public StubConfiguration(String groupId, String artifactId, String version, String classifier) {
		this.groupId = groupId
		this.artifactId = artifactId
		this.version = version
		this.classifier = classifier
	}

	public StubConfiguration(String stubPath, String defaultClassifier) {
		String[] parsedPath = parsedPathEmptyByDefault(stubPath, STUB_COLON_DELIMITER, defaultClassifier)
		this.groupId = parsedPath[0]
		this.artifactId = parsedPath[1]
		this.version = parsedPath[2]
		this.classifier = parsedPath[3]
	}

	public StubConfiguration(String stubPath) {
		String[] parsedPath = parsedPathEmptyByDefault(stubPath, STUB_COLON_DELIMITER, DEFAULT_CLASSIFIER)
		this.groupId = parsedPath[0]
		this.artifactId = parsedPath[1]
		this.version = parsedPath[2]
		this.classifier = parsedPath[3]
	}

	private List<String> parsedPathEmptyByDefault(String path, String delimiter, String defaultClassifier) {
		String[] splitPath = path.split(delimiter)
		String stubsGroupId = ""
		String stubsArtifactId = ""
		String stubsVersion = ""
		String stubsClassifier = ""
		if (splitPath.length >= 2) {
			stubsGroupId = splitPath[0]
			stubsArtifactId = splitPath[1]
			stubsVersion = splitPath.length >= 3 ? splitPath[2] : DEFAULT_VERSION
			stubsClassifier = splitPath.length == 4 ? splitPath[3] : defaultClassifier
		}
		return [stubsGroupId, stubsArtifactId, stubsVersion, stubsClassifier]
	}

	private boolean isDefined() {
		return StringUtils.hasText(groupId) && StringUtils.hasText(this.artifactId)
	}

	String toColonSeparatedDependencyNotation() {
		if(!isDefined()) {
			return ""
		}
		return [groupId, artifactId, version, classifier].join(STUB_COLON_DELIMITER)
	}

	@CompileDynamic
	boolean groupIdAndArtifactMatches(String ivyNotationAsString) {
		def (String groupId, String artifactId) = ivyNotationFrom(ivyNotationAsString)
		if (!groupId) {
			return this.artifactId == artifactId
		}
		return this.groupId == groupId && this.artifactId == artifactId
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		StubConfiguration that = (StubConfiguration) o

		if (artifactId != that.artifactId) return false
		if (groupId != that.groupId) return false

		return true
	}

	int hashCode() {
		int result
		result = (groupId != null ? groupId.hashCode() : 0)
		result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0)
		return result
	}

	boolean matchesIvyNotation(String ivyNotationAsString) {
		def strings = ivyNotationAsString.split(':')
		if (strings.length == 1) {
			return artifactId == ivyNotationAsString
		} else if(strings.length == 2) {
			return groupId == strings[0] &&
					artifactId == strings[1]
		} else if(strings.length == 3) {
			return groupId == strings[0] &&
					artifactId == strings[1] &&
					(strings[2] == DEFAULT_VERSION || version == strings[2])
		}
		return groupId == strings[0] &&
				artifactId == strings[1] &&
				(strings[2] == DEFAULT_VERSION || version == strings[2]) &&
				classifier == strings[3]
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