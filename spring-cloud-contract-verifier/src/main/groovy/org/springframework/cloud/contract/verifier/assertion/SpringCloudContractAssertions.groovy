package org.springframework.cloud.contract.verifier.assertion

import groovy.transform.CompileStatic
import org.assertj.core.api.Assertions

@CompileStatic
public class SpringCloudContractAssertions extends Assertions {

	/**
	 * Creates a new instance of <code>{@link CollectionAssert}</code>.
	 * @param <ELEMENT> type to assert
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	public static <ELEMENT> CollectionAssert<ELEMENT> assertThat(
			Iterable<? extends ELEMENT> actual) {
		return new CollectionAssert<>(actual);
	}
}
