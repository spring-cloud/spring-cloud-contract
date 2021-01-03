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

package org.springframework.cloud.contract.verifier.assertion;

import java.util.Iterator;
import java.util.Map;

import org.assertj.core.api.IterableAssert;
import org.assertj.core.util.Streams;

import static java.util.stream.Collectors.toList;

/**
 * Extension to {@link Iterable} assertions.
 *
 * @param <ELEMENT> type to assert
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class CollectionAssert<ELEMENT> extends IterableAssert<ELEMENT> {

	public CollectionAssert(Iterable<? extends ELEMENT> actual) {
		super(actual);
	}

	public CollectionAssert(Iterator<? extends ELEMENT> actual) {
		super(toIterable(actual));
	}

	private static <T> Iterable<T> toIterable(Iterator<T> iterator) {
		return Streams.stream(iterator).collect(toList());
	}

	/**
	 * Asserts all elements of the collection whether they match a regular expression.
	 * @param regex - regular expression to check against
	 * @return this
	 */
	public CollectionAssert allElementsMatch(String regex) {
		isNotNull();
		isNotEmpty();
		for (Object anActual : this.actual) {
			if (anActual == null) {
				failWithMessageRelatedToRegex(regex, anActual);
			}
			String value = anActual.toString();
			if (!value.matches(regex)) {
				failWithMessageRelatedToRegex(regex, value);
			}
		}
		return this;
	}

	private void someFakeMethodAddedToCauseAChange() {
		System.out.println("***** here");
	}

	private void failWithMessageRelatedToRegex(String regex, Object value) {
		failWithMessage("The value <%s> doesn't match the regex <%s>", value, regex);
	}

	/**
	 * Flattens the collection and checks whether size is greater than or equal to the
	 * provided value.
	 * @param size - the flattened collection should have size greater than or equal to
	 * this value
	 * @return this
	 */
	public CollectionAssert hasFlattenedSizeGreaterThanOrEqualTo(int size) {
		isNotNull();
		int flattenedSize = flattenedSize(0, this.actual);
		if (!(flattenedSize >= size)) {
			failWithMessage("The flattened size <%s> is not greater or equal to <%s>",
					flattenedSize, size);
		}
		return this;
	}

	/**
	 * Flattens the collection and checks whether size is less than or equal to the
	 * provided value.
	 * @param size - the flattened collection should have size less than or equal to this
	 * value
	 * @return this
	 */
	public CollectionAssert hasFlattenedSizeLessThanOrEqualTo(int size) {
		isNotNull();
		int flattenedSize = flattenedSize(0, this.actual);
		if (!(flattenedSize <= size)) {
			failWithMessage("The flattened size <%s> is not less or equal to <%s>",
					flattenedSize, size);
		}
		return this;
	}

	/**
	 * Flattens the collection and checks whether size is between the provided value.
	 * @param lowerBound - the flattened collection should have size greater than or equal
	 * to this value
	 * @param higherBound - the flattened collection should have size less than or equal
	 * to this value
	 * @return this
	 */
	public CollectionAssert hasFlattenedSizeBetween(int lowerBound, int higherBound) {
		isNotNull();
		int flattenedSize = flattenedSize(0, this.actual);
		if (!(flattenedSize >= lowerBound && flattenedSize <= higherBound)) {
			failWithMessage("The flattened size <%s> is not between <%s> and <%s>",
					flattenedSize, lowerBound, higherBound);
		}
		return this;
	}

	/**
	 * Checks whether size is greater than or equal to the provided value.
	 * @param size - the collection should have size greater than or equal to this value
	 * @return this
	 */
	public CollectionAssert hasSizeGreaterThanOrEqualTo(int size) {
		isNotNull();
		int actualSize = size(this.actual);
		if (!(actualSize >= size)) {
			failWithMessage("The size <%s> is not greater or equal to <%s>", actualSize,
					size);
		}
		return this;
	}

	/**
	 * Checks whether size is less than or equal to the provided value.
	 * @param size - the collection should have size less than or equal to this value
	 * @return this
	 */
	public CollectionAssert hasSizeLessThanOrEqualTo(int size) {
		isNotNull();
		int actualSize = size(this.actual);
		if (!(actualSize <= size)) {
			failWithMessage("The size <%s> is not less or equal to <%s>", actualSize,
					size);
		}
		return this;
	}

	/**
	 * Checks whether size is between the provided value.
	 * @param lowerBound - the collection should have size greater than or equal to this
	 * value
	 * @param higherBound - the collection should have size less than or equal to this
	 * value
	 * @return this
	 */
	public CollectionAssert hasSizeBetween(int lowerBound, int higherBound) {
		isNotNull();
		int size = size(this.actual);
		if (!(size >= lowerBound && size <= higherBound)) {
			failWithMessage("The size <%s> is not between <%s> and <%s>", size,
					lowerBound, higherBound);
		}
		return this;
	}

	@Override
	public CollectionAssert<ELEMENT> as(String description, Object... args) {
		return (CollectionAssert<ELEMENT>) super.as(description, args);
	}

	private int flattenedSize(int counter, Object object) {
		if (object instanceof Map) {
			return counter + ((Map) object).size();
		}
		if (object instanceof Iterator) {
			Iterator iterator = ((Iterator) object);
			while (iterator.hasNext()) {
				Object next = iterator.next();
				counter = flattenedSize(counter, next);
			}
			return counter;
		}
		if (object instanceof Iterable) {
			return flattenedSize(counter, ((Iterable) object).iterator());
		}
		return ++counter;
	}

	private int size(Iterable iterable) {
		int size = 0;
		for (Object value : iterable) {
			size++;
		}
		return size;
	}

}
