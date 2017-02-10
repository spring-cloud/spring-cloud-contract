package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class MatchingTypeSpec extends Specification {
	def "should return [#expected] for type [#type]"() {
		expect:
			MatchingType.regexRelated(type) == expected
		where:
			type                   | expected
			MatchingType.EQUALITY  | false
			MatchingType.TYPE      | false
			MatchingType.COMMAND   | false
			MatchingType.REGEX     | true
			MatchingType.DATE      | true
			MatchingType.TIME      | true
			MatchingType.TIMESTAMP | true

	}
}
