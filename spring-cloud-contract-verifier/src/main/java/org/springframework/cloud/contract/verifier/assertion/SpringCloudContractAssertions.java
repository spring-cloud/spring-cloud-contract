package org.springframework.cloud.contract.verifier.assertion;

import org.assertj.core.api.Assertions;

/**
 * Assertions used by the generated tests
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class SpringCloudContractAssertions extends Assertions {

	/**
	 * Creates a new instance of <code>{@link CollectionAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	public static <ELEMENT> CollectionAssert<ELEMENT> assertThat(Iterable<? extends ELEMENT> actual) {
		return new CollectionAssert<>(actual);
	}
}
