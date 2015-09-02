package io.codearte.accurest.util

import org.skyscreamer.jsonassert.JSONAssert

class AssertionUtil {

	private static boolean NON_STRICT = false

	public static void assertThatJsonsAreEqual(String expected, String actual) {
		JSONAssert.assertEquals(expected, actual, NON_STRICT)
	}
}
