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

import java.util.Objects;

/**
 * @author Marcin Grzejszczak
 */
public class PathBodyMatcher implements BodyMatcher {

	private String path;

	private MatchingTypeValue matchingTypeValue;

	PathBodyMatcher(String path, MatchingTypeValue matchingTypeValue) {
		this.path = path;
		this.matchingTypeValue = matchingTypeValue;
	}

	@Override
	public MatchingType matchingType() {
		return this.matchingTypeValue.getType();
	}

	@Override
	public String path() {
		return this.path;
	}

	@Override
	public Object value() {
		return this.matchingTypeValue.getValue();
	}

	@Override
	public Integer minTypeOccurrence() {
		return this.matchingTypeValue.getMinTypeOccurrence();
	}

	@Override
	public Integer maxTypeOccurrence() {
		return this.matchingTypeValue.getMaxTypeOccurrence();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PathBodyMatcher that = (PathBodyMatcher) o;
		return Objects.equals(path, that.path)
				&& Objects.equals(matchingTypeValue, that.matchingTypeValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, matchingTypeValue);
	}

	@Override
	public String toString() {
		return "PathBodyMatcher{" + "path='" + path + '\'' + ", matchingTypeValue="
				+ matchingTypeValue + '}';
	}

}
