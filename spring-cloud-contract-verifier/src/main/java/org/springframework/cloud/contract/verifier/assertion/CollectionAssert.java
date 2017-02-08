package org.springframework.cloud.contract.verifier.assertion;

import java.util.Iterator;

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
}
