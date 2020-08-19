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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author Marcin Grzejszczak
 */
public class CollectionAssertTests {

	@Test
	public void should_not_throw_an_exception_when_all_elements_match_regex() {
		Collection collection = collection();

		SpringCloudContractAssertions.assertThat(collection).allElementsMatch("[a-z]");
	}

	@Test
	public void should_throw_an_exception_when_at_least_one_element_doesnt_match_regex() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.allElementsMatch("[0-9]");
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining(
					"The value <a> doesn't match the regex <[0-9]>");
		}
	}

	@Test
	public void should_throw_an_exception_when_element_is_null() {
		Collection collection = collectionWithNulls();

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.allElementsMatch("[0-9]");
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining(
					"The value <null> doesn't match the regex <[0-9]>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).allElementsMatch("foo");
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_empty() {
		Collection collection = new ArrayList();

		try {
			SpringCloudContractAssertions.assertThat(collection).allElementsMatch("foo");
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be empty");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_flattened_size_is_greater_than_or_equal_to_provided_size() {
		Collection collection = nestedCollection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeGreaterThanOrEqualTo(0)
				.hasFlattenedSizeGreaterThanOrEqualTo(7);
	}

	@Test
	public void should_throw_an_exception_when_flattened_size_is_not_greater_than_or_equal_to_provided_size() {
		Collection collection = nestedCollection();

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasFlattenedSizeGreaterThanOrEqualTo(8);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining(
					"The flattened size <7> is not greater or equal to <8>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_flattened_greater_than_or_equal() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasFlattenedSizeGreaterThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_flattened_size_is_less_than_or_equal_to_provided_size() {
		Collection collection = nestedCollection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeLessThanOrEqualTo(8)
				.hasFlattenedSizeLessThanOrEqualTo(7);
	}

	@Test
	public void should_throw_an_exception_when_flattened_size_is_not_less_than_or_equal_to_provided_size() {
		Collection collection = nestedCollection();

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasFlattenedSizeLessThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining(
					"The flattened size <7> is not less or equal to <1>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_flattened_less_than_or_equal() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasFlattenedSizeLessThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_flattened_size_is_between_the_provided_sizes() {
		Collection collection = nestedCollection();

		SpringCloudContractAssertions.assertThat(collection).hasFlattenedSizeBetween(1, 8)
				.hasFlattenedSizeBetween(7, 7);
	}

	@Test
	public void should_throw_an_exception_when_flattened_size_is_not_between_the_provided_sizes() {
		Collection collection = nestedCollection();

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasFlattenedSizeBetween(8, 9);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining(
					"The flattened size <7> is not between <8> and <9>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_flattened_between() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasFlattenedSizeBetween(1, 2);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_size_is_greater_than_or_equal_to_provided_size() {
		Collection collection = collection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeGreaterThanOrEqualTo(0).hasSizeGreaterThanOrEqualTo(3);
	}

	@Test
	public void should_throw_an_exception_when_size_is_not_greater_than_or_equal_to_provided_size() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasSizeGreaterThanOrEqualTo(5);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("The size <3> is not greater or equal to <5>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_greater_than_or_equal() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasSizeGreaterThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_size_is_less_than_or_equal_to_provided_size() {
		Collection collection = collection();

		SpringCloudContractAssertions.assertThat(collection).hasSizeLessThanOrEqualTo(4)
				.hasSizeLessThanOrEqualTo(3);
	}

	@Test
	public void should_throw_an_exception_when_size_is_not_less_than_or_equal_to_provided_size() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasSizeLessThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("The size <3> is not less or equal to <1>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_less_than_or_equal() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection)
					.hasSizeLessThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_size_is_between_the_provided_sizes() {
		Collection collection = collection();

		SpringCloudContractAssertions.assertThat(collection).hasSizeBetween(1, 4)
				.hasSizeBetween(3, 3);
	}

	@Test
	public void should_throw_an_exception_when_size_is_not_between_the_provided_sizes() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection).hasSizeBetween(5, 7);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("The size <3> is not between <5> and <7>");
		}
	}

	@Test
	public void should_not_break_compilation_when_using_as() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection).as("for jsonpath x.y.z")
					.hasSizeBetween(5, 7);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining(
					"[for jsonpath x.y.z] The size <3> is not between <5> and <7>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_between() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).hasSizeBetween(1, 2);
			Assertions.fail("should throw exception");
		}
		catch (AssertionError e) {
			Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null");
		}
	}

	private Collection<String> collection() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		return list;
	}

	private Collection<String> collectionWithNulls() {
		List<String> list = new ArrayList<>();
		list.add(null);
		return list;
	}

	private Collection nestedCollection() {
		List list = new ArrayList();
		List list1 = new ArrayList();
		Map<String, String> map1 = new HashMap<>();
		map1.put("a", "1");
		map1.put("b", "2");
		map1.put("c", "3");
		List list2 = new ArrayList<>();
		Map<String, String> map2 = new HashMap<>();
		map2.put("d", "4");
		List list3 = new ArrayList();
		List innerList = Arrays.asList("A", "B", "C");
		list.add(list1);
		list.add(list2);
		list.add(list3);
		list1.add(map1);
		list2.add(map2);
		list3.add(innerList);
		return list;
	}

}
