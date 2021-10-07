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

import java.util.Collection;

/**
 * Extension to {@link Iterable} assertions.
 *
 * @param <ELEMENT> type to assert
 * @author Marcin Grzejszczak
 * @since 3.1.0
 */
public class ContractCollectionAssert<ELEMENT> extends org.assertj.core.api.CollectionAssert<ELEMENT> {

	private final CollectionAssert<ELEMENT> collectionAssert;
	
	public ContractCollectionAssert(Collection<? extends ELEMENT> actual) {
		super(actual);
		this.collectionAssert = new CollectionAssert<>(actual);
	}

	/**
	 * Asserts all elements of the collection whether they match a regular expression.
	 * @param regex - regular expression to check against
	 * @return this
	 */
	public ContractCollectionAssert<ELEMENT> allElementsMatch(String regex) {
		this.collectionAssert.allElementsMatch(regex);
		return this;
	}

	/**
	 * Flattens the collection and checks whether size is greater than or equal to the
	 * provided value.
	 * @param size - the flattened collection should have size greater than or equal to
	 * this value
	 * @return this
	 */
	public ContractCollectionAssert<ELEMENT> hasFlattenedSizeGreaterThanOrEqualTo(int size) {
		this.collectionAssert.hasFlattenedSizeGreaterThanOrEqualTo(size);
		return this;
	}

	/**
	 * Flattens the collection and checks whether size is less than or equal to the
	 * provided value.
	 * @param size - the flattened collection should have size less than or equal to this
	 * value
	 * @return this
	 */
	public ContractCollectionAssert<ELEMENT> hasFlattenedSizeLessThanOrEqualTo(int size) {
		this.collectionAssert.hasFlattenedSizeLessThanOrEqualTo(size);
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
	public ContractCollectionAssert<ELEMENT> hasFlattenedSizeBetween(int lowerBound, int higherBound) {
		this.collectionAssert.hasFlattenedSizeBetween(lowerBound, higherBound);
		return this;
	}

	/**
	 * Checks whether size is greater than or equal to the provided value.
	 * @param size - the collection should have size greater than or equal to this value
	 * @return this
	 */
	public ContractCollectionAssert<ELEMENT> hasSizeGreaterThanOrEqualTo(int size) {
		this.collectionAssert.hasSizeGreaterThanOrEqualTo(size);
		return this;
	}

	/**
	 * Checks whether size is less than or equal to the provided value.
	 * @param size - the collection should have size less than or equal to this value
	 * @return this
	 */
	public ContractCollectionAssert<ELEMENT> hasSizeLessThanOrEqualTo(int size) {
		this.collectionAssert.hasSizeLessThanOrEqualTo(size);
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
	public ContractCollectionAssert<ELEMENT> hasSizeBetween(int lowerBound, int higherBound) {
		this.collectionAssert.hasSizeBetween(lowerBound, higherBound);
		return this;
	}

	@Override
	public ContractCollectionAssert<ELEMENT> as(String description, Object... args) {
		return (ContractCollectionAssert<ELEMENT>) super.as(description, args);
	}

}
