package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Contains most commonly used media types
 *
 * @author Marcin Grzejszczak
 * @since 1.0.2
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class MediaTypes {

	String allValue() {
		return "*/*"
	}

	String applicationAtomXml() {
		return "application/atom+xml"
	}

	String applicationFormUrlencoded() {
		return "application/x-www-form-urlencoded"
	}

	String applicationJson() {
		return "application/json"
	}

	String applicationJsonUtf8() {
		return applicationJson() + ";charset=UTF-8"
	}

	String applicationOctetStream() {
		return "application/octet-stream"
	}

	String applicationPdf() {
		return "application/pdf"
	}

	String applicationXhtmlXml() {
		return "application/xhtml+xml"
	}

	String applicationXml() {
		return "application/xml"
	}

	String imageGif() {
		return "image/gif"
	}

	String imageJpeg() {
		return "image/jpeg"
	}

	String imagePng() {
		return "image/png"
	}

	String multipartFormData() {
		return "multipart/form-data"
	}

	String textHtml() {
		return "text/html"
	}

	String textMarkdown() {
		return "text/markdown"
	}

	String textPlain() {
		return "text/plain"
	}

	String textXml() {
		return "text/xml"
	}
}
