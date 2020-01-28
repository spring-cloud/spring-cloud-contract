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

package org.springframework.cloud.contract.stubrunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * Object representing a root project's version. Knows how to provide a minor bumped
 * version;
 *
 * @author Marcin Grzejszczak
 */
class ProjectVersion implements Comparable<ProjectVersion>, Serializable {

	private static final Pattern SNAPSHOT_PATTERN = Pattern
			.compile("^.*[\\.|\\-](BUILD-)?SNAPSHOT.*$");

	private static final String MILESTONE_REGEX = ".*[\\.|\\-]M[0-9]+";

	private static final String RC_REGEX = "^.*[\\.|\\-]RC.*$";

	private static final String RELEASE_REGEX = "^.*[\\.|\\-]RELEASE.*$";

	private static final String SR_REGEX = "^.*[\\.|\\-]SR[0-9]+.*$";

	private static final List<Pattern> VALID_PATTERNS = Arrays.asList(SNAPSHOT_PATTERN,
			Pattern.compile(MILESTONE_REGEX), Pattern.compile(RC_REGEX),
			Pattern.compile(RELEASE_REGEX), Pattern.compile(SR_REGEX));

	/**
	 * Version of the project.
	 */
	final String version;

	private final ReleaseType releaseType;

	ProjectVersion(String version) {
		this.version = version;
		this.releaseType = toReleaseType();
	}

	private SplitVersion assertVersion() {
		if (this.version == null) {
			throw new IllegalStateException("Version can't be null!");
		}
		SplitVersion splitByHyphen = tryHyphenSeparatedVersion();
		if (splitByHyphen != null) {
			return splitByHyphen;
		}
		return dotSeparatedReleaseTrainsAndVersions();
	}

	private SplitVersion tryHyphenSeparatedVersion() {
		// Check for hyphen separated BOMs versioning
		// Dysprosium-BUILD-SNAPSHOT or Dysprosium-RELEASE
		// 1.0.0-BUILD-SNAPSHOT or 1.0.0-RELEASE
		String[] splitByHyphen = this.version.split("\\-");
		int splitByHyphens = splitByHyphen.length;
		int numberOfHyphens = splitByHyphens - 1;
		int indexOfFirstHyphen = this.version.indexOf("-");
		boolean buildSnapshot = this.version.endsWith("BUILD-SNAPSHOT");
		if (numberOfHyphens == 1 && !buildSnapshot
				|| (numberOfHyphens > 1 && buildSnapshot)) {
			// Dysprosium or 1.0.0
			String versionName = this.version.substring(0, indexOfFirstHyphen);
			boolean hasDots = versionName.contains(".");
			// BUILD-SNAPSHOT
			String versionType = this.version.substring(indexOfFirstHyphen + 1);
			// Dysprosium-BUILD-SNAPSHOT
			if (splitByHyphens > 1 && !hasDots && validVersionType()) {
				return SplitVersion.hyphen(versionName, versionType);
			}
			// Dysprosium-RELEASE
			else if (splitByHyphens == 1 && !hasDots && validVersionType()) {
				return SplitVersion.hyphen(splitByHyphen[0], splitByHyphen[1]);
			}
			// 1.0.0-RELEASE or 1.0.0-BUILD-SNAPSHOT
			else if (splitByHyphens >= 1 && hasDots) {
				String[] newArray = combinedArrays(versionName, versionType);
				return SplitVersion.hyphen(newArray);
			}
			else {
				throw new UnsupportedOperationException(
						"Unknown version [" + this.version + "]");
			}
		}
		return null;
	}

	private boolean validVersionType() {
		return VALID_PATTERNS.stream().anyMatch(p -> p.matcher(this.version).matches());
	}

	private String[] combinedArrays(String versionName, String versionType) {
		String[] split = versionName.split("\\.");
		String[] newArray = new String[split.length + 1];
		for (int i = 0; i < split.length; i++) {
			newArray[i] = split[i];
		}
		newArray[split.length] = versionType;
		return newArray;
	}

	private SplitVersion dotSeparatedReleaseTrainsAndVersions() {
		// Hoxton.BUILD-SNAPSHOT or 1.0.0.BUILD-SNAPSHOT
		String[] splitVersion = this.version.split("\\.");
		return SplitVersion.dot(splitVersion);
	}

	boolean isSnapshot() {
		return this.version != null && this.version.contains("SNAPSHOT");
	}

	boolean isRc() {
		return this.version != null && this.version.matches(RC_REGEX);
	}

	boolean isMilestone() {
		return this.version != null && this.version.matches(MILESTONE_REGEX);
	}

	boolean isRelease() {
		return this.version != null && this.version.contains("RELEASE");
	}

	boolean isReleaseOrServiceRelease() {
		return isRelease() || isServiceRelease();
	}

	boolean isServiceRelease() {
		return this.version != null && this.version.matches(".*.SR[0-9]+");
	}

	private ReleaseType toReleaseType() {
		if (isMilestone()) {
			return ReleaseType.M;
		}
		else if (isRc()) {
			return ReleaseType.RC;
		}
		else if (isRelease()) {
			return ReleaseType.RELEASE;
		}
		else if (isServiceRelease()) {
			return ReleaseType.SR;
		}
		return ReleaseType.SNAPSHOT;
	}

	/*
	 * E.g. 1.0.1.RELEASE is more mature than 1.0.2.RC2
	 */
	boolean isMoreMature(ProjectVersion that) {
		SplitVersion thisSplit = assertVersion();
		SplitVersion thatSplit = that.assertVersion();
		int releaseTypeComparison = this.releaseType.compareTo(that.releaseType);
		boolean thisReleaseTypeHigher = releaseTypeComparison > 0;
		boolean bothGa = this.isReleaseOrServiceRelease()
				&& that.isReleaseOrServiceRelease();
		// 1.0.1.M2 vs 1.0.0.RELEASE (x)
		if (thisReleaseTypeHigher && !bothGa) {
			return true;
		}
		int versionComparison = thisSplit.gav().compareTo(thatSplit.gav());
		if (versionComparison == 0) {
			// 1.0.0.SR1 vs 1.0.1.RELEASE (x)
			// Finchley.RELEASE vs Finchley.SR1 (x)
			return thisReleaseTypeHigher;
		}
		// 1.0.0.SR1 vs 1.0.1.RELEASE (x)
		return versionComparison > 0;
	}

	boolean isSameWithoutSuffix(ProjectVersion that) {
		SplitVersion thisSplit = assertVersion();
		SplitVersion thatSplit = that.assertVersion();
		return thisSplit.major.equals(thatSplit.major)
				&& thisSplit.minor.equals(thatSplit.minor)
				&& thisSplit.patch.equals(thatSplit.patch);
	}

	@Override
	public String toString() {
		return this.version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProjectVersion that = (ProjectVersion) o;
		return Objects.equals(this.version, that.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.version);
	}

	@Override
	public int compareTo(ProjectVersion o) {
		// very simple comparison
		return this.version.compareTo(o.version);
	}

	private static final class SplitVersion {

		private static final String DOT = ".";

		private static final String HYPHEN = "-";

		final String major;

		final String minor;

		final String patch;

		final String delimiter;

		final String suffix;

		// 1.0.0.RELEASE
		// 1.0.0-RELEASE
		private SplitVersion(String major, String minor, String patch, String delimiter,
				String suffix) {
			this.major = major;
			this.minor = minor;
			this.patch = patch;
			this.delimiter = delimiter;
			this.suffix = suffix;
			assertIfValid();
		}

		// Hoxton.RELEASE
		// Hoxton-RELEASE
		private SplitVersion(String major, String delimiter, String suffix) {
			this(major, "", "", delimiter, suffix);
		}

		private SplitVersion(String[] args, String delimiter) {
			this.major = orDefault(args, 0);
			this.minor = orDefault(args, 1);
			this.patch = orDefault(args, 2);
			this.delimiter = delimiter;
			this.suffix = orDefault(args, 3);
			assertIfValid();
		}

		private static String orDefault(String[] args, int argIndex) {
			return args.length > argIndex ? args[argIndex] : "";
		}

		static SplitVersion hyphen(String major, String suffix) {
			return new SplitVersion(major, HYPHEN, suffix);
		}

		static SplitVersion hyphen(String[] args) {
			return version(args, HYPHEN);
		}

		static SplitVersion dot(String[] args) {
			return version(args, DOT);
		}

		private static SplitVersion version(String[] args, String delimiter) {
			if (args.length == 2) {
				return new SplitVersion(args[0], "", "", delimiter, args[1]);
			}
			else if (args.length == 3) {
				return new SplitVersion(args[0], args[1], "", delimiter, args[2]);
			}
			return new SplitVersion(args, delimiter);
		}

		private void assertIfValid() {
			if (isInvalid()) {
				throw new IllegalStateException(
						"Version is invalid. Should be of format [1.2.3.A] / [1.2.3-A] or [A.B] / [A-B]");
			}
		}

		private boolean isInvalid() {
			return wrongReleaseTrainVersion() || wrongLibraryVersion() || wrongDelimiter()
					|| noSuffix();
		}

		private boolean noSuffix() {
			return StringUtils.isEmpty(suffix);
		}

		private String gav() {
			// Finchley
			if (StringUtils.isEmpty(minor)) {
				return String.format("%s", major);
			}
			// 1.0.1
			return String.format("%s.%s.%s", major, minor, patch);
		}

		private boolean isNumeric(String string) {
			return string.matches("[0-9]+");
		}

		private boolean wrongDelimiter() {
			return !(DOT.equals(this.delimiter) || HYPHEN.equals(this.delimiter));
		}

		private boolean wrongLibraryVersion() {
			// GOOD:
			// 1.2.3.RELEASE, 1.2.3-RELEASE, Hoxton.BUILD-SNAPSHOT, Hoxton-RELEASE
			// must have
			// either major and suffix (release train)
			// major, minor, patch and suffix
			return isNumeric(major) && (StringUtils.isEmpty(minor)
					|| StringUtils.isEmpty(patch) || StringUtils.isEmpty(suffix)
					|| StringUtils.isEmpty(delimiter));
		}

		private boolean wrongReleaseTrainVersion() {
			// BAD: 1.EXAMPLE, GOOD: Hoxton.RELEASE
			return isNumeric(major) && StringUtils.isEmpty(suffix);
		}

	}

}

enum ReleaseType {

	SNAPSHOT, M, RC, RELEASE, SR

}
