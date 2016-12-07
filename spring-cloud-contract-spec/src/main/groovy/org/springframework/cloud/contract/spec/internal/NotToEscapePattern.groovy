package org.springframework.cloud.contract.spec.internal

import java.util.regex.Pattern


/**
 * Special case of Patterns that we don't want to escape
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
class NotToEscapePattern extends DslProperty<Pattern> {

	NotToEscapePattern(Pattern clientValue, Pattern serverValue) {
		super(clientValue, serverValue)
	}

	NotToEscapePattern(Pattern singleValue) {
		super(singleValue)
	}
}
