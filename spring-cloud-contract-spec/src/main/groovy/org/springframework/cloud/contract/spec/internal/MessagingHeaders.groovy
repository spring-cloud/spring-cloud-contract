package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Contains most commonly used messaging headers
 *
 * @author Marcin Grzejszczak
 * @since 1.1.2
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class MessagingHeaders {

	/**
	 * The Content Type of a message
	 * @return
	 */
	String messagingContentType() {
		return "contentType"
	}
}
