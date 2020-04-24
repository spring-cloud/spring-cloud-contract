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

package org.springframework.cloud.contract.spec.internal;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author Tim Ysewyn
 */
class MediaTypesTests {

	@Test
	public void ALL_VALUE() {
		BDDAssertions.then(MediaTypes.ALL_VALUE).isEqualTo("*/*");
		BDDAssertions.then(new MediaTypes().allValue()).isEqualTo(MediaTypes.ALL_VALUE);
	}

	@Test
	public void APPLICATION_ATOM_XML() {
		BDDAssertions.then(MediaTypes.APPLICATION_ATOM_XML)
				.isEqualTo("application/atom+xml");
		BDDAssertions.then(new MediaTypes().applicationAtomXml())
				.isEqualTo(MediaTypes.APPLICATION_ATOM_XML);
	}

	@Test
	public void APPLICATION_FORM_URLENCODED() {
		BDDAssertions.then(MediaTypes.APPLICATION_FORM_URLENCODED)
				.isEqualTo("application/x-www-form-urlencoded");
		BDDAssertions.then(new MediaTypes().applicationFormUrlencoded())
				.isEqualTo(MediaTypes.APPLICATION_FORM_URLENCODED);
	}

	@Test
	public void APPLICATION_JSON() {
		BDDAssertions.then(MediaTypes.APPLICATION_JSON).isEqualTo("application/json");
		BDDAssertions.then(new MediaTypes().applicationJson())
				.isEqualTo(MediaTypes.APPLICATION_JSON);
	}

	@Test
	public void APPLICATION_JSON_UTF8() {
		BDDAssertions.then(MediaTypes.APPLICATION_JSON_UTF8)
				.isEqualTo("application/json;charset=UTF-8");
		BDDAssertions.then(new MediaTypes().applicationJsonUtf8())
				.isEqualTo(MediaTypes.APPLICATION_JSON_UTF8);
	}

	@Test
	public void APPLICATION_OCTET_STREAM() {
		BDDAssertions.then(MediaTypes.APPLICATION_OCTET_STREAM)
				.isEqualTo("application/octet-stream");
		BDDAssertions.then(new MediaTypes().applicationOctetStream())
				.isEqualTo(MediaTypes.APPLICATION_OCTET_STREAM);
	}

	@Test
	public void APPLICATION_PDF() {
		BDDAssertions.then(MediaTypes.APPLICATION_PDF).isEqualTo("application/pdf");
		BDDAssertions.then(new MediaTypes().applicationPdf())
				.isEqualTo(MediaTypes.APPLICATION_PDF);
	}

	@Test
	public void APPLICATION_XHTML_XML() {
		BDDAssertions.then(MediaTypes.APPLICATION_XHTML_XML)
				.isEqualTo("application/xhtml+xml");
		BDDAssertions.then(new MediaTypes().applicationXhtmlXml())
				.isEqualTo(MediaTypes.APPLICATION_XHTML_XML);
	}

	@Test
	public void APPLICATION_XML() {
		BDDAssertions.then(MediaTypes.APPLICATION_XML).isEqualTo("application/xml");
		BDDAssertions.then(new MediaTypes().applicationXml())
				.isEqualTo(MediaTypes.APPLICATION_XML);
	}

	@Test
	public void IMAGE_GIF() {
		BDDAssertions.then(MediaTypes.IMAGE_GIF).isEqualTo("image/gif");
		BDDAssertions.then(new MediaTypes().imageGif()).isEqualTo(MediaTypes.IMAGE_GIF);
	}

	@Test
	public void IMAGE_JPEG() {
		BDDAssertions.then(MediaTypes.IMAGE_JPEG).isEqualTo("image/jpeg");
		BDDAssertions.then(new MediaTypes().imageJpeg()).isEqualTo(MediaTypes.IMAGE_JPEG);
	}

	@Test
	public void IMAGE_PNG() {
		BDDAssertions.then(MediaTypes.IMAGE_PNG).isEqualTo("image/png");
		BDDAssertions.then(new MediaTypes().imagePng()).isEqualTo(MediaTypes.IMAGE_PNG);
	}

	@Test
	public void MULTIPART_FORM_DATA() {
		BDDAssertions.then(MediaTypes.MULTIPART_FORM_DATA)
				.isEqualTo("multipart/form-data");
		BDDAssertions.then(new MediaTypes().multipartFormData())
				.isEqualTo(MediaTypes.MULTIPART_FORM_DATA);
	}

	@Test
	public void TEXT_HTML() {
		BDDAssertions.then(MediaTypes.TEXT_HTML).isEqualTo("text/html");
		BDDAssertions.then(new MediaTypes().textHtml()).isEqualTo(MediaTypes.TEXT_HTML);
	}

	@Test
	public void TEXT_MARKDOWN() {
		BDDAssertions.then(MediaTypes.TEXT_MARKDOWN).isEqualTo("text/markdown");
		BDDAssertions.then(new MediaTypes().textMarkdown())
				.isEqualTo(MediaTypes.TEXT_MARKDOWN);
	}

	@Test
	public void TEXT_PLAIN() {
		BDDAssertions.then(MediaTypes.TEXT_PLAIN).isEqualTo("text/plain");
		BDDAssertions.then(new MediaTypes().textPlain()).isEqualTo(MediaTypes.TEXT_PLAIN);
	}

	@Test
	public void TEXT_XML() {
		BDDAssertions.then(MediaTypes.TEXT_XML).isEqualTo("text/xml");
		BDDAssertions.then(new MediaTypes().textXml()).isEqualTo(MediaTypes.TEXT_XML);
	}

}
