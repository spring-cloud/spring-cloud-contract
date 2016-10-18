/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification

import java.util.regex.Pattern

class RegexPatternsSpec extends Specification {

	RegexPatterns regexPatterns = new RegexPatterns()

	def "should generate a regex for ip address [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.ipAddress()).matcher(textToMatch).matches()
		where:
			textToMatch       || shouldMatch
			'123.123.123.123' || true
			'a.b.'            || false
	}

	def "should generate a regex for hostname [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.hostname()).matcher(textToMatch).matches()
		where:
			textToMatch              || shouldMatch
			'https://asd.com'        || true
			'https://asd.com:8080'   || true
			'https://localhost'      || true
			'https://localhost:8080' || true
			'https://asd.com/asd'    || false
			'asd.com'                || false
	}

	def "should generate a regex for email [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.email()).matcher(textToMatch).matches()
		where:
			textToMatch   || shouldMatch
			'asd@asd.com' || true
			'a.b.'        || false
	}

	def "should generate a regex for url [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.url()).matcher(textToMatch).matches()
		where:
			textToMatch                    || shouldMatch
			'ftp://asd.com:9090/asd/a?a=b' || true
			'a.b.'                         || false
	}

	def "should generate a regex for a number [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
		shouldMatch == Pattern.compile(regexPatterns.number()).matcher(textToMatch).matches()
		where:
		textToMatch || shouldMatch
		'1'         || true
		'1.0'       || true
		'0.1'       || true
		'.1'        || true
		'1.'        || false
	}

	def "should generate a regex for a uuid [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
		shouldMatch == Pattern.compile(regexPatterns.uuid()).matcher(textToMatch).matches()
		where:
		textToMatch							|| shouldMatch
		UUID.randomUUID().toString()   		|| true
		UUID.randomUUID().toString()   		|| true
		UUID.randomUUID().toString() + "!" 	|| false
		'dog'       						|| false
		'5'          						|| false
	}
}
