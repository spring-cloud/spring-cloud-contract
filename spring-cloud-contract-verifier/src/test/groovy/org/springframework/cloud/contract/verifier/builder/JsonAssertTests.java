package org.springframework.cloud.contract.verifier.builder;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;

/**
 * @author Marcin Grzejszczak
 */
public class JsonAssertTests {
	// #537
	@Test
	public void should_compare_big_decimals() {
		DocumentContext context = JsonPath.parse("{\"foo\": 55534673.56}");
		assertThatJson(context).field("['foo']").isEqualTo(55534673.57);
	}
}
