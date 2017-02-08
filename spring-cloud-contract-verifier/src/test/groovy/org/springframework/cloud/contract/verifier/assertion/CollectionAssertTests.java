package org.springframework.cloud.contract.verifier.assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	private Collection<String> collection() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		return list;
	}

}