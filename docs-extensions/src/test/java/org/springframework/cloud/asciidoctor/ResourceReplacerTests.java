package org.springframework.cloud.asciidoctor;

import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class ResourceReplacerTests {

	@Test
	public void should_process_the_resources() throws Exception {
		ResourceReplacer replacer = new ResourceReplacer();

		String output = replacer.replaceOutput("<head></head>");

		then(output).contains("http://cdnjs.cloudflare.com/ajax/libs/zepto/1.1.6/zepto.min.js");
	}

}