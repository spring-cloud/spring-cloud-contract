package org.springframework.cloud.contract.verifier.assertion;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.assertj.core.api.IterableAssert;

/**
 * Extension to {@link Iterable} assertions
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class CollectionAssert<ELEMENT> extends IterableAssert<ELEMENT> {
	public CollectionAssert(Iterable<? extends ELEMENT> actual) {
		super(actual);
	}

	public CollectionAssert(Iterator<? extends ELEMENT> actual) {
		super(actual);
	}

	/**
	 * Asserts all elements of the collection whether they match a regular expression
	 * @param regex - regular expression to check against
	 * @return this
	 */
	public CollectionAssert allElementsMatch(String regex) {
		isNotNull();
		isNotEmpty();
		for (Object anActual : this.actual) {
			String value = anActual.toString();
			if (!value.matches(regex)) {
				failWithMessage("The value <%s> doesn't match the regex <%s>", value, regex);
			}
		}
		return this;
	}

	/**
	 * Flattens the collection and checks whether size is greater than or equal to the provided value
	 * @param size - the flattened collection should have size greater than or equal to this value
	 * @return this
	 */
	public CollectionAssert hasFlattenedSizeGreaterThanOrEqualTo(int size) {
		isNotNull();
		int flattenedSize = flattenedSize(0, this.actual);
		if (!(flattenedSize >= size)) {
			failWithMessage("The flattened size <%s> is not greater or equal to <%s>", flattenedSize, size);
		}
		return this;
	}

	/**
	 * Flattens the collection and checks whether size is less than or equal to the provided value
	 * @param size - the flattened collection should have size less than or equal to this value
	 * @return this
	 */
	public CollectionAssert hasFlattenedSizeLessThanOrEqualTo(int size) {
		isNotNull();
		int flattenedSize = flattenedSize(0, this.actual);
		if (!(flattenedSize <= size)) {
			failWithMessage("The flattened size <%s> is not less or equal to <%s>", flattenedSize, size);
		}
		return this;
	}

	/**
	 * Flattens the collection and checks whether size is between the provided value
	 * @param lowerBound - the flattened collection should have size greater than or equal to this value
	 * @param higherBound - the flattened collection should have size less than or equal to this value
	 * @return this
	 */
	public CollectionAssert hasFlattenedSizeBetween(int lowerBound, int higherBound) {
		isNotNull();
		int flattenedSize = flattenedSize(0, this.actual);
		if (!(flattenedSize >= lowerBound && flattenedSize <= higherBound)) {
			failWithMessage("The flattened size <%s> is not between <%s> and <%s>", flattenedSize, lowerBound, higherBound);
		}
		return this;
	}

	/**
	 * Checks whether size is greater than or equal to the provided value
	 * @param size - the collection should have size greater than or equal to this value
	 * @return this
	 */
	public CollectionAssert hasSizeGreaterThanOrEqualTo(int size) {
		isNotNull();
		int actualSize = size(this.actual);
		if (!(actualSize >= size)) {
			failWithMessage("The size <%s> is not greater or equal to <%s>", actualSize, size);
		}
		return this;
	}

	/**
	 * Checks whether size is less than or equal to the provided value
	 * @param size - the collection should have size less than or equal to this value
	 * @return this
	 */
	public CollectionAssert hasSizeLessThanOrEqualTo(int size) {
		isNotNull();
		int actualSize = size(this.actual);
		if (!(actualSize <= size)) {
			failWithMessage("The size <%s> is not less or equal to <%s>", actualSize, size);
		}
		return this;
	}

	/**
	 * Checks whether size is between the provided value
	 * @param lowerBound - the collection should have size greater than or equal to this value
	 * @param higherBound - the collection should have size less than or equal to this value
	 * @return this
	 */
	public CollectionAssert hasSizeBetween(int lowerBound, int higherBound) {
		isNotNull();
		int size = size(this.actual);
		if (!(size >= lowerBound && size <= higherBound)) {
			failWithMessage("The size <%s> is not between <%s> and <%s>", size, lowerBound, higherBound);
		}
		return this;
	}

	private int flattenedSize(int counter, Object object) {
		if (object instanceof Map) {
			return counter + ((Map) object).size();
		} else if (object instanceof Iterator) {
			Iterator iterator = ((Iterator) object);
			while (iterator.hasNext()) {
				Object next = iterator.next();
				counter = flattenedSize(counter, next);
			}
			return counter;
		} else if (object instanceof Collection) {
			return flattenedSize(counter, ((Collection) object).iterator());
		}
		return counter;
	}

	private int size(Iterable iterable) {
		int size = 0;
		for (Object value : iterable) {
			size++;
		}
		return size;
	}
}
