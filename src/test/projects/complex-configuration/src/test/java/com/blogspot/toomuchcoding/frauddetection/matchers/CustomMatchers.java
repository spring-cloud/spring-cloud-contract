package com.blogspot.toomuchcoding.frauddetection.matchers;

import org.junit.Assert;

public class CustomMatchers {

	public static void assertThatRejectionReasonIsNull(String rejectionReason) {
		Assert.assertNull(rejectionReason);
	}

}
