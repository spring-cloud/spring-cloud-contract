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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * Matching strategy of dynamic parts of the body.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @author Tim Ysewyn
 * @since 1.0.3
 */
public class BodyMatchers {

	protected final List<BodyMatcher> matchers = new LinkedList<>();

	public void jsonPath(String path, MatchingTypeValue matchingType) {
		this.matchers.add(new PathBodyMatcher(path, matchingType));
	}

	/**
	 * Adds xPath matcher; even though same implementation as in
	 * {@link BodyMatchers#jsonPath(java.lang.String, org.springframework.cloud.contract.spec.internal.MatchingTypeValue)},
	 * added for logical coherence in xml.
	 * @param xPath the xPath used to find the element to match
	 * @param matchingTypeValue to match the element found by the xPath against
	 */
	public void xPath(String xPath, MatchingTypeValue matchingTypeValue) {
		this.matchers.add(new PathBodyMatcher(xPath, matchingTypeValue));
	}

	/**
	 * @deprecated use{@link #matchers()}
	 * @return json path matchers
	 */
	@Deprecated
	public List<BodyMatcher> jsonPathMatchers() {
		return matchers();
	}

	public boolean hasMatchers() {
		return !this.matchers.isEmpty();
	}

	public List<BodyMatcher> matchers() {
		return this.matchers;
	}

	public MatchingTypeValue byDate() {
		return new MatchingTypeValue(MatchingType.DATE, RegexPatterns.isoDate());
	}

	public MatchingTypeValue byTime() {
		return new MatchingTypeValue(MatchingType.TIME, RegexPatterns.isoTime());
	}

	public MatchingTypeValue byTimestamp() {
		return new MatchingTypeValue(MatchingType.TIMESTAMP, RegexPatterns.isoDateTime());
	}

	public RegexMatchingTypeValue byRegex(String regex) {
		return byRegex(Pattern.compile(regex));
	}

	public RegexMatchingTypeValue byRegex(RegexProperty regex) {
		assertNotNull(regex);
		return new RegexMatchingTypeValue(MatchingType.REGEX, regex);
	}

	public RegexMatchingTypeValue byRegex(Pattern regex) {
		assertNotNull(regex);
		return new RegexMatchingTypeValue(MatchingType.REGEX, new RegexProperty(regex));
	}

	public MatchingTypeValue byEquality() {
		return new MatchingTypeValue(MatchingType.EQUALITY);
	}

	private void assertNotNull(Object object) {
		if (object == null) {
			throw new IllegalStateException("Object can't be null!");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BodyMatchers that = (BodyMatchers) o;
		return Objects.equals(matchers, that.matchers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(matchers);
	}

	@Override
	public String toString() {
		return "BodyMatchers{" + "\nmatchers=" + matchers + '}';
	}

	/**
	 * The output part of the contract.
	 * @param consumer function to manipulate the output message
	 * @return matching type
	 */
	public MatchingTypeValue byType(Consumer<MatchingTypeValueHolder> consumer) {
		MatchingTypeValueHolder matchingTypeValue = new MatchingTypeValueHolder();
		consumer.accept(matchingTypeValue);
		return matchingTypeValue.matchingTypeValue;
	}

	/**
	 * The output part of the contract.
	 * @param consumer function to manipulate the output message
	 * @return matching type
	 */
	public MatchingTypeValue byType(
			@DelegatesTo(MatchingTypeValueHolder.class) Closure consumer) {
		MatchingTypeValueHolder matchingTypeValue = new MatchingTypeValueHolder();
		consumer.setDelegate(matchingTypeValue);
		consumer.call();
		return matchingTypeValue.matchingTypeValue;
	}

}
