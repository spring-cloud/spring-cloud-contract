package org.springframework.cloud.contract.verifier.assertion

import org.assertj.core.api.Assertions
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak, Artem Ptushkin
 */
class CollectionAssertSpec extends Specification {

	def should_not_throw_an_exception_when_all_elements_match_regex() {
		setup:
		Collection collection = collection()

		when:
		SpringCloudContractAssertions.assertThat(collection).allElementsMatch("[a-z]")

		then:
		noExceptionThrown()
	}

	def should_throw_an_exception_when_at_least_one_element_doesnt_match_regex() {
		setup:
		Collection collection = collection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.allElementsMatch("[0-9]")

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e).hasMessageContaining(
				"The value <a> doesn't match the regex <[0-9]>")
	}

	def should_throw_an_exception_when_element_is_null() {
		setup:
		Collection collection = collectionWithNulls()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.allElementsMatch("[0-9]")

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e).hasMessageContaining(
					"The value <null> doesn't match the regex <[0-9]>")

	}

	def should_throw_an_exception_when_collection_is_null() {
		setup:
		Collection collection = null

		when:
		SpringCloudContractAssertions.assertThat(collection).allElementsMatch("foo")

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("Expecting actual not to be null")
	}

	def should_throw_an_exception_when_collection_is_empty() {
		setup:
		Collection collection = new ArrayList()

		when:
		SpringCloudContractAssertions.assertThat(collection).allElementsMatch("foo")

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("Expecting actual not to be empty")
	}

	def should_not_throw_an_exception_when_flattened_size_is_greater_than_or_equal_to_provided_size() {
		setup:
		Collection collection = nestedCollection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeGreaterThanOrEqualTo(0)
				.hasFlattenedSizeGreaterThanOrEqualTo(4)

		then:
		noExceptionThrown()
	}

	def should_throw_an_exception_when_flattened_size_is_not_greater_than_or_equal_to_provided_size() {
		setup:
		Collection collection = nestedCollection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeGreaterThanOrEqualTo(5)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e).hasMessageContaining(
				"The flattened size <4> is not greater or equal to <5>")
	}

	def should_throw_an_exception_when_collection_is_null_for_flattened_greater_than_or_equal() {
		setup:
		Collection collection = null

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeGreaterThanOrEqualTo(1)
		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null")
	}

	def should_not_throw_an_exception_when_flattened_size_is_less_than_or_equal_to_provided_size() {
		setup:
		Collection collection = nestedCollection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeLessThanOrEqualTo(5)
				.hasFlattenedSizeLessThanOrEqualTo(4)

		then:
		noExceptionThrown()
	}

	def should_throw_an_exception_when_flattened_size_is_not_less_than_or_equal_to_provided_size() {
		setup:
		Collection collection = nestedCollection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
					.hasFlattenedSizeLessThanOrEqualTo(1)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e).hasMessageContaining(
				"The flattened size <4> is not less or equal to <1>")
	}

	def should_throw_an_exception_when_collection_is_null_for_flattened_less_than_or_equal() {
		setup:
		Collection collection = null

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeLessThanOrEqualTo(1)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("Expecting actual not to be null")
	}

	def should_not_throw_an_exception_when_flattened_size_is_between_the_provided_sizes() {
		setup:
		Collection collection = nestedCollection()

		when:
		SpringCloudContractAssertions.assertThat(collection).hasFlattenedSizeBetween(1, 5)
				.hasFlattenedSizeBetween(4, 4)

		then:
		noExceptionThrown()
	}

	def should_throw_an_exception_when_flattened_size_is_not_between_the_provided_sizes() {
		setup:
		Collection collection = nestedCollection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeBetween(5, 7)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e).hasMessageContaining(
				"The flattened size <4> is not between <5> and <7>")
	}

	def should_throw_an_exception_when_collection_is_null_for_flattened_between() {
		setup:
		Collection collection = null

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasFlattenedSizeBetween(1, 2)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
					.hasMessageContaining("Expecting actual not to be null")
	}

	def should_not_throw_an_exception_when_size_is_greater_than_or_equal_to_provided_size() {
		setup:
		Collection collection = collection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeGreaterThanOrEqualTo(0).hasSizeGreaterThanOrEqualTo(3)

		then:
		noExceptionThrown()
	}

	def should_throw_an_exception_when_size_is_not_greater_than_or_equal_to_provided_size() {
		setup:
		Collection collection = collection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeGreaterThanOrEqualTo(5)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("The size <3> is not greater or equal to <5>")
	}

	def should_throw_an_exception_when_collection_is_null_for_greater_than_or_equal() {
		setup:
		Collection collection = null

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeGreaterThanOrEqualTo(1)
		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("Expecting actual not to be null")
	}

	def should_not_throw_an_exception_when_size_is_less_than_or_equal_to_provided_size() {
		setup:
		Collection collection = collection()

		when:
		SpringCloudContractAssertions.assertThat(collection).hasSizeLessThanOrEqualTo(4)
				.hasSizeLessThanOrEqualTo(3)

		then:
		noExceptionThrown()
	}

	def should_throw_an_exception_when_size_is_not_less_than_or_equal_to_provided_size() {
		setup:
		Collection collection = collection()

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeLessThanOrEqualTo(1)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("The size <3> is not less or equal to <1>")
	}

	def should_throw_an_exception_when_collection_is_null_for_less_than_or_equal() {
		setup:
		Collection collection = null

		when:
		SpringCloudContractAssertions.assertThat(collection)
				.hasSizeLessThanOrEqualTo(1)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("Expecting actual not to be null")
	}

	def should_not_throw_an_exception_when_size_is_between_the_provided_sizes() {
		setup:
		Collection collection = collection()

		when:
		SpringCloudContractAssertions.assertThat(collection).hasSizeBetween(1, 4)
				.hasSizeBetween(3, 3)

		then:
		noExceptionThrown()
	}

	def should_throw_an_exception_when_size_is_not_between_the_provided_sizes() {
		setup:
		Collection collection = collection()

		when:
			SpringCloudContractAssertions.assertThat(collection).hasSizeBetween(5, 7)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("The size <3> is not between <5> and <7>")
	}

	def should_not_break_compilation_when_using_as() {
		setup:
		Collection collection = collection()

		when:
		SpringCloudContractAssertions.assertThat(collection).as("for jsonpath x.y.z")
				.hasSizeBetween(5, 7)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e).hasMessageContaining(
				"[for jsonpath x.y.z] The size <3> is not between <5> and <7>")
	}

	def should_throw_an_exception_when_collection_is_null_for_between() {
		setup:
		Collection collection = null

		when:
		SpringCloudContractAssertions.assertThat(collection).hasSizeBetween( 1, 2)

		then:
		AssertionError e = thrown()
		Assertions.assertThat(e)
				.hasMessageContaining("Expecting actual not to be null")
	}

	Collection<String> collection() {
		List<String> list = new ArrayList<>()
		list.add("a")
		list.add("b")
		list.add("c")
		return list
	}

	Collection<String> collectionWithNulls() {
		List<String> list = new ArrayList<>()
		list.add(null)
		return list
	}

	Collection nestedCollection() {
		List list = new ArrayList<>()
		List list1 = new ArrayList<>()
		Map<String, String> map1 = new HashMap<>()
		map1.put("a", "1")
		map1.put("b", "2")
		map1.put("c", "3")
		List list2 = new ArrayList<>()
		Map<String, String> map2 = new HashMap<>()
		map2.put("d", "4")
		list.add(list1)
		list.add(list2)
		list1.add(map1)
		list2.add(map2)
		return list
	}
}

