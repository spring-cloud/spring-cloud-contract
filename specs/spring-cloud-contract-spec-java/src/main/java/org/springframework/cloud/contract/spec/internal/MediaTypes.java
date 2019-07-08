/*
 * Copyright 2013-2019 the original author or authors.
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

/**
 * Contains most commonly used media types.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.2
 */
public class MediaTypes {

	public String allValue() {
		return "*/*";
	}

	public String applicationAtomXml() {
		return "application/atom+xml";
	}

	public String applicationFormUrlencoded() {
		return "application/x-www-form-urlencoded";
	}

	public String applicationJson() {
		return "application/json";
	}

	public String applicationJsonUtf8() {
		return applicationJson() + ";charset=UTF-8";
	}

	public String applicationOctetStream() {
		return "application/octet-stream";
	}

	public String applicationPdf() {
		return "application/pdf";
	}

	public String applicationXhtmlXml() {
		return "application/xhtml+xml";
	}

	public String applicationXml() {
		return "application/xml";
	}

	public String imageGif() {
		return "image/gif";
	}

	public String imageJpeg() {
		return "image/jpeg";
	}

	public String imagePng() {
		return "image/png";
	}

	public String multipartFormData() {
		return "multipart/form-data";
	}

	public String textHtml() {
		return "text/html";
	}

	public String textMarkdown() {
		return "text/markdown";
	}

	public String textPlain() {
		return "text/plain";
	}

	public String textXml() {
		return "text/xml";
	}

}
