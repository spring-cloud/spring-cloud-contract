/*
 * Copyright 2013-2019 the original author or authors.
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
import java.util.regex.Pattern;

/**
 * Matching strategy of dynamic parts of the body.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 1.0.3
 */
public class BodyMatchers {

	private final RegexPatterns regexPatterns = new RegexPatterns();

	protected final List<BodyMatcher> matchers = new LinkedList<>();

	public void jsonPath(String path, MatchingTypeValue matchingType) {
		this.matchers.add(new PathBodyMatcher(path, matchingType));
	}

	/**
	 * Adds xPath matcher; even though same implementation as in
	 * {@link BodyMatchers#jsonPath(java.lang.String, org.springframework.cloud.contract.spec.internal.MatchingTypeValue)},
	 * added for logical coherence in xml
	 * @param xPath the xPath used to find the element to match
	 * @param matchingTypeValue to match the element found by the xPath against
	 */
	public void xPath(String xPath, MatchingTypeValue matchingTypeValue) {
		matchers.add(new PathBodyMatcher(xPath, matchingTypeValue));
	}

	/**
	 * @deprecated use{@link #matchers()}
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
		return new MatchingTypeValue(MatchingType.DATE, this.regexPatterns.isoDate());
	}

	public MatchingTypeValue byTime() {
		return new MatchingTypeValue(MatchingType.TIME, this.regexPatterns.isoTime());
	}

	public MatchingTypeValue byTimestamp() {
		return new MatchingTypeValue(MatchingType.TIMESTAMP,
				this.regexPatterns.isoDateTime());
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

}

/**
 * Matching type with corresponding values
 */
class RegexMatchingTypeValue extends MatchingTypeValue {

	RegexMatchingTypeValue(MatchingType type, Object value,
			Integer minTypeOccurrence, Integer maxTypeOccurrence) {
		super(type, value, minTypeOccurrence, maxTypeOccurrence);
	}

	RegexMatchingTypeValue(MatchingType type, Object value) {
		super(type, value);
	}

	public RegexMatchingTypeValue asInteger() {
		return typed(Integer.class);
	}

	private RegexMatchingTypeValue typed(Class clazz) {
		if (!(this.getValue() instanceof RegexProperty)) {
			throw new IllegalStateException("Value has to be a regex");
		}
		RegexProperty regexProperty = (RegexProperty) this.getValue();
		return new RegexMatchingTypeValue(this.getType(),
				new RegexProperty(regexProperty.getClientValue(),
						regexProperty.getServerValue(), clazz),
				this.getMinTypeOccurrence(), this.getMaxTypeOccurrence());
	}

	public RegexMatchingTypeValue asDouble() {
		return typed(Double.class);
	}

	public RegexMatchingTypeValue asFloat() {
		return typed(Float.class);
	}

	public RegexMatchingTypeValue asLong() {
		return typed(Long.class);
	}

	public RegexMatchingTypeValue asShort() {
		return typed(Short.class);
	}

	public RegexMatchingTypeValue asString() {
		return typed(String.class);
	}

	public RegexMatchingTypeValue asBooleanType() {
		return typed(Boolean.class);
	}

}

/**
 * Matching type with corresponding values
 */
class MatchingTypeValue {

	private MatchingType type;

	/**
	 * Value to check
	 */
	private Object value;

	/**
	 * Min occurrence when matching by type
	 */
	private Integer minTypeOccurrence;

	/**
	 * Max occurrence when matching by type
	 */
	private Integer maxTypeOccurrence;

	MatchingTypeValue(MatchingType type) {
		this.type = type;
	}

	MatchingTypeValue(MatchingType type, Object value) {
		this.type = type;
		this.value = value;
	}

	MatchingTypeValue(MatchingType type, Object value, Integer minTypeOccurrence) {
		this.type = type;
		this.value = value;
		this.minTypeOccurrence = minTypeOccurrence;
	}

	MatchingTypeValue(MatchingType type, Object value, Integer minTypeOccurrence,
			Integer maxTypeOccurrence) {
		this.type = type;
		this.value = value;
		this.minTypeOccurrence = minTypeOccurrence;
		this.maxTypeOccurrence = maxTypeOccurrence;
	}

	public MatchingType getType() {
		return type;
	}

	public void setType(MatchingType type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Integer getMinTypeOccurrence() {
		return minTypeOccurrence;
	}

	public void setMinTypeOccurrence(Integer minTypeOccurrence) {
		this.minTypeOccurrence = minTypeOccurrence;
	}

	public Integer getMaxTypeOccurrence() {
		return maxTypeOccurrence;
	}

	public void setMaxTypeOccurrence(Integer maxTypeOccurrence) {
		this.maxTypeOccurrence = maxTypeOccurrence;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MatchingTypeValue value1 = (MatchingTypeValue) o;
		return type == value1.type && Objects.equals(value, value1.value)
				&& Objects.equals(minTypeOccurrence, value1.minTypeOccurrence)
				&& Objects.equals(maxTypeOccurrence, value1.maxTypeOccurrence);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, value, minTypeOccurrence, maxTypeOccurrence);
	}

	@Override
	public String toString() {
		return "MatchingTypeValue{" + "type=" + type + ", value=" + value
				+ ", minTypeOccurrence=" + minTypeOccurrence + ", maxTypeOccurrence="
				+ maxTypeOccurrence + '}';
	}

}

class MatchingTypeValueHolder {

	MatchingTypeValue matchingTypeValue = new MatchingTypeValue(MatchingType.TYPE);

	public MatchingTypeValue minOccurrence(int number) {
		this.matchingTypeValue.setMinTypeOccurrence(number);
		return this.matchingTypeValue;
	}

	public MatchingTypeValue maxOccurrence(int number) {
		this.matchingTypeValue.setMaxTypeOccurrence(number);
		return this.matchingTypeValue;
	}

	public MatchingTypeValue occurrence(int number) {
		this.matchingTypeValue.setMinTypeOccurrence(number);
		this.matchingTypeValue.setMaxTypeOccurrence(number);
		return this.matchingTypeValue;
	}

}
