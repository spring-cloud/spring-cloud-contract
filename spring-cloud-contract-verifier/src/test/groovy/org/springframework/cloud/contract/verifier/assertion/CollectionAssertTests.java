package org.springframework.cloud.contract.verifier.assertion;

import java.util.ArrayList;
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
			SpringCloudContractAssertions.assertThat(collection).allElementsMatch("[0-9]");
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("The value <a> doesn't match the regex <[0-9]>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).allElementsMatch("foo");
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_empty() {
		Collection collection = new ArrayList();

		try {
			SpringCloudContractAssertions.assertThat(collection).allElementsMatch("foo");
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("Expecting actual not to be empty");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_flattened_size_is_greater_than_or_equal_to_provided_size() {
		Collection collection = nestedCollection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeGreaterThanOrEqualTo(0)
				.hasFlattenedSizeGreaterThanOrEqualTo(4);
	}

	@Test
	public void should_throw_an_exception_when_flattened_size_is_not_greater_than_or_equal_to_provided_size() {
		Collection collection = nestedCollection();

		try {
			SpringCloudContractAssertions.assertThat(collection).hasFlattenedSizeGreaterThanOrEqualTo(5);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("The flattened size <4> is not greater or equal to <5>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_flattened_greater_than_or_equal() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).hasFlattenedSizeGreaterThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_flattened_size_is_less_than_or_equal_to_provided_size() {
		Collection collection = nestedCollection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeLessThanOrEqualTo(5)
				.hasFlattenedSizeLessThanOrEqualTo(4);
	}

	@Test
	public void should_throw_an_exception_when_flattened_size_is_not_less_than_or_equal_to_provided_size() {
		Collection collection = nestedCollection();

		try {
			SpringCloudContractAssertions.assertThat(collection).hasFlattenedSizeLessThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("The flattened size <4> is not less or equal to <1>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_flattened_less_than_or_equal() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).hasFlattenedSizeLessThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("Expecting actual not to be null");
		}
	}


	@Test
	public void should_not_throw_an_exception_when_flattened_size_is_between_the_provided_sizes() {
		Collection collection = nestedCollection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeBetween(1, 5)
				.hasFlattenedSizeBetween(4, 4);
	}

	@Test
	public void should_throw_an_exception_when_flattened_size_is_not_between_the_provided_sizes() {
		Collection collection = nestedCollection();

		try {
			SpringCloudContractAssertions.assertThat(collection).hasFlattenedSizeBetween(5, 7);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("The flattened size <4> is not between <5> and <7>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_flattened_between() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).hasFlattenedSizeBetween(1, 2);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_size_is_greater_than_or_equal_to_provided_size() {
		Collection collection = collection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeGreaterThanOrEqualTo(0)
				.hasSizeGreaterThanOrEqualTo(3);
	}

	@Test
	public void should_throw_an_exception_when_size_is_not_greater_than_or_equal_to_provided_size() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection).hasSizeGreaterThanOrEqualTo(5);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("The size <3> is not greater or equal to <5>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_greater_than_or_equal() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).hasSizeGreaterThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("Expecting actual not to be null");
		}
	}

	@Test
	public void should_not_throw_an_exception_when_size_is_less_than_or_equal_to_provided_size() {
		Collection collection = collection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeLessThanOrEqualTo(4)
				.hasSizeLessThanOrEqualTo(3);
	}

	@Test
	public void should_throw_an_exception_when_size_is_not_less_than_or_equal_to_provided_size() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection).hasSizeLessThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("The size <3> is not less or equal to <1>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_less_than_or_equal() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).hasSizeLessThanOrEqualTo(1);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("Expecting actual not to be null");
		}
	}


	@Test
	public void should_not_throw_an_exception_when_size_is_between_the_provided_sizes() {
		Collection collection = collection();

		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeBetween(1, 4)
				.hasSizeBetween(3, 3);
	}

	@Test
	public void should_throw_an_exception_when_size_is_not_between_the_provided_sizes() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection).hasSizeBetween(5, 7);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("The size <3> is not between <5> and <7>");
		}
	}

	@Test
	public void should_not_break_compilation_when_using_as() {
		Collection collection = collection();

		try {
			SpringCloudContractAssertions.assertThat(collection).as("for jsonpath x.y.z").hasSizeBetween(5, 7);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("[for jsonpath x.y.z] The size <3> is not between <5> and <7>");
		}
	}

	@Test
	public void should_throw_an_exception_when_collection_is_null_for_between() {
		Collection collection = null;

		try {
			SpringCloudContractAssertions.assertThat(collection).hasSizeBetween(1, 2);
			Assertions.fail("should throw exception");
		} catch (AssertionError e) {
			Assertions.assertThat(e).hasMessageContaining("Expecting actual not to be null");
		}
	}

	private Collection<String> collection() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		return list;
	}

	private Collection nestedCollection() {
		List list = new ArrayList<>();
		List list1 = new ArrayList<>();
		Map<String, String> map1 = new HashMap<>();
		map1.put("a", "1");
		map1.put("b", "2");
		map1.put("c", "3");
		List list2 = new ArrayList<>();
		Map<String, String> map2 = new HashMap<>();
		map2.put("d", "4");
		list.add(list1);
		list.add(list2);
		list1.add(map1);
		list2.add(map2);
		return list;
	}

}